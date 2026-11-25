package com.sndbx.order.dto.response;

public record DeliveryResponse(
        String recipientName,
        String recipientPhone,
        String city,
        String address
) {
}