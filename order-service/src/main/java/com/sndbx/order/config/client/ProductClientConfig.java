package com.sndbx.order.config.client;

import com.sndbx.order.config.client.ServiceClientFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class ProductClientConfig {

    @Bean
    ProductClient productServiceApi(
            ServiceClientFactory factory,
            @Value("${app.clients.product-service.base-url}") String baseUrl,
            @Value("${app.clients.product-service.connect-timeout}") Duration connectTimeout,
            @Value("${app.clients.product-service.read-timeout}") Duration readTimeout) {
        return factory.create(ProductClient.class, "product-service",
                baseUrl, connectTimeout, readTimeout);
    }
}