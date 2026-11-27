package com.sndbx.order.config.openapi;

import com.sndbx.common.config.openapi.EndpointAccessResolver;
import io.swagger.v3.oas.models.media.UUIDSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiServiceConfig {

    @Bean
    public OpenApiCustomizer pathParameters() {
        return openApi -> openApi.getComponents()
                .addParameters("OrderId", new Parameter()
                        .name("id")
                        .in("path")
                        .required(true)
                        .description("Order ID")
                        .schema(new UUIDSchema())
                        .example("3fa85f64-5717-4562-b3fc-2c963f66afa6"));
    }

    @Bean
    public EndpointAccessResolver endpointAccessResolver() {
        return method -> method.hasMethodAnnotation(PublicEndpoint.class)
                ? EndpointAccessResolver.Access.PUBLIC
                : EndpointAccessResolver.Access.SECURED;
    }
}