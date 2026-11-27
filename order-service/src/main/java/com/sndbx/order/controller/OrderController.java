package com.sndbx.order.controller;

import com.sndbx.order.controller.openapi.OrderApi;
import com.sndbx.order.dto.request.CreateOrderRequest;
import com.sndbx.order.dto.response.OrderResponse;
import com.sndbx.order.dto.response.OrderSummaryResponse;
import com.sndbx.order.dto.response.PageResponse;
import com.sndbx.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController implements OrderApi {

    private final OrderService orderService;

    @Override
    public ResponseEntity<OrderResponse> create(Jwt jwt, String idempotencyKey, CreateOrderRequest request) {
        OrderResponse created = orderService.create(UUID.fromString(jwt.getSubject()), idempotencyKey, request);
        return ResponseEntity
                .created(URI.create("/api/products/" + created.id()))
                .body(created);
    }

    @Override
    public PageResponse<OrderSummaryResponse> list(Jwt jwt, int page, int size) {
        return orderService.list(UUID.fromString(jwt.getSubject()), page, size);
    }

    @Override
    public OrderResponse get(Jwt jwt, UUID id) {
        return orderService.get(UUID.fromString(jwt.getSubject()), id);
    }

    @Override
    public OrderResponse cancel(Jwt jwt, UUID id) {
        return orderService.cancel(UUID.fromString(jwt.getSubject()), id);
    }

    @Override
    public OrderResponse requestReturn(Jwt jwt, UUID id) {
        return orderService.requestReturn(UUID.fromString(jwt.getSubject()), id);
    }
}