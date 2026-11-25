package com.sndbx.inventory.service;

import com.sndbx.inventory.entity.ReservationStatus;
import com.sndbx.inventory.exception.ReservationConflictException;
import com.sndbx.inventory.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationExpiryJob {

    private static final int BATCH_SIZE = 100;

    private final ReservationRepository repository;
    private final ReservationService reservationService;

    @Scheduled(fixedDelayString = "${app.reservation.expiry-poll-delay-ms:60000}")
    public void releaseExpired() {
        List<String> orderIds = repository.findExpiredOrderIds(
                ReservationStatus.RESERVED, Instant.now(), PageRequest.of(0, BATCH_SIZE));

        for (String orderId : orderIds) {
            try {
                reservationService.expire(orderId);
                log.info("Expired reservation for order {}", orderId);
            } catch (ReservationConflictException | OptimisticLockingFailureException e) {
                log.info("Skipped expired reservation for order {}: {}", orderId, e.getMessage());
            } catch (Exception e) {
                log.warn("Failed to release expired reservation for order {}", orderId, e);
            }
        }
    }
}