package com.sndbx.order.controller.openapi;

import com.sndbx.order.config.openapi.PublicEndpoint;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@Tag(name = "Payment Stub", description = "Development-only endpoint that simulates a payment result by publishing a payment event. To be removed when payment-service is implemented.")
public interface PaymentStubApi {

    @PublicEndpoint
    @Operation(
            summary = "Simulate a payment result",
            description = "Publishes a PAYMENT_SUCCEEDED or PAYMENT_FAILED event for the order to the payment-events topic."
    )
    @ApiResponse(responseCode = "202", description = "Payment event published")
    @PostMapping("/{orderId}/payment")
    void simulate(@Parameter(description = "Order ID", ref = "#/components/parameters/OrderId") @PathVariable UUID orderId,
                  @Parameter(description = "true: payment succeeded, false: payment failed", example = "true") @RequestParam boolean success);
}