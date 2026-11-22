package com.sndbx.product.controller;

import com.sndbx.product.controller.filter.CatalogFilter;
import com.sndbx.product.dto.request.CreateProductRequest;
import com.sndbx.product.dto.request.UpdateProductRequest;
import com.sndbx.product.dto.request.UpdateVariantRequest;
import com.sndbx.product.dto.request.VariantRequest;
import com.sndbx.product.dto.response.CatalogFacetsResponse;
import com.sndbx.product.dto.response.CatalogItemResponse;
import com.sndbx.product.dto.response.PageResponse;
import com.sndbx.product.dto.response.ProductResponse;
import com.sndbx.product.service.CatalogService;
import com.sndbx.product.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    private final CatalogService catalogService;

    public ProductController(ProductService productService, CatalogService catalogService) {
        this.productService = productService;
        this.catalogService = catalogService;
    }

    @PostMapping
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody CreateProductRequest request) {
        ProductResponse created = productService.create(request);
        return ResponseEntity
                .created(URI.create("/api/products/" + created.id()))
                .body(created);
    }

    @GetMapping("/{productId}")
    public ProductResponse getProductById(@PathVariable String productId, HttpServletRequest request) {
        boolean manager = request.isUserInRole("PRODUCT_MANAGER");
        return productService.getProductById(productId, manager);
    }

    @PutMapping("/{productId}")
    public ProductResponse update(@PathVariable String productId,
                                  @Valid @RequestBody UpdateProductRequest request) {
        return productService.update(productId, request);
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deactivate(@PathVariable String productId) {
        productService.deactivate(productId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{productId}/activate")
    public ProductResponse activate(@PathVariable String productId) {
        return productService.activate(productId);
    }

    @PostMapping("/{productId}/variants")
    public ResponseEntity<ProductResponse> addVariant(@PathVariable String productId,
                                                      @Valid @RequestBody VariantRequest request) {
        ProductResponse updated = productService.addVariant(productId, request);
        return ResponseEntity
                .created(URI.create("/api/products/" + productId))
                .body(updated);
    }

    @PatchMapping("/{productId}/variants/{sku}")
    public ProductResponse updateVariant(@PathVariable String productId,
                                         @PathVariable String sku,
                                         @Valid @RequestBody UpdateVariantRequest request) {
        return productService.updateVariant(productId, sku, request);
    }

    @GetMapping
    public PageResponse<CatalogItemResponse> getProductCatalog(@Valid CatalogFilter filter) {
        return catalogService.findProductsFromCatalog(filter);
    }

    @GetMapping("/filters")
    public CatalogFacetsResponse getFilters(@Valid CatalogFilter filter) {
        return catalogService.facets(filter);
    }
}
