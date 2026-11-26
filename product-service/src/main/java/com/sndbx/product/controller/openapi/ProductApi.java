package com.sndbx.product.controller.openapi;

import com.sndbx.product.config.openapi.PublicEndpoint;
import com.sndbx.product.controller.filter.CatalogFilter;
import com.sndbx.product.dto.request.CreateProductRequest;
import com.sndbx.product.dto.request.UpdateProductRequest;
import com.sndbx.product.dto.request.UpdateVariantRequest;
import com.sndbx.product.dto.request.VariantRequest;
import com.sndbx.product.dto.response.CatalogFacetsResponse;
import com.sndbx.product.dto.response.CatalogItemResponse;
import com.sndbx.product.dto.response.PageResponse;
import com.sndbx.product.dto.response.ProductResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Products", description = "Perfume catalog. Read access is public; modifications are restricted to PRODUCT_MANAGER.")
public interface ProductApi {

    @Operation(
            summary = "Create a product",
            description = "The product is created as inactive. Product variants (1 to 10) must be provided during creation. New variants have inStock=false by default; availability is managed by inventory-service."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Product created. The resource URL is provided in the Location header.", content = @Content(schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "409", description = "Variants contain duplicate or already used SKU and volume values.", content = @Content)
    })
    @PostMapping
    ResponseEntity<ProductResponse> create(@Valid @RequestBody CreateProductRequest request);


    @PublicEndpoint(tokenOptional = true)
    @Operation(
            summary = "Get product details",
            description = "Authentication token is optional. Returns product details. Inactive products are only accessible to PRODUCT_MANAGER; other users receive 404."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product found", content = @Content(schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "404", description = "Product not found", content = @Content)
    })
    @GetMapping("/{productId}")
    ProductResponse getProductById(@Parameter(ref = "#/components/parameters/ProductId") @PathVariable String productId,
                                   HttpServletRequest request);


    @Operation(
            summary = "Update product details",
            description = "Updates product name, brand, description, gender, scent families, and images. Variants are managed through separate operations."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product details successfully updated", content = @Content(schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "404", description = "Product not found", content = @Content)
    })
    @PutMapping("/{productId}")
    ProductResponse update(@Parameter(ref = "#/components/parameters/ProductId") @PathVariable String productId,
                           @Valid @RequestBody UpdateProductRequest request);


    @Operation(
            summary = "Deactivate a product",
            description = "Removes the product from the storefront and catalog while preserving its data."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Product successfully deactivated"),
            @ApiResponse(responseCode = "404", description = "Product not found", content = @Content)
    })
    @DeleteMapping("/{productId}")
    ResponseEntity<Void> deactivate(@Parameter(ref = "#/components/parameters/ProductId") @PathVariable String productId);


    @Operation(
            summary = "Activate a product",
            description = "Makes the product visible in the catalog to all users. A product cannot be activated without at least one active variant."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product successfully activated", content = @Content(schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "404", description = "Product not found", content = @Content),
            @ApiResponse(responseCode = "409", description = "Product has no active variants and cannot be activated", content = @Content)
    })
    @PostMapping("/{productId}/activate")
    ProductResponse activate(@Parameter(ref = "#/components/parameters/ProductId") @PathVariable String productId);


    @Operation(
            summary = "Add a product variant",
            description = "Adds a new variant (volume) to the product. The SKU cannot be changed after creation."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Product variant successfully added. The resource URL is provided in the Location header.", content = @Content(schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "404", description = "Product not found", content = @Content),
            @ApiResponse(responseCode = "409", description = "Variants contain duplicate or already used SKU and volume values.", content = @Content)
    })
    @PostMapping("/{productId}/variants")
    ResponseEntity<ProductResponse> addVariantToProduct(@Parameter(ref = "#/components/parameters/ProductId") @PathVariable String productId,
                                                        @Valid @RequestBody VariantRequest request);


    @Operation(
            summary = "Update a product variant",
            description = "Updates the price and/or status (active/inactive) of a product variant. If the last active variant is deactivated, the product is automatically deactivated."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product variant successfully updated", content = @Content(schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "404", description = "Product or variant not found", content = @Content)
    })
    @PatchMapping("/{productId}/variants/{sku}")
    ProductResponse updateVariant(@Parameter(ref = "#/components/parameters/ProductId") @PathVariable String productId,
                                  @Parameter(ref = "#/components/parameters/VariantSku") @PathVariable String sku,
                                  @Valid @RequestBody UpdateVariantRequest request);


    @PublicEndpoint
    @Operation(
            summary = "Browse the product catalog",
            description = "Returns active products only. Filtering uses OR logic within each filter and AND logic between different filters. Prices, volumes, and price-based sorting are calculated only from variants matching the applied filters."
    )
    @ApiResponse(responseCode = "200", description = "Product catalog successfully retrieved. Returns a paginated list of active products matching the specified filters and sorting criteria.")
    @GetMapping
    PageResponse<CatalogItemResponse> getProductCatalog(@ParameterObject @Valid CatalogFilter filter);


    @PublicEndpoint
    @Operation(
            summary = "Get catalog filters with counts",
            description = "Returns available values for the catalog filter panel, e.g. 'Dior (12)'. Each filter count is calculated without applying that filter itself."
    )
    @ApiResponse(responseCode = "200", description = "Catalog filter options with product counts successfully retrieved.", content = @Content(schema = @Schema(implementation = CatalogFacetsResponse.class)))
    @GetMapping("/filters")
    CatalogFacetsResponse getFilters(@ParameterObject @Valid CatalogFilter filter);
}
