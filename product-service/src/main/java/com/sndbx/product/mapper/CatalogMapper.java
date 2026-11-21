package com.sndbx.product.mapper;

import com.sndbx.product.dto.response.CatalogItemResponse;
import com.sndbx.product.dto.response.PageResponse;
import com.sndbx.product.model.CatalogItemView;
import com.sndbx.product.model.Variant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CatalogMapper {

    @Mapping(target = "volumes", source = "matchedVariants", qualifiedByName = "volumesOf")
    CatalogItemResponse toItem(CatalogItemView view);

    @Mapping(target = "page", source = "number")
    PageResponse<CatalogItemResponse> toPageResponse(Page<CatalogItemView> page);

    @Named("volumesOf")
    default List<Integer> volumesOf(List<Variant> variants) {
        return variants.stream().map(Variant::volumeMl).sorted().toList();
    }
}