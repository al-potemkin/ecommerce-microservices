package com.sndbx.inventory.service;

import com.sndbx.common.exception.data.ApiProblem;
import com.sndbx.common.exception.data.CommonErrorCode;
import com.sndbx.inventory.dto.request.ReservationItemRequest;
import com.sndbx.inventory.dto.request.ReserveRequest;
import com.sndbx.inventory.dto.response.ReservationResponse;
import com.sndbx.inventory.entity.Reservation;
import com.sndbx.inventory.entity.ReservationStatus;
import com.sndbx.inventory.entity.StockItem;
import com.sndbx.inventory.exception.InsufficientStockException;
import com.sndbx.inventory.exception.ReservationConflictException;
import com.sndbx.inventory.exception.ReservationNotFoundException;
import com.sndbx.inventory.exception.StockItemNotFoundException;
import com.sndbx.inventory.exception.config.InventoryErrorCode;
import com.sndbx.inventory.mapper.ReservationMapper;
import com.sndbx.inventory.repository.ReservationRepository;
import com.sndbx.inventory.repository.StockItemRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ReservationService {

    private final StockItemRepository stockRepository;
    private final ReservationRepository reservationRepository;
    private final OutboxRecorder outboxRecorder;
    private final ReservationMapper mapper;
    private final StockAvailability availability;
    private final Duration ttl;

    public ReservationService(StockItemRepository stockRepository,
                              ReservationRepository reservationRepository, OutboxRecorder outboxRecorder,
                              ReservationMapper mapper,
                              StockAvailability availability,
                              @Value("${app.reservation.ttl-minutes:15}") long ttlMinutes) {
        this.stockRepository = stockRepository;
        this.reservationRepository = reservationRepository;
        this.outboxRecorder = outboxRecorder;
        this.mapper = mapper;
        this.availability = availability;
        this.ttl = Duration.ofMinutes(ttlMinutes);
    }

    @Transactional
    public ReservationResponse reserve(ReserveRequest request) {
        Map<String, Integer> requested = mergeBySku(request.items());

        List<StockItem> items = stockRepository.lockBySkus(requested.keySet());

        List<Reservation> existing = reservationRepository.findByOrderId(request.orderId());
        if (!existing.isEmpty()) {
            return mapper.toResponse(request.orderId(), existing);
        }

        Map<String, StockItem> bySku = items.stream()
                .collect(Collectors.toMap(StockItem::getSku, Function.identity()));

        List<String> missing = requested.keySet().stream()
                .filter(sku -> !bySku.containsKey(sku))
                .toList();
        if (!missing.isEmpty()) {
            throw new StockItemNotFoundException(missing);
        }

        List<ApiProblem.FieldError> shortages = new ArrayList<>();
        requested.forEach((sku, quantity) -> {
            int available = availability.of(bySku.get(sku));
            if (available < quantity) {
                shortages.add(new ApiProblem.FieldError(sku,
                        "requested " + quantity + ", available " + available));
            }
        });
        if (!shortages.isEmpty()) {
            throw new InsufficientStockException(shortages);
        }

        Instant expiresAt = Instant.now().plus(ttl);
        List<Reservation> created = new ArrayList<>();
        requested.forEach((sku, quantity) -> {
            StockItem item = bySku.get(sku);
            int availableBefore = availability.of(item);
            item.setReserved(item.getReserved() + quantity);
            availability.recordChange(item, availableBefore);
            created.add(Reservation.builder()
                    .orderId(request.orderId())
                    .stockItem(item)
                    .quantity(quantity)
                    .status(ReservationStatus.RESERVED)
                    .expiresAt(expiresAt)
                    .build());
        });
        reservationRepository.saveAll(created);

        return mapper.toResponse(request.orderId(), created);
    }

    @Transactional
    public ReservationResponse commit(String orderId) {
        stockRepository.lockByOrderId(orderId);
        List<Reservation> reservations = loadOrThrow(orderId);

        switch (reservations.getFirst().getStatus()) {
            case COMMITTED, RETURNED -> {
                return mapper.toResponse(orderId, reservations);
            }
            case RELEASED -> throw new ReservationConflictException(
                    InventoryErrorCode.RESERVATION_RELEASED,
                    "Reservation for order " + orderId + " was released or expired and cannot be committed"
            );
            case RESERVED -> {
                for (Reservation reservation : reservations) {
                    StockItem item = reservation.getStockItem();
                    item.setOnHand(item.getOnHand() - reservation.getQuantity());
                    item.setReserved(item.getReserved() - reservation.getQuantity());
                    reservation.setStatus(ReservationStatus.COMMITTED);
                }
            }
        }
        return mapper.toResponse(orderId, reservations);
    }

    @Transactional
    public ReservationResponse release(String orderId) {
        stockRepository.lockByOrderId(orderId);
        List<Reservation> reservations = loadOrThrow(orderId);

        switch (reservations.getFirst().getStatus()) {
            case RELEASED -> {
                return mapper.toResponse(orderId, reservations);
            }
            case COMMITTED, RETURNED ->
                    throw new ReservationConflictException(InventoryErrorCode.RESERVATION_ALREADY_COMMITTED,
                            "Reservation for order " + orderId + " is already committed and cannot be released");
            case RESERVED -> {
                for (Reservation reservation : reservations) {
                    StockItem item = reservation.getStockItem();
                    int availableBefore = availability.of(item);
                    item.setReserved(item.getReserved() - reservation.getQuantity());
                    reservation.setStatus(ReservationStatus.RELEASED);
                    availability.recordChange(item, availableBefore);
                }
            }
        }
        return mapper.toResponse(orderId, reservations);
    }

    @Transactional
    public void returnToStock(String orderId) {
        stockRepository.lockByOrderId(orderId);
        List<Reservation> reservations = loadOrThrow(orderId);

        switch (reservations.getFirst().getStatus()) {
            case RETURNED -> {
            }
            case RESERVED, RELEASED -> throw new ReservationConflictException(CommonErrorCode.CONFLICT,
                    "Order " + orderId + " was not committed, nothing to return to stock");
            case COMMITTED -> {
                for (Reservation reservation : reservations) {
                    StockItem item = reservation.getStockItem();
                    int availableBefore = availability.of(item);
                    item.setOnHand(item.getOnHand() + reservation.getQuantity());
                    reservation.setStatus(ReservationStatus.RETURNED);
                    availability.recordChange(item, availableBefore);
                }
            }
        }
    }

    @Transactional
    public void expire(String orderId) {
        stockRepository.lockByOrderId(orderId);
        boolean stillReserved = loadOrThrow(orderId).getFirst().getStatus() == ReservationStatus.RESERVED;
        release(orderId);
        if (stillReserved) {
            outboxRecorder.recordReservationExpired(orderId);
        }
    }

    @Transactional(readOnly = true)
    public ReservationResponse get(String orderId) {
        return mapper.toResponse(orderId, loadOrThrow(orderId));
    }

    private List<Reservation> loadOrThrow(String orderId) {
        List<Reservation> reservations = reservationRepository.findByOrderId(orderId);
        if (reservations.isEmpty()) {
            throw new ReservationNotFoundException(orderId);
        }
        return reservations;
    }

    private Map<String, Integer> mergeBySku(List<ReservationItemRequest> items) {
        Map<String, Integer> merged = new TreeMap<>();
        for (ReservationItemRequest item : items) {
            merged.merge(item.sku(), item.quantity(), Integer::sum);
        }
        return merged;
    }
}