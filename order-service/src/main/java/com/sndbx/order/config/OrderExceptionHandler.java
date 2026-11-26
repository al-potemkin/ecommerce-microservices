package com.sndbx.order.config;

import com.sndbx.common.config.ApiProblemFactory;
import com.sndbx.common.exception.GlobalExceptionHandler;
import com.sndbx.common.exception.data.CommonErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;

@RestControllerAdvice
public class OrderExceptionHandler extends GlobalExceptionHandler {

    public OrderExceptionHandler(ApiProblemFactory problems) {
        super(problems);
    }

    @ExceptionHandler({ResourceAccessException.class, OAuth2AuthorizationException.class})
    ResponseEntity<Object> handleDependencyUnavailable(Exception ex, HttpServletRequest request) {
        return respond(CommonErrorCode.SERVICE_UNAVAILABLE, ex, request);
    }
}