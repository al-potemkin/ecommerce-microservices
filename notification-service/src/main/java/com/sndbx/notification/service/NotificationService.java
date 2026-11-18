package com.sndbx.notification.service;

import com.sndbx.inventory.event.InventoryRejectedEvent;
import com.sndbx.inventory.event.InventoryReservedEvent;
import com.sndbx.notification.entity.ProcessedOrderId;
import com.sndbx.notification.repository.ProcessedOrderIdRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    private final ProcessedOrderIdRepository processedOrderIdRepository;

    public NotificationService(ProcessedOrderIdRepository processedOrderIdRepository) {
        this.processedOrderIdRepository = processedOrderIdRepository;
    }

    @KafkaListener(topics = "inventory-reserved")
    public void sendNotificationIfReserved(InventoryReservedEvent inventoryReservedEvent) {
        UUID orderId = inventoryReservedEvent.getOrderId();
        try {
            processedOrderIdRepository.save(new ProcessedOrderId(
                    orderId
            ));
        } catch (DataIntegrityViolationException e) {
            logger.info("Order {} already processed", orderId);
            return;
        }

        logger.info("Received inventory reserved event: {}", inventoryReservedEvent);
    }

    @KafkaListener(topics = "inventory-rejected")
    public void sendNotificationIfRejected(InventoryRejectedEvent inventoryRejectedEvent) {
        UUID orderId = inventoryRejectedEvent.getOrderId();
        try {
            processedOrderIdRepository.save(new ProcessedOrderId(
                    orderId
            ));
        } catch (DataIntegrityViolationException e) {
            logger.info("Order {} already processed", orderId);
            return;
        }

        logger.info("Received inventory rejected event: {}", inventoryRejectedEvent);
    }
}
