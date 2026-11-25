package com.sndbx.order.controller;

import com.sndbx.payment.events.PaymentEvent;
import com.sndbx.payment.events.PaymentEventType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

// TODO: remove after payment-service impl
//   and `/api/orders/dev/**` in SecurityConfig
@Profile("dev")
@RestController
@RequestMapping("/api/order/dev")
@RequiredArgsConstructor
public class PaymentStubController {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topics.payment-events}")
    private String topic;

    @PostMapping("/{orderId}/payment")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void simulate(@PathVariable UUID orderId, @RequestParam boolean success) {
        PaymentEvent event = PaymentEvent.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setType(success ? PaymentEventType.PAYMENT_SUCCEEDED : PaymentEventType.PAYMENT_FAILED)
                .setOccurredAt(Instant.now())
                .setOrderId(orderId.toString())
                .build();
        kafkaTemplate.send(topic, orderId.toString(), event);
    }
}