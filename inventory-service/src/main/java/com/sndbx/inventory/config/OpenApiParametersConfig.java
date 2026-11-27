package com.sndbx.inventory.config;

import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiParametersConfig {

    @Bean
    public OpenApiCustomizer pathParameters() {
        return openApi -> openApi.getComponents()
                .addParameters("Sku", new Parameter()
                        .name("sku")
                        .in("path")
                        .required(true)
                        .description("Variant SKU")
                        .schema(new StringSchema())
                        .example("DIOR-SAUV-100"))
                .addParameters("OrderId", new Parameter()
                        .name("orderId")
                        .in("path")
                        .required(true)
                        .description("Order ID")
                        .schema(new StringSchema())
                        .example("0aff518c-155c-3084-9e7b-83619b1dbfc2"));
    }
}
