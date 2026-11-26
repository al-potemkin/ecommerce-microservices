package com.sndbx.product.service;

import com.sndbx.product.config.KafkaTopicConfig;
import com.sndbx.product.dto.payload.ProductEventPayload;
import com.sndbx.product.events.ProductEvent;
import com.sndbx.product.mapper.ProductEventAvroMapper;
import com.sndbx.product.model.outbox.OutboxEvent;
import com.sndbx.product.model.outbox.OutboxStatus;
import com.sndbx.product.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxRelay {

    private static final long SEND_TIMEOUT_SECONDS = 10;

    private final OutboxRepository repository;
    private final KafkaTemplate<String, ProductEvent> kafkaTemplate;
    private final ProductEventAvroMapper avroMapper;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelayString = "${app.outbox.poll-delay-ms:1000}")
    public void publishPending() {
        List<OutboxEvent> batch = repository.findTop100ByStatusOrderByIdAsc(OutboxStatus.PENDING);
        for (OutboxEvent event : batch) {
            if (!send(event)) {
                return;
            }
        }
    }

    private boolean send(OutboxEvent event) {
        try {
            ProductEvent message = avroMapper.toAvro(objectMapper.readValue(event.getPayload(), ProductEventPayload.class));
            kafkaTemplate.send(KafkaTopicConfig.PRODUCT_EVENTS, event.getAggregateId(), message)
                    .get(SEND_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (Exception e) {
            log.warn("Outbox event {} ({}) was not sent, will retry: {}",
                    event.getEventId(), event.getType(), e.toString());
            repository.incrementAttempts(event.getId());
            return false;
        }
        repository.markSent(event.getId(), Instant.now());
        return true;
    }
}