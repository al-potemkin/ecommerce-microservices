package com.sndbx.order.service;

import com.sndbx.order.dto.payload.OrderEventItem;
import com.sndbx.order.dto.payload.OrderEventPayload;
import com.sndbx.order.entity.Order;
import com.sndbx.order.entity.outbox.OrderOutboxType;
import com.sndbx.order.entity.outbox.OutboxEvent;
import com.sndbx.order.entity.outbox.OutboxStatus;
import com.sndbx.order.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class OutboxRecorder {

    private final OutboxRepository repository;
    private final ObjectMapper objectMapper;

    @Transactional(propagation = Propagation.MANDATORY)
    public void record(Order order) {
        OrderOutboxType type = switch (order.getStatus()) {
            case AWAITING_PAYMENT -> OrderOutboxType.ORDER_CREATED;
            case PAID -> OrderOutboxType.ORDER_PAID;
            case SHIPPED -> OrderOutboxType.ORDER_SHIPPED;
            case RETURN_REQUESTED -> OrderOutboxType.ORDER_RETURN_REQUESTED;
            case RETURNED -> OrderOutboxType.ORDER_RETURNED;
            case CANCELLED -> OrderOutboxType.ORDER_CANCELLED;
            case EXPIRED -> OrderOutboxType.ORDER_EXPIRED;
        };

        List<OrderEventItem> items = order.getItems().stream()
                .map(i -> new OrderEventItem(i.getSku(), i.getQuantity(), i.getUnitPrice()))
                .toList();

        OrderEventPayload event = new OrderEventPayload(
                UUID.randomUUID().toString(),
                type,
                Instant.now(),
                order.getId().toString(),
                order.getUserId().toString(),
                order.getStatus(),
                order.getTotalAmount(),
                items);

        repository.save(OutboxEvent.builder()
                .eventId(event.eventId())
                .aggregateId(event.orderId())
                .type(type)
                .payload(objectMapper.writeValueAsString(event))
                .status(OutboxStatus.PENDING)
                .createdAt(event.occurredAt())
                .build());
    }
}