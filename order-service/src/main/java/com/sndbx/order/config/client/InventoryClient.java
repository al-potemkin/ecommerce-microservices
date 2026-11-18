package com.sndbx.order.config.client;

import com.sndbx.order.exception.ServiceUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;


@HttpExchange("/api/inventory")
public interface InventoryClient {
    Logger log = LoggerFactory.getLogger(InventoryClient.class);

    @GetExchange("/check")
//    @CircuitBreaker(name = "inventoryServiceCircuitBreaker", fallbackMethod = "fallbackMethod")
    boolean isInStock(@RequestParam String productId, @RequestParam Integer quantity);

    default boolean fallbackMethod(Throwable throwable) {
        log.error("Failed to invoke the inventory service. Cause: {}", throwable.getMessage());
        throw new ServiceUnavailableException("Service is temporarily unavailable");
    }
}
