package com.sndbx.inventory.controller;


import com.sndbx.inventory.controller.openapi.ReservationApi;
import com.sndbx.inventory.dto.request.ReserveRequest;
import com.sndbx.inventory.dto.response.ReservationResponse;
import com.sndbx.inventory.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/reservations")
@RequiredArgsConstructor
public class ReservationController implements ReservationApi {

    private final ReservationService reservationService;

    @Override
    public ReservationResponse reserve(ReserveRequest request) {
        return reservationService.reserve(request);
    }

    @Override
    public ReservationResponse commit(String orderId) {
        return reservationService.commit(orderId);
    }

    @Override
    public ReservationResponse release(String orderId) {
        return reservationService.release(orderId);
    }

    @Override
    public ReservationResponse get(String orderId) {
        return reservationService.get(orderId);
    }
}