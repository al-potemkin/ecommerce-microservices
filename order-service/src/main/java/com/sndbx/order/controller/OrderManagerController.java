package com.sndbx.order.controller;

import com.sndbx.order.dto.response.OrderResponse;
import com.sndbx.order.dto.response.OrderSummaryResponse;
import com.sndbx.order.dto.response.PageResponse;
import com.sndbx.order.entity.OrderStatus;
import com.sndbx.order.service.OrderService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/order/manage")
@RequiredArgsConstructor
public class OrderManagerController {

    private final OrderService orderService;

    @GetMapping
    public PageResponse<OrderSummaryResponse> list(@RequestParam(required = false) OrderStatus status,
                                                   @RequestParam(defaultValue = "0") @Min(0) int page,
                                                   @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size) {
        return orderService.listAll(status, page, size);
    }

    @GetMapping("/{id}")
    public OrderResponse get(@PathVariable UUID id) {
        return orderService.getForManager(id);
    }

    @PostMapping("/{id}/ship")
    public OrderResponse ship(@PathVariable UUID id) {
        return orderService.ship(id);
    }

    @PostMapping("/{id}/return/confirm")
    public OrderResponse confirmReturn(@PathVariable UUID id) {
        return orderService.confirmReturn(id);
    }
}
