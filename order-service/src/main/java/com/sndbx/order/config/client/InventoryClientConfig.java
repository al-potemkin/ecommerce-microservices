package com.sndbx.order.config.client;

import com.sndbx.order.client.InventoryClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class InventoryClientConfig {

    @Bean
    InventoryClient inventoryClient(
            ServiceClientFactory factory,
            @Value("${app.clients.inventory-service.base-url}") String baseUrl,
            @Value("${app.clients.inventory-service.connect-timeout}") Duration connectTimeout,
            @Value("${app.clients.inventory-service.read-timeout}") Duration readTimeout) {
        return factory.create(InventoryClient.class, "inventory-service",
                baseUrl, connectTimeout, readTimeout, new com.sndbx.order.config.client.InventoryErrorMapper());
    }
}