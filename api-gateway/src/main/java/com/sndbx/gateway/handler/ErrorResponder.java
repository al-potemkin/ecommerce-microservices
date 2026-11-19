package com.sndbx.gateway.handler;

import com.sndbx.gateway.config.utility.ApiProblem;
import com.sndbx.gateway.config.utility.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.http.MediaType;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.result.view.ViewResolver;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;
import java.util.Set;

@Component
public class ErrorResponder {

    private final ServerResponse.Context context;
    private final TraceIdResolver traceIds;
    private final String typeBaseUri;
    private final String source;

    public ErrorResponder(ServerCodecConfigurer codecs,
                          TraceIdResolver traceIds,
                          @Value("${app.errors.type-base-uri:https://api.example.com/problems}") String typeBaseUri,
                          @Value("${spring.application.name}") String source) {
        this.context = new ServerResponse.Context() {
            @Override
            public List<HttpMessageWriter<?>> messageWriters() {
                return codecs.getWriters();
            }

            @Override
            public List<ViewResolver> viewResolvers() {
                return List.of();
            }
        };
        this.traceIds = traceIds;
        this.typeBaseUri = typeBaseUri;
        this.source = source;
    }

    public Mono<Void> write(ServerWebExchange exchange, ErrorCode code) {
        return write(exchange, code, code.defaultDetail(), List.of());
    }

    public Mono<Void> write(ServerWebExchange exchange, ErrorCode code,
                            String detail, List<ApiProblem.FieldError> errors) {
        ApiProblem body = new ApiProblem(code, detail, typeBaseUri,
                instance(exchange), traceIds.resolve(exchange), source, errors);

        return ServerResponse.status(code.status())
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .bodyValue(body)
                .flatMap(response -> response.writeTo(exchange, context));
    }

    /**
     * Исходный путь запроса без query-строки (после forward на fallback путь меняется).
     */
    private String instance(ServerWebExchange exchange) {
        Set<URI> originals = exchange.getAttributeOrDefault(
                ServerWebExchangeUtils.GATEWAY_ORIGINAL_REQUEST_URL_ATTR, Set.<URI>of());
        URI uri = originals.isEmpty() ? exchange.getRequest().getURI() : originals.iterator().next();
        String path = uri.getRawPath();
        return (path == null || path.isEmpty()) ? "/" : path;
    }
}