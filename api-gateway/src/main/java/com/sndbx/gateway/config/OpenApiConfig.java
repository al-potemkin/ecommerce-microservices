package com.sndbx.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;

@Configuration
@Profile("dev")
public class OpenApiConfig {
    private static final String OPENAPI_PATH = "/aggregate/%s/v3/api-docs";
    private static final String V3_API_DOCS = "/v3/api-docs";

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    SecurityWebFilterChain openApiSecurityFilterChain(ServerHttpSecurity http) {
        return http
                .securityMatcher(ServerWebExchangeMatchers.pathMatchers(
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/webjars/**",
                        "/v3/api-docs/**",
                        "/aggregate/**"
                ))
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(ex -> ex.anyExchange().permitAll())
                .build();
    }

    @Bean
    RouteLocator openApiRouteLocator(RouteLocatorBuilder builder,
                                     @Value("${PRODUCT_SERVICE_URL}") String productUri,
                                     @Value("${ORDER_SERVICE_URL}") String orderUri,
                                     @Value("${INVENTORY_SERVICE_URL}") String inventoryUri) {
        return builder.routes()
                .route("product-openapi", r -> r.path(OPENAPI_PATH.formatted("product-service"))
                        .filters(f -> f.rewritePath(OPENAPI_PATH.formatted("product-service"), V3_API_DOCS))
                        .uri(productUri))
                .route("inventory-openapi", r -> r.path(OPENAPI_PATH.formatted("inventory-service"))
                        .filters(f -> f.rewritePath(OPENAPI_PATH.formatted("inventory-service"), V3_API_DOCS))
                        .uri(inventoryUri))
                .route("order-openapi", r -> r.path(OPENAPI_PATH.formatted("order-service"))
                        .filters(f -> f.rewritePath(OPENAPI_PATH.formatted("order-service"), V3_API_DOCS))
                        .uri(orderUri))
                .build();
    }
}