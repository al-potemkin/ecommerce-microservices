package com.sndbx.order.mapper;

import com.sndbx.order.dto.OrderRequest;
import com.sndbx.order.dto.OrderResponse;
import com.sndbx.order.entity.Order;
//import com.sndbx.order.event.OrderPlacedEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface OrderMapper {
//    @Mapping(target = "id", ignore = true)
//    @Mapping(target = "orderId", expression = "java(UUID.randomUUID())")
//    @Mapping(target = "productId", source = "input.productId")
//    @Mapping(target = "quantity", source = "input.quantity")
//    @Mapping(target = "price", source = "price")
//    @Mapping(target = "email", expression = "java(jwt.getClaimAsString(\"email\"))")
//    @Mapping(target = "firstName", expression = "java(jwt.getClaimAsString(\"given_name\"))")
//    @Mapping(target = "lastName", expression = "java(jwt.getClaimAsString(\"family_name\"))")
//    @Mapping(target = "userId", source = "jwt.subject")
//    Order toOrder(OrderRequest input, Jwt jwt, UUID orderId, BigDecimal price);
//
//    @Mapping(target = "orderId", source = "orderId")
//    OrderPlacedEvent toOrderPlacedEvent(OrderRequest input, UUID orderId);
//
//    OrderResponse toOrderResponse(Order input);
//
//    List<OrderResponse> toOrderResponse(List<Order> input);
}
