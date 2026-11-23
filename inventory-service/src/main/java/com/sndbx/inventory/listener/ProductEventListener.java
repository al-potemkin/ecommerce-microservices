package com.sndbx.inventory.listener;

import com.sndbx.inventory.service.StockService;
import com.sndbx.product.events.ProductEvent;
import com.sndbx.product.events.ProductEventType;
import com.sndbx.product.events.VariantSnapshot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductEventListener {

    private final StockService stockService;

    @KafkaListener(topics = "${app.kafka.topics.product-events}")
    public void onProductEvent(ProductEvent event) {
        if (event.getType() == ProductEventType.UNKNOWN) {
            log.warn("Skipping product event {} with unknown type", event.getEventId()); //todo: dlt
            return;
        }
        List<String> skus = event.getVariants().stream()
                .map(VariantSnapshot::getSku)
                .toList();
        int created = stockService.registerSkus(event.getProductId(), skus);
        log.info("Product event {} ({}) for product {}: {} variants, {} stock items created",
                event.getEventId(), event.getType(), event.getProductId(), skus.size(), created);
    }
}