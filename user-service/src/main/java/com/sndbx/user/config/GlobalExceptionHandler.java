package com.sndbx.user.config;

import com.sndbx.user.exception.config.ApiProblem;
import com.sndbx.user.exception.config.ErrorCode;
import com.sndbx.user.exception.config.UserServiceException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
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

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private final ApiProblemFactory problems;

    @ExceptionHandler(UserServiceException.class)
    ResponseEntity<Object> handleDomain(UserServiceException ex, HttpServletRequest request) {
        return respond(ex.getErrorCode(), ex.getMessage(), ex.getErrors(), ex, request, null);
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    ResponseEntity<Object> handleOptimisticLock(OptimisticLockingFailureException ex,
                                                HttpServletRequest request) {
        return respond(ErrorCode.CONCURRENT_MODIFICATION, ex, request);
    }

    @ExceptionHandler({DataAccessResourceFailureException.class, CannotCreateTransactionException.class})
    ResponseEntity<Object> handleDbUnavailable(Exception ex, HttpServletRequest request) {
        return respond(ErrorCode.SERVICE_UNAVAILABLE, ex, request);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<Object> handleUnexpected(Exception ex, HttpServletRequest request) {
        return respond(ErrorCode.INTERNAL_ERROR, ex, request);
    }

    @ExceptionHandler(AuthenticationException.class)
    ResponseEntity<Object> handleAuthentication(AuthenticationException ex, HttpServletRequest request) {
        return respond(ErrorCode.UNAUTHORIZED, ex, request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<Object> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        return respond(ErrorCode.FORBIDDEN, ex, request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<Object> handleIntegrityViolation(DataIntegrityViolationException ex,
                                                    HttpServletRequest request) {
        return respond(ErrorCode.CONFLICT, ex, request);
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
        return respond(ErrorCode.VALIDATION_FAILED, null, errors, ex, servletRequest(request), null);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, @Nullable Object body,
                                                             HttpHeaders headers, HttpStatusCode statusCode,
                                                             WebRequest request) {
        ErrorCode code = ErrorCode.fromStatus(statusCode.value());
        return respond(code, null, null, ex, servletRequest(request), headers);
    }

    private ResponseEntity<Object> respond(ErrorCode code, Exception ex, HttpServletRequest request) {
        return respond(code, null, null, ex, request, null);
    }

    private ResponseEntity<Object> respond(ErrorCode code,
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