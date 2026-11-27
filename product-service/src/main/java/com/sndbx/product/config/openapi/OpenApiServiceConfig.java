package com.sndbx.product.config.openapi;

import com.sndbx.common.config.openapi.EndpointAccessResolver;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiServiceConfig {

    @Bean
    public OpenApiCustomizer pathParameters() {
        return openApi -> openApi.getComponents()
                .addParameters("ProductId", new Parameter()
                        .name("productId")
                        .in("path")
                        .required(true)
                        .description("Product ID")
                        .schema(new StringSchema())
                        .example("6745a1f2c3d4e5f607182930"))
                .addParameters("VariantSku", new Parameter()
                        .name("sku")
                        .in("path")
                        .required(true)
                        .description("Variant SKU")
                        .schema(new StringSchema())
                        .example("DIOR-SAUV-100"));
    }

    @Bean
    public EndpointAccessResolver endpointAccessResolver() {
        return method -> {
            PublicEndpoint pub = method.getMethodAnnotation(PublicEndpoint.class);
            if (pub == null) {
                return EndpointAccessResolver.Access.SECURED;
            }
            return pub.tokenOptional()
                    ? EndpointAccessResolver.Access.TOKEN_OPTIONAL
                    : EndpointAccessResolver.Access.PUBLIC;
        };
    }
}