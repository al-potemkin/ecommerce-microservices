package com.sndbx.order.listener;

import com.sndbx.order.service.OrderService;
import com.sndbx.payment.events.PaymentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private final OrderService orderService;

    @KafkaListener(topics = "${app.kafka.topics.payment-events}")
    public void onPaymentEvent(PaymentEvent event) {
        UUID orderId = UUID.fromString(event.getOrderId());
        switch (event.getType()) {
            case PAYMENT_SUCCEEDED -> orderService.onPaymentSucceeded(orderId);
            case PAYMENT_FAILED -> orderService.onPaymentFailed(orderId);
            case UNKNOWN -> log.warn("Skipping payment event {} with unknown type", event.getEventId());
        }
    }
}