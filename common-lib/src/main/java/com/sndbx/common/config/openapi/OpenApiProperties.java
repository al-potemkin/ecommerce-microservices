package com.sndbx.common.config.openapi;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "app.openapi")
public record OpenApiProperties(
        String authUrl,
        String tokenUrl,
        String title,
        @DefaultValue("false") boolean addForbidden) {
}