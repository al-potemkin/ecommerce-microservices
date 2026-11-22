package com.sndbx.product.model;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;

public record Variant(
        @Indexed(unique = true) String sku,
        int volumeMl,
        @Field(targetType = FieldType.DECIMAL128) BigDecimal price,
        boolean active,
        boolean inStock) {

    public Variant withPrice(BigDecimal price) {
        return new Variant(sku, volumeMl, price, active, inStock);
    }

    public Variant withActive(boolean active) {
        return new Variant(sku, volumeMl, price, active, inStock);
    }

    public Variant withInStock(boolean inStock) {
        return new Variant(sku, volumeMl, price, active, inStock);
    }
}