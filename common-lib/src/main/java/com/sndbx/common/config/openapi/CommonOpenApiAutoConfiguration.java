package com.sndbx.common.config.openapi;

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
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.List;

@AutoConfiguration
@ConditionalOnClass(OpenAPI.class)
@ConditionalOnProperty(prefix = "springdoc.api-docs", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(OpenApiProperties.class)
public class CommonOpenApiAutoConfiguration {

    public static final String SCHEME = "keycloak";

    @Bean
    @ConditionalOnMissingBean
    public EndpointAccessResolver endpointAccessResolver() {
        return method -> EndpointAccessResolver.Access.SECURED;
    }

    @Bean
    @ConditionalOnMissingBean
    public OpenAPI openAPI(OpenApiProperties props,
                           @Value("${spring.application.name}") String appName) {
        SecurityScheme keycloak = new SecurityScheme()
                .type(SecurityScheme.Type.OAUTH2)
                .flows(new OAuthFlows().authorizationCode(new OAuthFlow()
                        .authorizationUrl(props.authUrl())
                        .tokenUrl(props.tokenUrl())));

        String title = props.title() != null ? props.title() : appName + " API";

        return new OpenAPI()
                .info(new Info().title(title).version("v1"))
                // "/" = origin of the Swagger UI page, so requests go through the gateway
                .servers(List.of(new Server().url("/").description("API Gateway")))
                .components(new Components()
                        .addSecuritySchemes(SCHEME, keycloak)
                        .addSchemas("ApiProblem", problemSchema(appName)))
                .addSecurityItem(new SecurityRequirement().addList(SCHEME));
    }

    @Bean
    public OperationCustomizer errorResponsesCustomizer(OpenApiProperties props,
                                                        EndpointAccessResolver accessResolver) {
        return (operation, handlerMethod) -> {
            ApiResponses responses = operation.getResponses();
            if (responses == null) {
                responses = new ApiResponses();
                operation.setResponses(responses);
            }

            switch (accessResolver.resolve(handlerMethod)) {
                case PUBLIC -> operation.setSecurity(List.of());
                case TOKEN_OPTIONAL -> operation.setSecurity(List.of(
                        new SecurityRequirement(),
                        new SecurityRequirement().addList(SCHEME)));
                case SECURED -> {
                    addError(responses, "401", "Authentication token is missing or invalid");
                    if (props.addForbidden()) {
                        addError(responses, "403", "Insufficient permissions: required role is missing or a different role is needed");
                    }
                    addError(responses, "500", "Internal server error");
                }
            }

            boolean hasParams = operation.getParameters() != null && !operation.getParameters().isEmpty();
            if (operation.getRequestBody() != null || hasParams) {
                addError(responses, "400", "Request validation failed or a parameter has an invalid format (see errors[] for details)");
            }

            addDefaultError(responses);
            return operation;
        };
    }

    private Schema<?> problemSchema(String appName) {
        return new ObjectSchema()
                .description("Unified error format (RFC 9457)")
                .addProperty("title", new StringSchema().example("Not Found"))
                .addProperty("status", new IntegerSchema().example(404))
                .addProperty("detail", new StringSchema().example("Resource not found"))
                .addProperty("instance", new StringSchema().example("/api/resource/123"))
                .addProperty("code", new StringSchema())
                .addProperty("timestamp", new StringSchema().format("date-time"))
                .addProperty("traceId", new StringSchema())
                .addProperty("source", new StringSchema().example(appName))
                .addProperty("errors", new ArraySchema().items(new ObjectSchema()
                        .addProperty("field", new StringSchema())
                        .addProperty("message", new StringSchema())));
    }

    private void addError(ApiResponses responses, String code, String description) {
        if (responses.containsKey(code)) {
            return;
        }
        responses.addApiResponse(code, new ApiResponse().description(description));
    }

    // the only response that carries the error schema
    private void addDefaultError(ApiResponses responses) {
        if (responses.containsKey(ApiResponses.DEFAULT)) {
            return;
        }
        responses.addApiResponse(ApiResponses.DEFAULT, new ApiResponse()
                .description("Error response")
                .content(new Content().addMediaType(
                        "application/json",
                        new MediaType().schema(new Schema<>().$ref("#/components/schemas/ApiProblem")))));
    }
}