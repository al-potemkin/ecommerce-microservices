package com.sndbx.inventory.mapper;

import com.sndbx.inventory.dto.payload.InventoryEventPayload;
import com.sndbx.inventory.events.InventoryEvent;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface InventoryEventAvroMapper {

    InventoryEvent toAvro(InventoryEventPayload payload);
}