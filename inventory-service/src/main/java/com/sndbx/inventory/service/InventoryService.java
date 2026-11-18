package com.sndbx.inventory.service;

import com.sndbx.inventory.dto.InventoryRejectedDto;
import com.sndbx.inventory.dto.InventoryReservedDto;
import com.sndbx.inventory.entity.OutboxEvent;
import com.sndbx.inventory.entity.ProcessedOrderId;
import com.sndbx.inventory.repository.InventoryRepository;
import com.sndbx.inventory.repository.OutboxEventRepository;
import com.sndbx.inventory.repository.ProcessedOrderIdRepository;
import com.sndbx.order.event.OrderPlacedEvent;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

@Service
@Slf4j
public class InventoryService {
    private static final Logger logger = LoggerFactory.getLogger(InventoryService.class);
    private final InventoryRepository inventoryRepository;
    private final ProcessedOrderIdRepository processedOrderIdRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final TransactionTemplate transactionTemplate;
    private final ObjectMapper objectMapper;

    public InventoryService(InventoryRepository inventoryRepository, ProcessedOrderIdRepository processedOrderIdRepository, OutboxEventRepository outboxEventRepository, TransactionTemplate transactionTemplate, ObjectMapper objectMapper) {
        this.inventoryRepository = inventoryRepository;
        this.processedOrderIdRepository = processedOrderIdRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.transactionTemplate = transactionTemplate;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "order-placed")
    public void reserveInventory(OrderPlacedEvent orderPlacedEvent) {
        log.info("Inventory service, incoming event from order-placed topic: {}", orderPlacedEvent);
        transactionTemplate.executeWithoutResult(status -> processOrderInTransaction(orderPlacedEvent));
    }

    private void processOrderInTransaction(OrderPlacedEvent orderPlacedEvent) {
        UUID orderId = orderPlacedEvent.getOrderId();
        String email = orderPlacedEvent.getEmail();
        String productId = orderPlacedEvent.getProductId();
        int quantity = orderPlacedEvent.getQuantity();

        try {
            processedOrderIdRepository.save(new ProcessedOrderId(orderId));
        } catch (DataIntegrityViolationException e) {
            logger.info("Order {} already processed", orderId);
            return;
        }

        int count = inventoryRepository.deductStock(productId, quantity);
        String topic = (count > 0) ? "inventory-reserved" : "inventory-rejected";
        Object dto = (count > 0) ? new InventoryReservedDto(orderId, email) : new InventoryRejectedDto(orderId, email);
        String json;

        try {
            json = objectMapper.writeValueAsString(dto);
        } catch (Exception e) {
            throw new RuntimeException("Serialization failed for order: " + orderId, e);
        }

        OutboxEvent outboxEvent = new OutboxEvent(orderId, topic, json);

        outboxEventRepository.save(outboxEvent);
    }
}