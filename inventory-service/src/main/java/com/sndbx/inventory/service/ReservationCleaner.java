package com.sndbx.inventory.service;

import com.sndbx.inventory.entity.ReservationStatus;
import com.sndbx.inventory.repository.ReservationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Slf4j
@Component
public class ReservationCleaner {

    private static final List<ReservationStatus> FINISHED =
            List.of(ReservationStatus.COMMITTED, ReservationStatus.RELEASED);

    private final ReservationRepository repository;
    private final Duration retention;

    public ReservationCleaner(ReservationRepository repository,
                              @Value("${app.reservation.retention-days:30}") long retentionDays) {
        this.repository = repository;
        this.retention = Duration.ofDays(retentionDays);
    }

    @Scheduled(cron = "${app.reservation.cleanup-cron:0 30 3 * * *}")
    @Transactional
    public void deleteOldFinished() {
        int deleted = repository.deleteFinishedBefore(FINISHED, Instant.now().minus(retention));
        if (deleted > 0) {
            log.info("Deleted {} finished reservations older than {}", deleted, retention);
        }
    }
}