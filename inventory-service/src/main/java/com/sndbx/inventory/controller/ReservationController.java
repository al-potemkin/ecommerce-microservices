package com.sndbx.inventory.controller;


import com.sndbx.inventory.dto.request.ReserveRequest;
import com.sndbx.inventory.dto.response.ReservationResponse;
import com.sndbx.inventory.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    public ReservationResponse reserve(@Valid @RequestBody ReserveRequest request) {
        return reservationService.reserve(request);
    }

    @PostMapping("/{orderId}/commit")
    public ReservationResponse commit(@PathVariable String orderId) {
        return reservationService.commit(orderId);
    }

    @PostMapping("/{orderId}/release")
    public ReservationResponse release(@PathVariable String orderId) {
        return reservationService.release(orderId);
    }

    @GetMapping("/{orderId}")
    public ReservationResponse get(@PathVariable String orderId) {
        return reservationService.get(orderId);
    }
}