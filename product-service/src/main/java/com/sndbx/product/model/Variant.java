package com.sndbx.product.model;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;

public record Variant(
        @Indexed(unique = true) String sku,
        @Indexed int volumeMl,
        @Indexed @Field(targetType = FieldType.DECIMAL128) BigDecimal price,
        boolean active) {
}