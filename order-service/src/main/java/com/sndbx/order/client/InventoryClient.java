package com.sndbx.order.client;

import com.sndbx.order.dto.client.ReservationResponse;
import com.sndbx.order.dto.client.ReserveRequest;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange("/internal/reservations")
public interface InventoryClient {

    @PostExchange
    ReservationResponse reserve(@RequestBody ReserveRequest request);

    @PostExchange("/{orderId}/commit")
    ReservationResponse commit(@PathVariable String orderId);

    @PostExchange("/{orderId}/release")
    ReservationResponse release(@PathVariable String orderId);
}