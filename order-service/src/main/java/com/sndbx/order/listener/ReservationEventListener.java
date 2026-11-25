package com.sndbx.order.listener;

import com.sndbx.inventory.events.ReservationEvent;
import com.sndbx.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationEventListener {

    private final OrderService orderService;

    @KafkaListener(topics = "${app.kafka.topics.reservation-events}")
    public void onReservationEvent(ReservationEvent event) {
        switch (event.getType()) {
            case RESERVATION_EXPIRED -> orderService.onReservationExpired(UUID.fromString(event.getOrderId()));
            case UNKNOWN -> log.warn("Skipping reservation event {} with unknown type", event.getEventId());
        }
    }
}
