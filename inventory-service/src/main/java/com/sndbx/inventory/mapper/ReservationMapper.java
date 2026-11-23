package com.sndbx.inventory.mapper;

import com.sndbx.inventory.dto.response.ReservationItemResponse;
import com.sndbx.inventory.dto.response.ReservationResponse;
import com.sndbx.inventory.entity.Reservation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ReservationMapper {

    @Mapping(target = "sku", source = "stockItem.sku")
    ReservationItemResponse toItem(Reservation reservation);

    default ReservationResponse toResponse(String orderId, List<Reservation> reservations) {
        Reservation first = reservations.getFirst();
        return new ReservationResponse(
                orderId,
                first.getStatus(),
                first.getExpiresAt(),
                reservations.stream().map(this::toItem).toList());
    }
}