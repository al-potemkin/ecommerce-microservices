package com.sndbx.inventory.service;


import com.sndbx.inventory.entity.outbox.OutboxStatus;
import com.sndbx.inventory.repository.OutboxRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@Component
public class OutboxCleaner {

    private final OutboxRepository repository;
    private final Duration retention;

    public OutboxCleaner(OutboxRepository repository,
                         @Value("${app.outbox.retention-days:7}") long retentionDays) {
        this.repository = repository;
        this.retention = Duration.ofDays(retentionDays);
    }

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void deleteOldSent() {
        int deleted = repository.deleteSentBefore(OutboxStatus.SENT, Instant.now().minus(retention));
        if (deleted > 0) {
            log.info("Deleted {} sent outbox events older than {}", deleted, retention);
        }
    }
}