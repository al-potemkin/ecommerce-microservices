package com.sndbx.order.config.client;

import com.sndbx.order.exception.ServiceUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.math.BigDecimal;

@HttpExchange("/api/product")
public interface ProductClient {
    Logger log = LoggerFactory.getLogger(ProductClient.class);

    @GetExchange("/price/{id}")
    @CircuitBreaker(name = "productServiceCircuitBreaker", fallbackMethod = "fallbackMethod")
    BigDecimal getProductPriceById(@PathVariable String id);

    default BigDecimal fallbackMethod(Throwable throwable) {
        log.error("Failed to invoke the product service. Cause: {}", throwable.getMessage());
        throw new ServiceUnavailableException("Service is temporarily unavailable");
    }
}
