package com.sndbx.order.mapper;

import com.sndbx.order.dto.payload.OrderEventPayload;
import com.sndbx.order.events.OrderEvent;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EventAvroMapper {

    OrderEvent toAvro(OrderEventPayload payload);
}
