package com.sndbx.inventory.service;

import com.sndbx.inventory.entity.StockItem;
import com.sndbx.inventory.entity.outbox.StockEventType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockAvailability {

    private final OutboxRecorder outboxRecorder;

    public int of(StockItem item) {
        return item.getOnHand() - item.getReserved();
    }

    public void recordChange(StockItem item, int availableBefore) {
        int availableAfter = of(item);
        if (availableBefore <= 0 && availableAfter > 0) {
            outboxRecorder.record(StockEventType.SKU_IN_STOCK, item.getSku());
        } else if (availableBefore > 0 && availableAfter <= 0) {
            outboxRecorder.record(StockEventType.SKU_OUT_OF_STOCK, item.getSku());
        }
    }
}