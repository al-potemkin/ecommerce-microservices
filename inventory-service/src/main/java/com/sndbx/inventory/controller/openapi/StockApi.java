package com.sndbx.inventory.controller.openapi;

import com.sndbx.inventory.dto.request.ReceiveStockRequest;
import com.sndbx.inventory.dto.request.SetOnHandRequest;
import com.sndbx.inventory.dto.response.PageResponse;
import com.sndbx.inventory.dto.response.StockItemResponse;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Stock", description = "Stock levels per SKU. Restricted to INVENTORY_MANAGER.")
public interface StockApi {

    @Operation(
            summary = "List stock items",
            description = "Returns a paginated list of stock items, optionally filtered by product ID."
    )
    @ApiResponse(responseCode = "200", description = "Stock items successfully retrieved")
    @GetMapping
    PageResponse<StockItemResponse> list(
            @Parameter(description = "Product ID to filter by", ref = "#/components/parameters/VariantSku") @RequestParam(required = false) String productId,
            @Parameter(description = "Page number starting from 0") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Page size, 1 to 100") @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    );


    @Operation(
            summary = "Get a stock item",
            description = "Returns the stock item for the given SKU."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stock item found", content = @Content(schema = @Schema(implementation = StockItemResponse.class))),
            @ApiResponse(responseCode = "404", description = "Stock item not found", content = @Content)
    })
    @GetMapping("/{sku}")
    StockItemResponse get(@Parameter(ref = "#/components/parameters/VariantSku") @PathVariable String sku);


    @Operation(
            summary = "Receive stock",
            description = "Adds the received quantity to the on-hand stock of the SKU."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stock successfully received", content = @Content(schema = @Schema(implementation = StockItemResponse.class))),
            @ApiResponse(responseCode = "404", description = "Stock item not found", content = @Content)
    })
    @PostMapping("/{sku}/receipts")
    StockItemResponse receive(@Parameter(ref = "#/components/parameters/VariantSku") @PathVariable String sku,
                              @Valid @RequestBody ReceiveStockRequest request);


    @Operation(
            summary = "Set on-hand quantity",
            description = "Replaces the on-hand quantity of the SKU, for example after a physical stock count."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "On-hand quantity successfully updated", content = @Content(schema = @Schema(implementation = StockItemResponse.class))),
            @ApiResponse(responseCode = "404", description = "Stock item not found", content = @Content),
            @ApiResponse(responseCode = "409", description = "New on-hand quantity is lower than the number of reserved units", content = @Content)
    })
    @PutMapping("/{sku}/on-hand")
    StockItemResponse setOnHand(@Parameter(ref = "#/components/parameters/VariantSku") @PathVariable String sku,
                                @Valid @RequestBody SetOnHandRequest request);
}