package com.sndbx.inventory.dto;

import java.util.UUID;

public record InventoryReservedDto(UUID orderId, String email) {
}