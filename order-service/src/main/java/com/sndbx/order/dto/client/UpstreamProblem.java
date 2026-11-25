package com.sndbx.order.dto.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UpstreamProblem(String code, String detail, List<Field> errors) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Field(String field, String message) {
    }
}