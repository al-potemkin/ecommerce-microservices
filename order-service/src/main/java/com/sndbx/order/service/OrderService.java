package com.sndbx.order.service;

import com.sndbx.order.config.client.InventoryClient;
import com.sndbx.order.config.client.ProductClient;
import com.sndbx.order.dto.OrderRequest;
import com.sndbx.order.dto.OrderResponse;
import com.sndbx.order.entity.Order;
//import com.sndbx.order.event.OrderCancelledEvent;
//import com.sndbx.order.event.OrderPlacedEvent;
import com.sndbx.order.exception.NotFoundException;
import com.sndbx.order.exception.OutOfStockException;
import com.sndbx.order.mapper.OrderMapper;
//import com.sndbx.order.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class OrderService {
//    private final KafkaTemplate<UUID, OrderPlacedEvent> kafkaTemplate;
//    private final InventoryClient inventoryClient;
//    private final ProductClient productClient;
//    private final OrderRepository orderRepository;
//    private final OrderMapper orderMapper;
//
//    public OrderService(KafkaTemplate<UUID, OrderPlacedEvent> kafkaTemplate, InventoryClient inventoryClient, ProductClient productClient, OrderRepository orderRepository, OrderMapper orderMapper) {
//        this.kafkaTemplate = kafkaTemplate;
//        this.inventoryClient = inventoryClient;
//        this.productClient = productClient;
//        this.orderRepository = orderRepository;
//        this.orderMapper = orderMapper;
//    }
//
//    public UUID placeOrder(OrderRequest orderRequest) {
//        log.info("Received request to place an order: {}", orderRequest);
//
////        if (!inventoryClient.isInStock(orderRequest.productId(), orderRequest.quantity())) {
////            throw new OutOfStockException("Product is out of stock. Product ID: %s".formatted(orderRequest.productId()));
////        }
//
//        UUID orderId = UUID.randomUUID();
////        BigDecimal price = productClient.getProductPriceById(orderRequest.productId())
////                .multiply(BigDecimal.valueOf(orderRequest.quantity()));
////
////        orderRepository.save(orderMapper.toOrder(orderRequest, null, orderId, price));
////
////        OrderPlacedEvent orderPlacedEvent = orderMapper.toOrderPlacedEvent(orderRequest, orderId);
////        log.info("Publishing event to Kafka topic 'order-placed': {}", orderPlacedEvent);
////
////        kafkaTemplate.send("order-placed", orderId, orderPlacedEvent);
//        return orderId;
//    }
//
//    public void deleteOrderById(UUID orderId, String comment, String userId) {
//        Order order = orderRepository.findByOrderIdAndUserId(orderId, userId)
//                .orElseThrow(() -> new NotFoundException("Order not found. Order ID: %s".formatted(orderId)));
//
//        OrderCancelledEvent orderCancelledEvent = OrderCancelledEvent.newBuilder()
//                .setOrderId(orderId)
//                .setEmail(order.getEmail())
//                .setComment(comment)
//                .build();
//
//        orderRepository.markOrderAsDeleted(order.getId());
//
//        kafkaTemplate.send("order-cancelled", orderId, orderCancelledEvent);
//    }
//
//    public List<OrderResponse> getOrdersByUserId(String userId) {
//        List<Order> orders = orderRepository.findByUserId(userId);
//        return orderMapper.toOrderResponse(orders);
//    }
//
//    public OrderResponse getOrderForUser(Long orderId, String userId) {
//        Order order = orderRepository.findByIdAndUserId(orderId, userId)
//                .orElseThrow(() -> new NotFoundException("Order not found"));
//        return orderMapper.toOrderResponse(order);
//    }

}