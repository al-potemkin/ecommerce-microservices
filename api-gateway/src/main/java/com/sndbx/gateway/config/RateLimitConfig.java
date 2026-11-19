package com.sndbx.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;

@Configuration
public class RateLimitConfig {

    @Bean
    KeyResolver userKeyResolver() {
        return exchange -> ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(JwtAuthenticationToken.class::isInstance)
                .map(auth -> "user:" + ((JwtAuthenticationToken) auth).getToken().getSubject())
                .switchIfEmpty(Mono.fromSupplier(() -> {
                    InetSocketAddress addr = exchange.getRequest().getRemoteAddress();
                    return "ip:" + (addr != null ? addr.getAddress().getHostAddress() : "unknown");
                }));
    }
}