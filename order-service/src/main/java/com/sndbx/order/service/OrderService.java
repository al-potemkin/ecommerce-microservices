package com.sndbx.order.service;

import com.sndbx.common.exception.data.ApiProblem;
import com.sndbx.order.client.InventoryClient;
import com.sndbx.order.client.ProductClient;
import com.sndbx.order.dto.client.ReservationItem;
import com.sndbx.order.dto.client.ReservationResponse;
import com.sndbx.order.dto.client.ReserveRequest;
import com.sndbx.order.dto.client.VariantInfo;
import com.sndbx.order.dto.request.CreateOrderRequest;
import com.sndbx.order.dto.request.DeliveryRequest;
import com.sndbx.order.dto.request.OrderItemRequest;
import com.sndbx.order.dto.response.OrderResponse;
import com.sndbx.order.dto.response.OrderSummaryResponse;
import com.sndbx.order.dto.response.PageResponse;
import com.sndbx.order.entity.Order;
import com.sndbx.order.entity.OrderItem;
import com.sndbx.order.entity.OrderStatus;
import com.sndbx.order.exception.InvalidOrderTransitionException;
import com.sndbx.order.exception.OrderItemsUnavailableException;
import com.sndbx.order.exception.OrderNotFoundException;
import com.sndbx.order.exception.ReservationAlreadyCommittedException;
import com.sndbx.order.exception.ReservationReleasedException;
import com.sndbx.order.exception.ReturnWindowExpiredException;
import com.sndbx.order.mapper.OrderMapper;
import com.sndbx.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper mapper;
    private final ProductClient productClient;
    private final InventoryClient inventoryClient;
    private final OutboxRecorder outboxRecorder;
    private final TransactionTemplate tx;

    @Value("${app.order.return-window-days}")
    private int returnWindowDays;

    private static final Set<OrderStatus> PAID_OR_LATER = EnumSet.of(
            OrderStatus.PAID, OrderStatus.SHIPPED,
            OrderStatus.RETURN_REQUESTED, OrderStatus.RETURNED
    );


    /**
     * Намеренно без @Transactional: внутри есть HTTP-вызовы, держать ради них
     * соединение с БД нельзя. Сохранение выполняет сам репозиторий в своей транзакции.
     */
    public OrderResponse create(UUID userId, String idempotencyKey, CreateOrderRequest request) {
        UUID orderId = deterministicOrderId(userId, idempotencyKey);

        Optional<Order> existing = orderRepository.findWithItemsById(orderId);
        if (existing.isPresent()) {
            log.info("Idempotent replay for order {}", orderId);
            return mapper.toResponse(existing.get());
        }

        Map<String, Integer> requested = mergeBySku(request.items());
        Map<String, VariantInfo> variants = loadAvailableVariants(requested.keySet());

        ReservationResponse reservation = reserve(orderId, requested);

        Order order = buildOrder(orderId, userId, request.delivery(), requested, variants, reservation.expiresAt());
        try {
            Order saved = tx.execute(status -> {
                Order persisted = orderRepository.save(order);
                outboxRecorder.record(persisted);
                return persisted;
            });
            OrderResponse response = mapper.toResponse(saved);
            log.info("Created order {} for user {}", orderId, userId);
            return response;
        } catch (DataIntegrityViolationException e) {
            // параллельный запрос с тем же ключом успел сохранить заказ раньше
            return orderRepository.findWithItemsById(orderId)
                    .map(mapper::toResponse)
                    .orElseThrow(() -> e);
        }
    }

    public PageResponse<OrderSummaryResponse> list(UUID userId, int page, int size) {
        Page<Order> result = orderRepository.findByUserId(userId,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        return new PageResponse<>(
                result.map(mapper::toSummary).getContent(),
                page, size, result.getTotalElements(), result.getTotalPages());
    }

    public OrderResponse get(UUID userId, UUID orderId) {
        return orderRepository.findWithItemsById(orderId)
                .filter(order -> order.getUserId().equals(userId))
                .map(mapper::toResponse)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    /**
     * Меняет статус заказа и проставляет время перехода.
     * Вызывается внутри транзакции вызывающего кода с управляемой сущностью:
     * изменения сохранит dirty checking, а @Version не даст двум параллельным
     * переходам перезаписать друг друга.
     */
    public void transition(Order order, OrderStatus target) {
        OrderStatus current = order.getStatus();
        if (!current.canTransitionTo(target)) {
            throw new InvalidOrderTransitionException(order.getId(), current, target);
        }

        Instant now = Instant.now();
        switch (target) {
            case PAID -> order.setPaidAt(now);
            case SHIPPED -> order.setShippedAt(now);
            case RETURN_REQUESTED -> order.setReturnRequestedAt(now);
            case RETURNED, CANCELLED, EXPIRED -> order.setFinishedAt(now);
            case AWAITING_PAYMENT -> {
            }
        }
        order.setStatus(target);
        outboxRecorder.record(order);
    }

    public void onPaymentSucceeded(UUID orderId) {
        Order order = getOrThrow(orderId);
        if (PAID_OR_LATER.contains(order.getStatus())) {
            log.info("Order {} is already paid, ignoring duplicate payment event", orderId);
            return;
        }
        if (order.getStatus() != OrderStatus.AWAITING_PAYMENT) {
            throw new InvalidOrderTransitionException(orderId, order.getStatus(), OrderStatus.PAID);
        }
        try {
            inventoryClient.commit(orderId.toString());
        } catch (ReservationReleasedException e) {
            log.error("Order {} was paid but its reservation is already released, refund required", orderId);
            throw e;
        }
        changeStatus(orderId, OrderStatus.PAID);
    }

    public void onPaymentFailed(UUID orderId) {
        Order order = getOrThrow(orderId);
        if (order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.EXPIRED) {
            log.info("Order {} is already closed, ignoring payment failure event", orderId);
            return;
        }
        if (order.getStatus() != OrderStatus.AWAITING_PAYMENT) {
            throw new InvalidOrderTransitionException(orderId, order.getStatus(), OrderStatus.CANCELLED);
        }
        inventoryClient.release(orderId.toString());
        changeStatus(orderId, OrderStatus.CANCELLED);
    }

    public OrderResponse cancel(UUID userId, UUID orderId) {
        Order order = getOrThrow(orderId);
        if (!order.getUserId().equals(userId)) {
            throw new OrderNotFoundException(orderId);
        }
        if (order.getStatus() != OrderStatus.CANCELLED) {
            if (order.getStatus() != OrderStatus.AWAITING_PAYMENT) {
                throw new InvalidOrderTransitionException(orderId, order.getStatus(), OrderStatus.CANCELLED);
            }
            inventoryClient.release(orderId.toString());
            changeStatus(orderId, OrderStatus.CANCELLED);
        }
        return get(userId, orderId);
    }

    public OrderResponse requestReturn(UUID userId, UUID orderId) {
        tx.executeWithoutResult(s -> {
            Order order = getOrThrow(orderId);
            if (!order.getUserId().equals(userId)) {
                throw new OrderNotFoundException(orderId);
            }
            if (order.getStatus() == OrderStatus.RETURN_REQUESTED) {
                return;
            }
            if (order.getStatus() == OrderStatus.SHIPPED
                    && order.getShippedAt().plus(Duration.ofDays(returnWindowDays)).isBefore(Instant.now())) {
                throw new ReturnWindowExpiredException(returnWindowDays);
            }
            transition(order, OrderStatus.RETURN_REQUESTED);
        });
        return get(userId, orderId);
    }

    public PageResponse<OrderSummaryResponse> listAll(OrderStatus status, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Order> result = status == null
                ? orderRepository.findAll(pageable)
                : orderRepository.findByStatus(status, pageable);
        return new PageResponse<>(
                result.map(mapper::toSummary).getContent(),
                page, size, result.getTotalElements(), result.getTotalPages()
        );
    }

    public OrderResponse getForManager(UUID orderId) {
        return orderRepository.findWithItemsById(orderId)
                .map(mapper::toResponse)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    public OrderResponse ship(UUID orderId) {
        changeStatus(orderId, OrderStatus.SHIPPED);
        return getForManager(orderId);
    }

    public OrderResponse confirmReturn(UUID orderId) {
        changeStatus(orderId, OrderStatus.RETURNED);
        return getForManager(orderId);
    }

    public void onReservationExpired(UUID orderId) {
        Optional<Order> found = orderRepository.findById(orderId);
        if (found.isEmpty()) {
            log.info("Reservation expired for unknown order {}, ignoring", orderId);
            return;
        }
        OrderStatus status = found.get().getStatus();
        if (status != OrderStatus.AWAITING_PAYMENT) {
            log.info("Reservation expired for order {} in status {}, ignoring", orderId, status);
            return;
        }
        changeStatus(orderId, OrderStatus.EXPIRED);
        log.info("Order {} expired", orderId);
    }

    private Order getOrThrow(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    private void changeStatus(UUID orderId, OrderStatus target) {
        tx.executeWithoutResult(status -> transition(getOrThrow(orderId), target));
    }

    private UUID deterministicOrderId(UUID userId, String idempotencyKey) {
        return UUID.nameUUIDFromBytes((userId + ":" + idempotencyKey).getBytes(StandardCharsets.UTF_8));
    }

    private Map<String, Integer> mergeBySku(List<OrderItemRequest> items) {
        Map<String, Integer> merged = new TreeMap<>();
        for (OrderItemRequest item : items) {
            merged.merge(item.sku(), item.quantity(), Integer::sum);
        }
        return merged;
    }

    private Map<String, VariantInfo> loadAvailableVariants(Set<String> skus) {
        Map<String, VariantInfo> bySku = productClient.getVariants(skus).stream()
                .collect(Collectors.toMap(VariantInfo::sku, Function.identity()));

        List<ApiProblem.FieldError> problems = new ArrayList<>();
        for (String sku : skus) {
            VariantInfo variant = bySku.get(sku);
            if (variant == null) {
                problems.add(new ApiProblem.FieldError(sku, "not found"));
            } else if (!variant.active()) {
                problems.add(new ApiProblem.FieldError(sku, "not available"));
            }
        }
        if (!problems.isEmpty()) {
            throw new OrderItemsUnavailableException(problems);
        }
        return bySku;
    }

    private ReservationResponse reserve(UUID orderId, Map<String, Integer> requested) {
        List<ReservationItem> items = requested.entrySet().stream()
                .map(e -> new ReservationItem(e.getKey(), e.getValue()))
                .toList();

        ReservationResponse reservation = inventoryClient.reserve(new ReserveRequest(orderId.toString(), items));

        switch (reservation.status()) {
            case RESERVED -> {
            }
            case RELEASED -> throw new ReservationReleasedException(
                    "The reservation for this Idempotency-Key has expired, retry with a new key");
            case COMMITTED -> throw new ReservationAlreadyCommittedException(
                    "The reservation for this Idempotency-Key is already committed, retry with a new key");
        }
        return reservation;
    }

    private Order buildOrder(UUID orderId, UUID userId, DeliveryRequest delivery,
                             Map<String, Integer> requested, Map<String, VariantInfo> variants,
                             Instant expiresAt) {
        Order order = Order.builder()
                .id(orderId)
                .userId(userId)
                .status(OrderStatus.AWAITING_PAYMENT)
                .expiresAt(expiresAt)
                .build();
        mapper.applyDelivery(delivery, order);

        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<String, Integer> entry : requested.entrySet()) {
            OrderItem item = mapper.toItem(variants.get(entry.getKey()), entry.getValue());
            order.addItem(item);
            total = total.add(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }
        order.setTotalAmount(total.setScale(2, RoundingMode.HALF_UP));
        return order;
    }
}