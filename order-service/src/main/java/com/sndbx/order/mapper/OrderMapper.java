package com.sndbx.order.mapper;

import com.sndbx.order.dto.client.VariantInfo;
import com.sndbx.order.dto.request.DeliveryRequest;
import com.sndbx.order.dto.response.DeliveryResponse;
import com.sndbx.order.dto.response.OrderItemResponse;
import com.sndbx.order.dto.response.OrderResponse;
import com.sndbx.order.dto.response.OrderSummaryResponse;
import com.sndbx.order.entity.Order;
import com.sndbx.order.entity.OrderItem;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "delivery", source = "order")
    OrderResponse toResponse(Order order);

    @Mapping(target = "city", source = "deliveryCity")
    @Mapping(target = "address", source = "deliveryAddress")
    DeliveryResponse toDelivery(Order order);

    @Mapping(target = "lineTotal",
            expression = "java(item.getUnitPrice().multiply(java.math.BigDecimal.valueOf(item.getQuantity())))")
    OrderItemResponse toItemResponse(OrderItem item);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "recipientName", source = "recipientName")
    @Mapping(target = "recipientPhone", source = "recipientPhone")
    @Mapping(target = "deliveryCity", source = "city")
    @Mapping(target = "deliveryAddress", source = "address")
    void applyDelivery(DeliveryRequest delivery, @MappingTarget Order order);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "sku", source = "variant.sku")
    @Mapping(target = "productId", source = "variant.productId")
    @Mapping(target = "productName", source = "variant.name")
    @Mapping(target = "volumeMl", source = "variant.volumeMl")
    @Mapping(target = "unitPrice", source = "variant.price")
    @Mapping(target = "quantity", source = "quantity")
    OrderItem toItem(VariantInfo variant, int quantity);

    OrderSummaryResponse toSummary(Order order);
}