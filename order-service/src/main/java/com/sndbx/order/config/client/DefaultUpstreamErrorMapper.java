package com.sndbx.order.config.client;

import com.sndbx.order.exception.DependencyUnavailableException;
import com.sndbx.order.exception.UpstreamErrorException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestClientResponseException;

@Slf4j
public class DefaultUpstreamErrorMapper implements UpstreamErrorMapper {

    @Override
    public RuntimeException map(String dependency, RestClientResponseException ex) {
        if (ex.getStatusCode().is5xxServerError()) {
            log.warn("{} responded with {}", dependency, ex.getStatusCode());
            return new DependencyUnavailableException(dependency);
        }
        log.error("{} rejected the request with {}: {}",
                dependency, ex.getStatusCode(), ex.getResponseBodyAsString());
        return new UpstreamErrorException(dependency, ex.getStatusCode().value());
    }
}