package com.sndbx.order.controller.openapi;

import com.sndbx.order.dto.response.OrderResponse;
import com.sndbx.order.dto.response.OrderSummaryResponse;
import com.sndbx.order.dto.response.PageResponse;
import com.sndbx.order.entity.OrderStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@Tag(name = "Order management", description = "Order processing for managers. Restricted to the manager role.")
public interface OrderManagerApi {

    @Operation(
            summary = "List all orders",
            description = "Returns a paginated list of orders of all users, optionally filtered by status."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Orders successfully retrieved", content = @Content(schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions: manager role is required", content = @Content)
    })
    @GetMapping
    PageResponse<OrderSummaryResponse> list(@Parameter(description = "Order status to filter by") @RequestParam(required = false) OrderStatus status,
                                            @Parameter(description = "Page number starting from 0") @RequestParam(defaultValue = "0") @Min(0) int page,
                                            @Parameter(description = "Page size, 1 to 50") @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size);


    @Operation(
            summary = "Get an order",
            description = "Returns the details of any order."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order found", content = @Content(schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions: manager role is required", content = @Content),
            @ApiResponse(responseCode = "404", description = "Order not found", content = @Content)
    })
    @GetMapping("/{id}")
    OrderResponse get(@Parameter(ref = "#/components/parameters/OrderId") @PathVariable UUID id);


    @Operation(
            summary = "Ship an order",
            description = "Marks the order as shipped. Only possible for orders in a status that allows shipping."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order successfully marked as shipped", content = @Content(schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions: manager role is required", content = @Content),
            @ApiResponse(responseCode = "404", description = "Order not found", content = @Content),
            @ApiResponse(responseCode = "409", description = "Order cannot be shipped in its current status", content = @Content)
    })
    @PostMapping("/{id}/ship")
    OrderResponse ship(@Parameter(ref = "#/components/parameters/OrderId") @PathVariable UUID id);


    @Operation(
            summary = "Confirm a return",
            description = "Confirms the return requested by the customer. The returned units are added back to the stock in inventory-service."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Return successfully confirmed", content = @Content(schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions: manager role is required", content = @Content),
            @ApiResponse(responseCode = "404", description = "Order not found", content = @Content),
            @ApiResponse(responseCode = "409", description = "Return cannot be confirmed in the current order status", content = @Content)
    })
    @PostMapping("/{id}/return/confirm")
    OrderResponse confirmReturn(@Parameter(ref = "#/components/parameters/OrderId") @PathVariable UUID id);
}