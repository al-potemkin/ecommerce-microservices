package com.sndbx.order.config;

import com.sndbx.common.kafka.KafkaNotRetryableExceptions;
import com.sndbx.order.exception.InvalidOrderTransitionException;
import com.sndbx.order.exception.OrderNotFoundException;
import com.sndbx.order.exception.ReservationAlreadyCommittedException;
import com.sndbx.order.exception.ReservationReleasedException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class KafkaRetryConfig {

    @Bean
    KafkaNotRetryableExceptions orderNotRetryable() {
        return () -> List.of(
                OrderNotFoundException.class,
                InvalidOrderTransitionException.class,
                ReservationReleasedException.class,
                ReservationAlreadyCommittedException.class,
                IllegalArgumentException.class
        );
    }
}