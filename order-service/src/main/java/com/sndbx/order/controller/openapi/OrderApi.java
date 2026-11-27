package com.sndbx.order.controller.openapi;

import com.sndbx.order.dto.request.CreateOrderRequest;
import com.sndbx.order.dto.response.OrderResponse;
import com.sndbx.order.dto.response.OrderSummaryResponse;
import com.sndbx.order.dto.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@Tag(name = "Orders", description = "Orders of the authenticated customer. Available to any authenticated user; every user works only with their own orders.")
public interface OrderApi {

    @Operation(
            summary = "Create an order",
            description = "Creates an order from the given items and delivery data and reserves the stock. Repeating the request with the same Idempotency-Key does not create a duplicate order."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Order created and stock reserved", content = @Content(schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "409", description = "Not enough available stock for one or more SKUs", content = @Content)
    })
    @PostMapping
    ResponseEntity<OrderResponse> create(@Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
                                         @Parameter(description = "Unique key that makes order creation idempotent", ref = "#/components/parameters/OrderId")
                                         @RequestHeader("Idempotency-Key") @NotBlank @Size(max = 64) String idempotencyKey,
                                         @Valid @RequestBody CreateOrderRequest request);


    @Operation(
            summary = "List my orders",
            description = "Returns a paginated list of the orders of the authenticated user."
    )
    @ApiResponse(responseCode = "200", description = "Orders successfully retrieved")
    @GetMapping
    PageResponse<OrderSummaryResponse> list(@Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
                                            @Parameter(description = "Page number starting from 0") @RequestParam(defaultValue = "0") @Min(0) int page,
                                            @Parameter(description = "Page size, 1 to 50") @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size);


    @Operation(
            summary = "Get an order",
            description = "Returns the details of the order of the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order found", content = @Content(schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "404", description = "Order not found", content = @Content)
    })
    @GetMapping("/{id}")
    OrderResponse get(@Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
                      @Parameter(ref = "#/components/parameters/OrderId") @PathVariable UUID id);


    @Operation(
            summary = "Cancel an order",
            description = "Cancels the order and releases the reserved stock. Only possible while the order status allows cancellation."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order successfully cancelled", content = @Content(schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "404", description = "Order not found", content = @Content),
            @ApiResponse(responseCode = "409", description = "Order cannot be cancelled in its current status", content = @Content)
    })
    @PostMapping("/{id}/cancel")
    OrderResponse cancel(@Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
                         @Parameter(ref = "#/components/parameters/OrderId") @PathVariable UUID id);


    @Operation(
            summary = "Request a return",
            description = "Requests a return of the purchased order. Possible only within the return window after purchase; the return is confirmed by a manager."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Return successfully requested", content = @Content(schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "404", description = "Order not found", content = @Content),
            @ApiResponse(responseCode = "409", description = "Return cannot be requested: the order status does not allow it or the return window has passed", content = @Content)
    })
    @PostMapping("/{id}/return")
    OrderResponse requestReturn(@Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
                                @Parameter(ref = "#/components/parameters/OrderId") @PathVariable UUID id);
}
