package com.sndbx.order.config.client;

import com.sndbx.order.exception.DependencyUnavailableException;
import com.sndbx.order.exception.UpstreamErrorException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.net.http.HttpClient;
import java.time.Duration;

@Slf4j
@Component
public class ServiceClientFactory {

    private final RestClient.Builder baseBuilder;
    private final OAuth2ClientHttpRequestInterceptor authInterceptor;

    public ServiceClientFactory(RestClient.Builder baseBuilder,
                                OAuth2ClientHttpRequestInterceptor authInterceptor) {
        this.baseBuilder = baseBuilder;
        this.authInterceptor = authInterceptor;
    }

    public <T> T create(Class<T> api, String dependency, String baseUrl,
                        Duration connectTimeout, Duration readTimeout) {
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder()
                        .version(HttpClient.Version.HTTP_1_1)
                        .connectTimeout(connectTimeout)
                        .build());
        requestFactory.setReadTimeout(readTimeout);

        RestClient restClient = baseBuilder.clone()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .requestInterceptor(authInterceptor)
                .defaultStatusHandler(HttpStatusCode::is5xxServerError, (request, response) -> {
                    log.warn("{} responded with {} to {} {}", dependency,
                            response.getStatusCode(), request.getMethod(), request.getURI());
                    throw new DependencyUnavailableException(dependency);
                })
                .defaultStatusHandler(HttpStatusCode::is4xxClientError, (request, response) -> {
                    log.error("{} rejected {} {} with {}", dependency,
                            request.getMethod(), request.getURI(), response.getStatusCode());
                    throw new UpstreamErrorException(dependency, response.getStatusCode().value());
                })
                .build();

        return HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build()
                .createClient(api);
    }
}