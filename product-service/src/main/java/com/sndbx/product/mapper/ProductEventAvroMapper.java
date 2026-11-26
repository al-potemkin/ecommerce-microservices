package com.sndbx.product.mapper;

import com.sndbx.product.dto.payload.ProductEventPayload;
import com.sndbx.product.events.ProductEvent;
import com.sndbx.product.events.VariantSnapshot;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductEventAvroMapper {
    ProductEvent toAvro(ProductEventPayload payload);

    VariantSnapshot toAvro(ProductEventPayload.VariantSnapshot variant);
}