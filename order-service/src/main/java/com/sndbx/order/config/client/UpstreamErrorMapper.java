package com.sndbx.order.config.client;

import org.springframework.web.client.RestClientResponseException;

@FunctionalInterface
public interface UpstreamErrorMapper {

    RuntimeException map(String dependency, RestClientResponseException ex);
}