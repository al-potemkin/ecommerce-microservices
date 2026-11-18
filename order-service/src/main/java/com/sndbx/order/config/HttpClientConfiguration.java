package com.sndbx.order.config;

import com.sndbx.order.config.client.InventoryClient;
import com.sndbx.order.config.client.ProductClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.HttpClientSettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.time.Duration;

@Configuration
public class HttpClientConfiguration {

    @Bean
    InventoryClient inventoryClient(@Value("${services.inventory.url}") String url) {
        RestClient restClient = RestClient.builder()
                .baseUrl(url)
                .requestFactory(requestFactory())
                .build();

        HttpServiceProxyFactory httpServiceProxyFactory = HttpServiceProxyFactory.builderFor(
                RestClientAdapter.create(restClient)
        ).build();
        return httpServiceProxyFactory.createClient(InventoryClient.class);
    }

    @Bean
    ProductClient productClient(@Value("${services.product.url}") String url) {
        RestClient restClient = RestClient.builder()
                .baseUrl(url)
                .requestFactory(requestFactory())
                .build();

        HttpServiceProxyFactory httpServiceProxyFactory = HttpServiceProxyFactory.builderFor(
                RestClientAdapter.create(restClient)
        ).build();
        return httpServiceProxyFactory.createClient(ProductClient.class);
    }

    private ClientHttpRequestFactory requestFactory() {
        HttpClientSettings httpClientSettings = HttpClientSettings.defaults()
                .withConnectTimeout(Duration.ofSeconds(5))
                .withReadTimeout(Duration.ofSeconds(10));
        return ClientHttpRequestFactoryBuilder.detect().build(httpClientSettings);
    }
}
