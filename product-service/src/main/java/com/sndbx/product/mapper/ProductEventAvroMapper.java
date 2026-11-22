package com.sndbx.product.mapper;

import com.sndbx.product.events.ProductEvent;
import com.sndbx.product.events.VariantSnapshot;
import com.sndbx.product.model.outbox.ProductEventPayload;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductEventAvroMapper {
    ProductEvent toAvro(ProductEventPayload payload);

    VariantSnapshot toAvro(ProductEventPayload.VariantSnapshot variant);
}