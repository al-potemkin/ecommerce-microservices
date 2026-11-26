package com.sndbx.inventory.controller.openapi;

import com.sndbx.inventory.dto.request.ReserveRequest;
import com.sndbx.inventory.dto.response.ReservationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Reservations (internal)", description = "Stock reservations for orders. Internal API: not routed by the gateway, requires the INTERNAL_SERVICE role (client credentials).")
public interface ReservationApi {

    @Operation(
            summary = "Reserve stock for an order",
            description = "Reserves the requested quantities for the order. A reservation that is not committed expires automatically and is released."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stock successfully reserved", content = @Content(schema = @Schema(implementation = ReservationResponse.class))),
            @ApiResponse(responseCode = "404", description = "Stock items not found for one or more of the requested SKUs", content = @Content),
            @ApiResponse(responseCode = "409", description = "Not enough available stock for one or more SKUs", content = @Content)
    })
    @PostMapping
    ReservationResponse reserve(@Valid @RequestBody ReserveRequest request);


    @Operation(
            summary = "Commit a reservation",
            description = "Confirms the reservation of the order. Called by order-service after successful payment."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reservation successfully committed", content = @Content(schema = @Schema(implementation = ReservationResponse.class))),
            @ApiResponse(responseCode = "404", description = "Reservation not found", content = @Content),
            @ApiResponse(responseCode = "409", description = "Reservation cannot be committed in its current state (for example, it has expired or was released)", content = @Content)
    })
    @PostMapping("/{orderId}/commit")
    ReservationResponse commit(@Parameter(ref = "#/components/parameters/OrderId") @PathVariable String orderId);


    @Operation(
            summary = "Release a reservation",
            description = "Releases the reserved units back to available stock. Called by order-service on payment failure or order cancellation."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reservation successfully released", content = @Content(schema = @Schema(implementation = ReservationResponse.class))),
            @ApiResponse(responseCode = "404", description = "Reservation not found", content = @Content),
            @ApiResponse(responseCode = "409", description = "Reservation cannot be released in its current state", content = @Content)
    })
    @PostMapping("/{orderId}/release")
    ReservationResponse release(@Parameter(ref = "#/components/parameters/OrderId") @PathVariable String orderId);


    @Operation(
            summary = "Get a reservation",
            description = "Returns the reservation of the order."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reservation found", content = @Content(schema = @Schema(implementation = ReservationResponse.class))),
            @ApiResponse(responseCode = "404", description = "Reservation not found", content = @Content)
    })
    @GetMapping("/{orderId}")
    ReservationResponse get(@Parameter(ref = "#/components/parameters/OrderId") @PathVariable String orderId);
}