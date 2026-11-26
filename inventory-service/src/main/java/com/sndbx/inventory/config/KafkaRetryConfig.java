package com.sndbx.inventory.config;

import com.sndbx.common.kafka.KafkaNotRetryableExceptions;
import com.sndbx.inventory.exception.ReservationConflictException;
import com.sndbx.inventory.exception.ReservationNotFoundException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class KafkaRetryConfig {

    @Bean
    KafkaNotRetryableExceptions inventoryNotRetryable() {
        return () -> List.of(
                ReservationNotFoundException.class,
                ReservationConflictException.class
        );
    }
}