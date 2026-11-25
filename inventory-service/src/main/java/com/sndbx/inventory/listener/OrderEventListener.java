package com.sndbx.inventory.listener;

import com.sndbx.inventory.service.ReservationService;
import com.sndbx.order.events.OrderEvent;
import com.sndbx.order.events.OrderEventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final ReservationService reservationService;

    @KafkaListener(topics = "${app.kafka.topics.order-events}")
    public void onOrderEvent(OrderEvent event) {
        if (event.getType() != OrderEventType.ORDER_RETURNED) {
            return;
        }
        reservationService.returnToStock(event.getOrderId());
        log.info("Returned stock for order {}", event.getOrderId());
    }
}