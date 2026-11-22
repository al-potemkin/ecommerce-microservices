package com.sndbx.product.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.sndbx.product.model.Product;
import com.sndbx.product.model.Variant;
import com.sndbx.product.model.outbox.OutboxEvent;
import com.sndbx.product.model.outbox.OutboxStatus;
import com.sndbx.product.model.outbox.ProductEventPayload;
import com.sndbx.product.model.outbox.ProductEventType;
import com.sndbx.product.repository.OutboxRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class OutboxRecorder {

    private final OutboxRepository repository;
    private final ObjectMapper objectMapper;

    @Transactional(propagation = Propagation.MANDATORY)
    public void record(ProductEventType type, Product product, List<Variant> variants) {
        ProductEventPayload event = new ProductEventPayload(
                UUID.randomUUID().toString(),
                type,
                Instant.now(),
                product.getId(),
                product.getName(),
                product.isActive(),
                variants.stream()
                        .map(v -> new ProductEventPayload.VariantSnapshot(v.sku(), v.volumeMl(), v.active()))
                        .toList());

        repository.save(OutboxEvent.builder()
                .eventId(event.eventId())
                .aggregateId(product.getId())
                .type(type.name())
                .payload(objectMapper.writeValueAsString(event))
                .status(OutboxStatus.PENDING)
                .createdAt(event.occurredAt())
                .build());
    }
}
