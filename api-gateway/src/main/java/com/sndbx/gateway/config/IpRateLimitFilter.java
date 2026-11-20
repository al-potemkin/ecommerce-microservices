package com.sndbx.gateway.config;

import com.sndbx.gateway.config.utility.ErrorCode;
import com.sndbx.gateway.handler.ErrorResponder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.Instant;

/**
 * before Spring Security chain (-100)
 */
@Component
@Order(-200)
public class IpRateLimitFilter implements WebFilter {
    private static final Logger log = LoggerFactory.getLogger(IpRateLimitFilter.class);

    private final ReactiveStringRedisTemplate redis;
    private final ErrorResponder errors;
    private final long limit;
    private final long windowSeconds;

    public IpRateLimitFilter(ReactiveStringRedisTemplate redis,
                             ErrorResponder errors,
                             @Value("${app.rate-limit.ip.requests:200}") long limit,
                             @Value("${app.rate-limit.ip.window-seconds:10}") long windowSeconds) {
        this.redis = redis;
        this.errors = errors;
        this.limit = limit;
        this.windowSeconds = windowSeconds;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        if (exchange.getRequest().getPath().value().startsWith("/actuator/health")) {
            return chain.filter(exchange);
        }

        String key = "rl:ip:" + clientIp(exchange) + ":" + Instant.now().getEpochSecond() / windowSeconds;
        return redis.opsForValue().increment(key)
                .flatMap(count -> count == 1L
                        ? redis.expire(key, Duration.ofSeconds(windowSeconds * 2)).thenReturn(count)
                        : Mono.just(count))
                .onErrorResume(ex -> {
                    log.warn("IP rate limiter unavailable, skipping: {}", ex.toString());
                    return Mono.just(0L);   // fail-open
                })
                .flatMap(count -> {
                    if (count > limit) {
                        exchange.getResponse().getHeaders()
                                .set(HttpHeaders.RETRY_AFTER, String.valueOf(windowSeconds));
                        return errors.write(exchange, ErrorCode.TOO_MANY_REQUESTS);
                    }
                    return chain.filter(exchange);
                });
    }

    private static String clientIp(ServerWebExchange exchange) {
        InetSocketAddress addr = exchange.getRequest().getRemoteAddress();
        return addr != null && addr.getAddress() != null ? addr.getAddress().getHostAddress() : "unknown";
    }
}