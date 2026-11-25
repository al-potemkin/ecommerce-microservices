package com.sndbx.order.entity.outbox;

public enum OrderOutboxType {
    ORDER_CREATED,
    ORDER_PAID,
    ORDER_CANCELLED,
    ORDER_EXPIRED,
    ORDER_SHIPPED,
    ORDER_RETURN_REQUESTED,
    ORDER_RETURNED
}
