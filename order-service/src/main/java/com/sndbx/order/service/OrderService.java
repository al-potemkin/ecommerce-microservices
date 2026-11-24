package com.sndbx.order.service;

import com.sndbx.order.entity.Order;
import com.sndbx.order.entity.OrderStatus;
import com.sndbx.order.exception.InvalidOrderTransitionException;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class OrderService {
    public void transition(Order order, OrderStatus target) {
        OrderStatus current = order.getStatus();
        if (!current.canTransitionTo(target)) {
            throw new InvalidOrderTransitionException(order.getId(), current, target);
        }

        Instant now = Instant.now();
        switch (target) {
            case PAID -> order.setPaidAt(now);
            case SHIPPED -> order.setShippedAt(now);
            case RETURN_REQUESTED -> order.setReturnRequestedAt(now);
            case RETURNED, CANCELLED, EXPIRED -> order.setFinishedAt(now);
            case AWAITING_PAYMENT -> {
            }
        }
        order.setStatus(target);
    }
}