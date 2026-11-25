package com.sndbx.inventory.service;

import com.sndbx.inventory.dto.payload.InventoryEventPayload;
import com.sndbx.inventory.dto.payload.ReservationEventPayload;
import com.sndbx.inventory.entity.outbox.OutboxEvent;
import com.sndbx.inventory.entity.outbox.OutboxStatus;
import com.sndbx.inventory.entity.outbox.StockEventType;
import com.sndbx.inventory.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OutboxRecorder {

    private final OutboxRepository repository;
    private final ObjectMapper objectMapper;

    @Transactional(propagation = Propagation.MANDATORY)
    public void record(StockEventType type, String sku) {
        InventoryEventPayload event = new InventoryEventPayload(
                UUID.randomUUID().toString(),
                type,
                Instant.now(),
                sku);

        repository.save(OutboxEvent.builder()
                .eventId(event.eventId())
                .aggregateId(sku)
                .type(type)
                .payload(objectMapper.writeValueAsString(event))
                .status(OutboxStatus.PENDING)
                .createdAt(event.occurredAt())
                .build());
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void recordReservationExpired(String orderId) {
        ReservationEventPayload event = new ReservationEventPayload(
                UUID.randomUUID().toString(),
                Instant.now(),
                orderId);

        repository.save(OutboxEvent.builder()
                .eventId(event.eventId())
                .aggregateId(orderId)
                .type(StockEventType.RESERVATION_EXPIRED)
                .payload(objectMapper.writeValueAsString(event))
                .status(OutboxStatus.PENDING)
                .createdAt(event.occurredAt())
                .build());
    }
}