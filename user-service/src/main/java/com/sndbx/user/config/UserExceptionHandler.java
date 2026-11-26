package com.sndbx.user.config;

import com.sndbx.common.config.ApiProblemFactory;
import com.sndbx.common.exception.GlobalExceptionHandler;
import com.sndbx.common.exception.data.CommonErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class UserExceptionHandler extends GlobalExceptionHandler {

    public UserExceptionHandler(ApiProblemFactory problems) {
        super(problems);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<Object> handleIntegrityViolation(DataIntegrityViolationException ex,
                                                    HttpServletRequest request) {
        return respond(CommonErrorCode.CONFLICT, ex, request);
    }
}