package com.sndbx.product.service;

import com.sndbx.inventory.events.InventoryEvent;
import com.sndbx.product.config.KafkaTopicConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryEventListener {

    private final ProductService productService;

    @KafkaListener(topics = KafkaTopicConfig.INVENTORY_EVENTS)
    public void onEvent(InventoryEvent event) {
        log.info("Inventory event {} type={} sku={}", event.getEventId(), event.getType(), event.getSku());

        switch (event.getType()) {
            case SKU_IN_STOCK -> productService.setVariantStock(event.getSku(), true);
            case SKU_OUT_OF_STOCK -> productService.setVariantStock(event.getSku(), false);
            case UNKNOWN -> log.warn("Unknown inventory event type, skipped: eventId={}", event.getEventId());
        }
    }
}