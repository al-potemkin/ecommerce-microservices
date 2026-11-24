package com.sndbx.order.entity;

import java.util.EnumSet;
import java.util.Set;

public enum OrderStatus {
    AWAITING_PAYMENT,
    PAID,
    SHIPPED,
    RETURN_REQUESTED,
    RETURNED,
    CANCELLED,
    EXPIRED;

    public Set<OrderStatus> allowedTransitions() {
        return switch (this) {
            case AWAITING_PAYMENT -> EnumSet.of(PAID, CANCELLED, EXPIRED);
            case PAID -> EnumSet.of(SHIPPED);
            case SHIPPED -> EnumSet.of(RETURN_REQUESTED);
            case RETURN_REQUESTED -> EnumSet.of(RETURNED);
            case RETURNED, CANCELLED, EXPIRED -> EnumSet.noneOf(OrderStatus.class);
        };
    }

    public boolean canTransitionTo(OrderStatus target) {
        return allowedTransitions().contains(target);
    }
}