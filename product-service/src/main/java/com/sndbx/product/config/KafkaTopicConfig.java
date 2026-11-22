package com.sndbx.product.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {
    public static final String PRODUCT_EVENTS = "product-events";
    public static final String INVENTORY_EVENTS = "inventory-events";

    @Bean
    NewTopic productEvents() {
        return TopicBuilder.name(PRODUCT_EVENTS)
                .partitions(3)
                .replicas(1)
                .build();
    }

    // TODO: move to inventory-service
    @Bean
    public NewTopic inventoryEventsTopic() {
        return TopicBuilder.name(INVENTORY_EVENTS)
                .partitions(3)
                .replicas(1)
                .build();
    }
}