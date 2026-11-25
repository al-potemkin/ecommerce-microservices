package com.sndbx.order.service;

import com.sndbx.order.config.KafkaTopicConfig;
import com.sndbx.order.dto.payload.OrderEventPayload;
import com.sndbx.order.entity.outbox.OutboxEvent;
import com.sndbx.order.entity.outbox.OutboxStatus;
import com.sndbx.order.events.OrderEvent;
import com.sndbx.order.mapper.EventAvroMapper;
import com.sndbx.order.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxRelay {

    private static final int BATCH_SIZE = 100;
    private static final long SEND_TIMEOUT_SECONDS = 10;

    private final OutboxRepository repository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final EventAvroMapper avroMapper;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelayString = "${app.outbox.poll-delay-ms:1000}")
    @Transactional
    public void publishPending() {
        List<OutboxEvent> batch = repository.lockPending(BATCH_SIZE);
        for (OutboxEvent event : batch) {
            if (!send(event)) {
                return;
            }
        }
    }

    private boolean send(OutboxEvent event) {
        try {
            OrderEvent message = avroMapper.toAvro(
                    objectMapper.readValue(event.getPayload(), OrderEventPayload.class));
            kafkaTemplate.send(KafkaTopicConfig.ORDER_EVENTS, event.getAggregateId(), message)
                    .get(SEND_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (Exception e) {
            log.warn("Outbox event {} ({}) was not sent, will retry: {}",
                    event.getEventId(), event.getType(), e.toString());
            event.setAttempts(event.getAttempts() + 1);
            return false;
        }
        event.setStatus(OutboxStatus.SENT);
        event.setSentAt(Instant.now());
        event.setAttempts(event.getAttempts() + 1);
        return true;
    }
}