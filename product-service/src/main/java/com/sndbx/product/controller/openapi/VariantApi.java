package com.sndbx.product.controller.openapi;

import com.sndbx.product.dto.request.StockStatusRequest;
import com.sndbx.product.dto.response.VariantInfoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Set;

@Tag(name = "Variants  (internal)", description = "Product variant management and stock availability. Internal API: not routed by the gateway, requires the INTERNAL_SERVICE role (client credentials).")
public interface VariantApi {

    @Operation(
            summary = "Get variants by SKUs",
            description = "Returns information about product variants matching the provided SKUs."
    )
    @ApiResponse(responseCode = "200", description = "Variant information successfully retrieved", content = @Content(array = @ArraySchema(schema = @Schema(implementation = VariantInfoResponse.class))))
    @GetMapping
    List<VariantInfoResponse> getVariantsBySkus(@Parameter(description = "Variant SKUs (1 to 100)", array = @ArraySchema(schema = @Schema(type = "string", example = "DIOR-SAUV-100"))) @RequestParam @NotEmpty @Size(max = 100) Set<String> skus);

    @Operation(
            summary = "Update variant stock status",
            description = "Updates the stock availability of a product variant."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = ""),
            @ApiResponse(responseCode = "404", description = "Product not found", content = @Content)
    })
    @PutMapping("/{sku}/stock")
    ResponseEntity<Void> setStock(@Parameter(ref = "#/components/parameters/VariantSku") @PathVariable String sku,
                                  @Valid @RequestBody StockStatusRequest request);
}
