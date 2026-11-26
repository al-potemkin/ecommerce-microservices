package com.sndbx.common.exception;

import com.sndbx.common.config.ApiProblemFactory;
import com.sndbx.common.exception.data.ApiProblem;
import com.sndbx.common.exception.data.CommonErrorCode;
import com.sndbx.common.exception.data.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;

@Order()
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final ApiProblemFactory problems;

    public GlobalExceptionHandler(ApiProblemFactory problems) {
        this.problems = problems;
    }

    @ExceptionHandler(ServiceException.class)
    ResponseEntity<Object> handleDomain(ServiceException ex, HttpServletRequest request) {
        return respond(ex.getErrorCode(), ex.getMessage(), ex.getErrors(), ex, request, null);
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    ResponseEntity<Object> handleOptimisticLock(OptimisticLockingFailureException ex,
                                                HttpServletRequest request) {
        return respond(CommonErrorCode.CONCURRENT_MODIFICATION, ex, request);
    }

    @ExceptionHandler({DataAccessResourceFailureException.class, CannotCreateTransactionException.class})
    ResponseEntity<Object> handleDbUnavailable(Exception ex, HttpServletRequest request) {
        return respond(CommonErrorCode.SERVICE_UNAVAILABLE, ex, request);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<Object> handleUnexpected(Exception ex, HttpServletRequest request) {
        return respond(CommonErrorCode.INTERNAL_ERROR, ex, request);
    }

    @ExceptionHandler(AuthenticationException.class)
    ResponseEntity<Object> handleAuthentication(AuthenticationException ex, HttpServletRequest request) {
        return respond(CommonErrorCode.UNAUTHORIZED, ex, request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<Object> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        return respond(CommonErrorCode.FORBIDDEN, ex, request);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers, HttpStatusCode statusCode,
                                                                  WebRequest request) {
        List<ApiProblem.FieldError> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> new ApiProblem.FieldError(
                        e.getField(),
                        e.isBindingFailure() ? "Invalid value" : e.getDefaultMessage()
                ))
                .toList();
        return respond(CommonErrorCode.VALIDATION_FAILED, null, errors, ex, servletRequest(request), null);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, @Nullable Object body,
                                                             HttpHeaders headers, HttpStatusCode statusCode,
                                                             WebRequest request) {
        ErrorCode code = CommonErrorCode.fromStatus(statusCode.value());
        return respond(code, null, null, ex, servletRequest(request), headers);
    }

    protected ResponseEntity<Object> respond(ErrorCode code, Exception ex, HttpServletRequest request) {
        return respond(code, null, null, ex, request, null);
    }

    protected ResponseEntity<Object> respond(ErrorCode code,
                                             @Nullable String detail,
                                             @Nullable List<ApiProblem.FieldError> errors,
                                             Exception ex,
                                             HttpServletRequest request,
                                             @Nullable HttpHeaders headers) {
        ApiProblem problem = problems.create(code, detail, request, errors);
        logException(problem.getStatus(), ex, request);
        return ResponseEntity.status(problem.getStatus())
                .headers(headers)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }

    private HttpServletRequest servletRequest(WebRequest request) {
        return ((ServletWebRequest) request).getRequest();
    }

    private void logException(int status, Exception ex, HttpServletRequest request) {
        String target = request.getMethod() + " " + request.getRequestURI();
        if (status >= 500) {
            log.error("{} -> {}: {}", target, status, ex.getMessage(), ex);
        } else {
            log.warn("{} -> {}: {} ({})", target, status, ex.getClass().getSimpleName(), ex.getMessage());
        }
    }
}