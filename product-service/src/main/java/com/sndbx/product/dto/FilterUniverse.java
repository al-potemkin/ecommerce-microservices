package com.sndbx.product.dto;

import java.util.List;

public record FilterUniverse(List<String> brands, List<Integer> volumes) {
}