package com.sndbx.order.config.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.http.client.HttpClientSettings;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
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

    public <T> T create(Class<T> api, String dependency, String serviceUrl,
                        Duration connectTimeout,
                        Duration readTimeout) {
        return create(api, dependency, serviceUrl, connectTimeout, readTimeout,
                new DefaultUpstreamErrorMapper());
    }

    public <T> T create(Class<T> api, String dependency, String serviceUrl,
                        Duration connectTimeout,
                        Duration readTimeout,
                        UpstreamErrorMapper errorMapper) {
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder()
                        .version(HttpClient.Version.HTTP_1_1)
                        .connectTimeout(connectTimeout)
                        .build());
        requestFactory.setReadTimeout(readTimeout);

        HttpClientSettings.defaults();

        RestClient restClient = baseBuilder.clone()
                .baseUrl(serviceUrl)
                .requestFactory(requestFactory)
                .requestInterceptor(authInterceptor)
                .build();

        T target = HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build()
                .createClient(api);

        return translateErrors(api, target, dependency, errorMapper);
    }

    private <T> T translateErrors(Class<T> api, T target, String dependency,
                                  UpstreamErrorMapper errorMapper) {
        return api.cast(Proxy.newProxyInstance(
                api.getClassLoader(),
                new Class<?>[]{api},
                (proxy, method, args) -> {
                    try {
                        return method.invoke(target, args);
                    } catch (InvocationTargetException e) {
                        if (e.getCause() instanceof RestClientResponseException ex) {
                            throw errorMapper.map(dependency, ex);
                        }
                        throw e.getCause();
                    }
                }));
    }
}