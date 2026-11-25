package com.sndbx.inventory.mapper;

import com.sndbx.inventory.dto.payload.InventoryEventPayload;
import com.sndbx.inventory.dto.payload.ReservationEventPayload;
import com.sndbx.inventory.events.InventoryEvent;
import com.sndbx.inventory.events.ReservationEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ValueMapping;

@Mapper(componentModel = "spring")
public interface EventAvroMapper {

    @ValueMapping(source = "RESERVATION_EXPIRED", target = "UNKNOWN")
    InventoryEvent toAvro(InventoryEventPayload payload);

    @Mapping(target = "type", constant = "RESERVATION_EXPIRED")
    ReservationEvent toAvro(ReservationEventPayload payload);
}