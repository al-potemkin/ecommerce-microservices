package com.sndbx.product.dto;

public record FacetValue<T>(T value, long count) {
}