package com.sndbx.product.config.openapi;


import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;

@Configuration
@Profile("dev")
public class OpenApiConfig {

    private static final String SCHEME = "keycloak";

    @Bean
    public OpenAPI openAPI(@Value("${app.openapi.auth-url}") String authUrl,
                           @Value("${app.openapi.token-url}") String tokenUrl) {
        SecurityScheme keycloak = new SecurityScheme()
                .type(SecurityScheme.Type.OAUTH2)
                .flows(new OAuthFlows().authorizationCode(new OAuthFlow()
                        .authorizationUrl(authUrl)
                        .tokenUrl(tokenUrl)));

        return new OpenAPI()
                .info(new Info()
                        .title("Product Service API")
                        .version("v1"))
                .servers(List.of(new Server()
                        .url("/")
                        .description("API Gateway")))
                .components(new Components()
                        .addParameters("ProductId",
                                new Parameter()
                                        .name("productId")
                                        .in("path")
                                        .required(true)
                                        .schema(new StringSchema())
                                        .description("Product ID")
                                        .example("6745a1f2c3d4e5f607182930")
                        )
                        .addParameters("VariantSku",
                                new Parameter()
                                        .name("sku")
                                        .in("path")
                                        .required(true)
                                        .schema(new StringSchema())
                                        .description("Variant SKU")
                                        .example("DIOR-SAUV-100"))
                        .addSecuritySchemes(SCHEME, keycloak)
                        .addSchemas("ApiProblem", problemSchema()))
                .addSecurityItem(new SecurityRequirement().addList(SCHEME));
    }

    // Схема описана вручную по вашему формату ошибки, чтобы не зависеть от наследования ProblemDetail
    private Schema<?> problemSchema() {
        return new ObjectSchema()
                .description("Единый формат ошибки")
                .addProperty(
                        "title",
                        new StringSchema().example("Not Found")
                )
                .addProperty(
                        "status",
                        new IntegerSchema().example(404)
                )
                .addProperty(
                        "detail",
                        new StringSchema().example("Product not found")
                )
                .addProperty(
                        "instance",
                        new StringSchema().example("/api/products/123")
                )
                .addProperty(
                        "code",
                        new StringSchema().example("PRODUCT_NOT_FOUND")
                )
                .addProperty(
                        "timestamp",
                        new StringSchema().format("date-time")
                )
                .addProperty(
                        "traceId",
                        new StringSchema()
                )
                .addProperty(
                        "source",
                        new StringSchema().example("product-service")
                )
                .addProperty(
                        "errors",
                        new ArraySchema().items(new ObjectSchema()
                                .addProperty("field", new StringSchema())
                                .addProperty("message", new StringSchema()))
                );
    }

    @Bean
    public OperationCustomizer errorResponsesCustomizer() {
        return (operation, handlerMethod) -> {
            PublicEndpoint pub = handlerMethod.getMethodAnnotation(PublicEndpoint.class);

            ApiResponses responses = operation.getResponses();
            if (responses == null) {
                responses = new ApiResponses();
                operation.setResponses(responses);
            }

            if (pub == null) {
                addError(responses, "401", "Authentication token is missing or invalid");
                addError(responses, "403", "Insufficient permissions: required role is missing or a different role is needed");
                addError(responses, "500", "Internal server error");
            } else if (pub.tokenOptional()) {
                operation.setSecurity(List.of(new SecurityRequirement(), new SecurityRequirement().addList(SCHEME)));
            } else {
                operation.setSecurity(List.of());
            }

            boolean hasQuery = operation.getParameters() != null
                    && operation.getParameters().stream().anyMatch(p -> "query".equals(p.getIn()));
            if (operation.getRequestBody() != null || hasQuery) {
                addError(responses, "400", "Request validation failed (see errors[] for details)");
            }

            addDefaultError(responses);
            return operation;
        };
    }

    private void addError(ApiResponses responses, String code, String description) {
        if (responses.containsKey(code)) {
            return;
        }
        responses.addApiResponse(code, new ApiResponse()
                .description(description));
    }

    // единственный блок со схемой ошибки
    private void addDefaultError(ApiResponses responses) {
        if (responses.containsKey(ApiResponses.DEFAULT)) {
            return;
        }
        responses.addApiResponse(ApiResponses.DEFAULT, new ApiResponse()
                .description("Error response")
                .content(new Content()
                        .addMediaType(
                                "application/json",
                                new MediaType().schema(new Schema<>().$ref("#/components/schemas/ApiProblem"))
                        )));
    }
}