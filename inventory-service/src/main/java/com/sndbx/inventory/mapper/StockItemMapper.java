package com.sndbx.inventory.mapper;

import com.sndbx.inventory.dto.response.PageResponse;
import com.sndbx.inventory.dto.response.StockItemResponse;
import com.sndbx.inventory.entity.StockItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

@Mapper(componentModel = "spring")
public interface StockItemMapper {

    @Mapping(target = "available", expression = "java(stockItem.getOnHand() - stockItem.getReserved())")
    StockItemResponse toResponse(StockItem stockItem);

    @Mapping(target = "page", source = "number")
    PageResponse<StockItemResponse> toPageResponse(Page<StockItem> page);
}