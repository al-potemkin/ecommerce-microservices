package com.sndbx.product.config;

import com.sndbx.common.kafka.KafkaNotRetryableExceptions;
import com.sndbx.product.exception.VariantNotFoundException;
import com.sndbx.product.exception.config.ProductServiceException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class KafkaRetryConfig {

    @Bean
    KafkaNotRetryableExceptions productNotRetryable() {
        return () -> List.of(
                VariantNotFoundException.class,
                ProductServiceException.class
        );
    }
}