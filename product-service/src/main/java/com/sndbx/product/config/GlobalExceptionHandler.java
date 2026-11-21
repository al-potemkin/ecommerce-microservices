package com.sndbx.product.config;

import com.sndbx.product.config.utility.ApiProblem;
import com.sndbx.product.config.utility.ErrorCode;
import com.sndbx.product.exception.ProductServiceException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private final ProblemFactory problems;

    @ExceptionHandler(ProductServiceException.class)
    ResponseEntity<Object> handleDomain(ProductServiceException ex, HttpServletRequest request) {
        return respond(problems.create(ex.getErrorCode(), ex.getMessage(), request, ex.getErrors()));
    }

    @ExceptionHandler(DuplicateKeyException.class)
    ResponseEntity<Object> handleDuplicateKey(DuplicateKeyException ex, HttpServletRequest request) {
        return respond(problems.create(ErrorCode.SKU_ALREADY_EXISTS, request));
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    ResponseEntity<Object> handleOptimisticLock(OptimisticLockingFailureException ex,
                                                HttpServletRequest request) {
        return respond(problems.create(ErrorCode.CONCURRENT_MODIFICATION, request));
    }

    @ExceptionHandler(DataAccessResourceFailureException.class)
    ResponseEntity<Object> handleDbUnavailable(DataAccessResourceFailureException ex,
                                               HttpServletRequest request) {
        log.error("Database unavailable on {} {}", request.getMethod(), request.getRequestURI(), ex);
        return respond(problems.create(ErrorCode.SERVICE_UNAVAILABLE, request));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<Object> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception on {} {}", request.getMethod(), request.getRequestURI(), ex);
        return respond(problems.create(ErrorCode.INTERNAL_ERROR, request));
    }

    @ExceptionHandler(AuthenticationException.class)
    ResponseEntity<Object> handleAuthentication(AuthenticationException ex, HttpServletRequest request) {
        return respond(problems.create(ErrorCode.UNAUTHORIZED, request));
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<Object> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        return respond(problems.create(ErrorCode.FORBIDDEN, request));
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        List<ApiProblem.FieldError> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> new ApiProblem.FieldError(e.getField(),
                        e.isBindingFailure() ? "Invalid value" : e.getDefaultMessage()))
                .toList();
        return respond(problems.create(ErrorCode.VALIDATION_FAILED, null, servletRequest(request), errors));
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, @Nullable Object body,
                                                             HttpHeaders headers, HttpStatusCode statusCode,
                                                             WebRequest request) {
        ErrorCode code = ErrorCode.fromStatus(statusCode.value());
        ApiProblem problem = problems.create(code, servletRequest(request));
        return ResponseEntity.status(problem.getStatus())
                .headers(headers)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }

    private ResponseEntity<Object> respond(ApiProblem problem) {
        return ResponseEntity.status(problem.getStatus())
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }

    private HttpServletRequest servletRequest(WebRequest request) {
        return ((ServletWebRequest) request).getRequest();
    }
}
