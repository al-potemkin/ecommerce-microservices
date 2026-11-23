package com.sndbx.inventory.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {
    public static final String INVENTORY_EVENTS = "inventory-events";
    private static final String INVENTORY_EVENTS_DLT = "inventory-events.DLT";

    @Bean
    public NewTopic inventoryEventsTopic() {
        return TopicBuilder.name(INVENTORY_EVENTS)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic inventoryEventsDltTopic() {
        return TopicBuilder.name(INVENTORY_EVENTS_DLT)
                .partitions(1)
                .replicas(1)
                .build();
    }
}
