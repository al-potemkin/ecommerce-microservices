package com.sndbx.order.dto.client;

import java.util.List;

public record ReserveRequest(String orderId, List<ReservationItem> items) {
}