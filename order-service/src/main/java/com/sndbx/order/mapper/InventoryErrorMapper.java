package com.sndbx.order.config.client;

import com.sndbx.order.dto.client.UpstreamProblem;
import com.sndbx.order.exception.InsufficientStockException;
import com.sndbx.order.exception.ReservationAlreadyCommittedException;
import com.sndbx.order.exception.ReservationReleasedException;
import com.sndbx.order.exception.config.ApiProblem;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;

public class InventoryErrorMapper implements UpstreamErrorMapper {

    private final UpstreamErrorMapper fallback = new DefaultUpstreamErrorMapper();

    @Override
    public RuntimeException map(String dependency, RestClientResponseException ex) {
        UpstreamProblem problem = ex.getStatusCode().is4xxClientError() ? readProblem(ex) : null;
        if (problem == null || problem.code() == null) {
            return fallback.map(dependency, ex);
        }
        return switch (problem.code()) {
            case "INSUFFICIENT_STOCK" -> new InsufficientStockException(
                    "Not enough stock for the requested items", toFieldErrors(problem, "not enough stock"));
            case "STOCK_ITEM_NOT_FOUND" -> new InsufficientStockException(
                    "Some items are not available", toFieldErrors(problem, "not available"));
            case "RESERVATION_RELEASED" -> new ReservationReleasedException(problem.detail());
            case "RESERVATION_ALREADY_COMMITTED" -> new ReservationAlreadyCommittedException(problem.detail());
            default -> fallback.map(dependency, ex);
        };
    }

    private UpstreamProblem readProblem(RestClientResponseException ex) {
        try {
            return ex.getResponseBodyAs(UpstreamProblem.class);
        } catch (Exception e) {
            return null;
        }
    }

    private List<ApiProblem.FieldError> toFieldErrors(UpstreamProblem problem, String message) {
        if (problem.errors() == null) {
            return List.of();
        }
        return problem.errors().stream()
                .map(f -> new ApiProblem.FieldError(f.field(), message))
                .toList();
    }
}