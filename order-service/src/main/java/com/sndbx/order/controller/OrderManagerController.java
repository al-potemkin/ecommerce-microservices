package com.sndbx.order.controller;

import com.sndbx.order.controller.openapi.OrderManagerApi;
import com.sndbx.order.dto.response.OrderResponse;
import com.sndbx.order.dto.response.OrderSummaryResponse;
import com.sndbx.order.dto.response.PageResponse;
import com.sndbx.order.entity.OrderStatus;
import com.sndbx.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/order/manage")
@RequiredArgsConstructor
public class OrderManagerController implements OrderManagerApi {

    private final OrderService orderService;

    @Override
    public PageResponse<OrderSummaryResponse> list(OrderStatus status, int page, int size) {
        return orderService.listAll(status, page, size);
    }

    @Override
    public OrderResponse get(UUID id) {
        return orderService.getForManager(id);
    }

    @Override
    public OrderResponse ship(UUID id) {
        return orderService.ship(id);
    }

    @Override
    public OrderResponse confirmReturn(UUID id) {
        return orderService.confirmReturn(id);
    }
}
