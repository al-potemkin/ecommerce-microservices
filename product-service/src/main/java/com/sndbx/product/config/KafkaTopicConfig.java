package com.sndbx.product.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {
    public static final String PRODUCT_EVENTS = "product-events";
    private static final String PRODUCT_EVENTS_DLT = "product-events.DLT";

    @Bean
    NewTopic productEvents() {
        return TopicBuilder.name(PRODUCT_EVENTS)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic productEventsDltTopic() {
        return TopicBuilder.name(PRODUCT_EVENTS_DLT)
                .partitions(1)
                .replicas(1)
                .build();
    }
}