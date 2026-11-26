package com.sndbx.common;

import com.sndbx.common.config.ApiProblemFactory;
import com.sndbx.common.config.KeycloakRealmRoleConverter;
import com.sndbx.common.config.SecurityProblemHandler;
import com.sndbx.common.exception.GlobalExceptionHandler;
import io.micrometer.tracing.Tracer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.web.servlet.HandlerExceptionResolver;

@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class CommonLibAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ApiProblemFactory commonApiProblemFactory(ObjectProvider<Tracer> tracer,
                                                     @Value("${spring.application.name}") String source) {
        return new ApiProblemFactory(tracer, source);
    }

    @Bean
    @ConditionalOnMissingBean
    public GlobalExceptionHandler commonGlobalExceptionHandler(ApiProblemFactory problems) {
        return new GlobalExceptionHandler(problems);
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(BearerTokenAuthenticationEntryPoint.class)
    static class SecurityConfiguration {

        @Bean
        @ConditionalOnMissingBean
        SecurityProblemHandler commonSecurityProblemHandler(
                @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
            return new SecurityProblemHandler(resolver);
        }

        @Bean
        @ConditionalOnMissingBean
        KeycloakRealmRoleConverter commonKeycloakRealmRoleConverter() {
            return new KeycloakRealmRoleConverter();
        }

        @Bean
        @ConditionalOnMissingBean
        JwtAuthenticationConverter commonJwtAuthenticationConverter(KeycloakRealmRoleConverter roleConverter) {
            JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
            converter.setJwtGrantedAuthoritiesConverter(roleConverter);
            return converter;
        }
    }
}