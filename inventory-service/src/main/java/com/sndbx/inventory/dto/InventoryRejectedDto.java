package com.sndbx.inventory.dto;

import java.util.UUID;

public record InventoryRejectedDto(UUID orderId, String email) {
}