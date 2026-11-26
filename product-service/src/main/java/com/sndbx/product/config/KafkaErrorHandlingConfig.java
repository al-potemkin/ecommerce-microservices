package com.sndbx.product.config;

import com.sndbx.product.exception.VariantNotFoundException;
import com.sndbx.product.exception.config.ProductServiceException;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;

import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class KafkaErrorHandlingConfig {

    private static final String DLT_SUFFIX = ".DLT";

    @Bean
    CommonErrorHandler kafkaErrorHandler(
            KafkaTemplate<String, Object> avroTemplate,
            @Value("${app.kafka.retry.max-retries}") int maxRetries,
            @Value("${app.kafka.retry.initial-interval-ms}") long initialIntervalMs,
            @Value("${app.kafka.retry.multiplier}") double multiplier,
            @Value("${app.kafka.retry.max-interval-ms}") long maxIntervalMs
    ) {
        // Same producer settings, but raw bytes: for messages that could not be deserialized
        KafkaTemplate<String, Object> bytesTemplate = new KafkaTemplate<>(
                avroTemplate.getProducerFactory().copyWithConfigurationOverride(
                        Map.of(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class)));

        // Order matters: the first class the value is assignable to wins
        Map<Class<?>, KafkaOperations<?, ?>> templates = new LinkedHashMap<>();
        templates.put(byte[].class, bytesTemplate);
        templates.put(Object.class, avroTemplate);

        // Partition -1: let the producer choose, so DLT does not need as many partitions as the source topic
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(templates,
                (record, ex) -> new TopicPartition(record.topic() + DLT_SUFFIX, -1));

        ExponentialBackOffWithMaxRetries backOff = new ExponentialBackOffWithMaxRetries(maxRetries);
        backOff.setInitialInterval(initialIntervalMs);
        backOff.setMultiplier(multiplier);
        backOff.setMaxInterval(maxIntervalMs);

        DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, backOff);
        handler.addNotRetryableExceptions(
                DataIntegrityViolationException.class,
                VariantNotFoundException.class,
                ProductServiceException.class);
        return handler;
    }
}