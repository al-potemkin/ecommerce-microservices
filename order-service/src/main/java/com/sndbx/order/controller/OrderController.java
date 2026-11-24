//package com.sndbx.order.controller;
//
//
//import com.sndbx.order.dto.OrderRequest;
//import com.sndbx.order.dto.OrderResponse;
//import com.sndbx.order.service.OrderService;
//import jakarta.validation.Valid;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.HttpStatus;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.security.oauth2.jwt.Jwt;
//import org.springframework.web.bind.annotation.DeleteMapping;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.ResponseStatus;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.List;
//
//@Slf4j
//@RestController
//@RequestMapping("/api/order")
//public class OrderController {
//    private final OrderService orderService;
//
//    public OrderController(OrderService orderService) {
//        this.orderService = orderService;
//    }
//
//
//    @GetMapping
//    public void test(@AuthenticationPrincipal Jwt jwt) {
//        log.info("Subject: {}", jwt.getSubject());
//    }
//
//    @PostMapping
//    @ResponseStatus(HttpStatus.CREATED)
//    public OrderResponse placeOrder(@Valid @RequestBody OrderRequest orderRequest) {
//        return orderService.placeOrder(orderRequest);
//    }
//
//    @GetMapping("/my")
//    @ResponseStatus(HttpStatus.OK)
//    public List<OrderResponse> getOrders(@AuthenticationPrincipal Jwt jwt) {
//        return orderService.getOrdersByUserId(jwt.getSubject());
//    }
//
//    @GetMapping("/my/{orderId}")
//    @ResponseStatus(HttpStatus.OK)
//    public OrderResponse getOrderById(@PathVariable Long orderId, @AuthenticationPrincipal Jwt jwt) {
//        return orderService.getOrderForUser(orderId, jwt.getSubject());
//    }
//
//    @DeleteMapping("/my/{orderId}")
//    @ResponseStatus(HttpStatus.NO_CONTENT)
//    public void deleteOrderById(@PathVariable Long orderId, @AuthenticationPrincipal Jwt jwt) {
//        orderService.deleteOrderById(orderId, jwt.getSubject());
//    }
//}