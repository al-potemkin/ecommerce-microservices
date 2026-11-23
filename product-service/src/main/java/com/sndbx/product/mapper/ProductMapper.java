package com.sndbx.product.mapper;

import com.sndbx.product.dto.request.CreateProductRequest;
import com.sndbx.product.dto.request.UpdateProductRequest;
import com.sndbx.product.dto.request.VariantRequest;
import com.sndbx.product.dto.response.ProductResponse;
import com.sndbx.product.dto.response.VariantResponse;
import com.sndbx.product.model.Product;
import com.sndbx.product.model.Variant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    Product toEntity(CreateProductRequest request);

    @Mapping(target = "active", source = "active", defaultValue = "true")
    @Mapping(target = "inStock", constant = "false")
    Variant toVariant(VariantRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "variants", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateGeneral(UpdateProductRequest request, @MappingTarget Product product);

    ProductResponse toResponse(Product product);

    VariantResponse toResponse(Variant variant);
}