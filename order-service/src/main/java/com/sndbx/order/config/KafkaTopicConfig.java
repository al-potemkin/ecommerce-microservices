package com.sndbx.order.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {
    public static final String ORDER_EVENTS = "order-events";
    public static final String ORDER_EVENTS_DLT = "order-events.DLT";
    public static final String PAYMENT_EVENTS = "payment-events";
    private static final String PAYMENT_EVENTS_DLT = "payment-events.DLT";

    @Bean
    NewTopic orderEventsTopic() {
        return TopicBuilder.name(ORDER_EVENTS)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic orderEventsDltTopic() {
        return TopicBuilder.name(ORDER_EVENTS_DLT)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic paymentEventsTopic() {
        return TopicBuilder.name(PAYMENT_EVENTS)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic paymentEventsDltTopic() {
        return TopicBuilder.name(PAYMENT_EVENTS_DLT)
                .partitions(1)
                .replicas(1)
                .build();
    }
}
