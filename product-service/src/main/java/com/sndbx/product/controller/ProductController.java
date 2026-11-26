package com.sndbx.product.controller;

import com.sndbx.product.controller.filter.CatalogFilter;
import com.sndbx.product.controller.openapi.ProductApi;
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
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController implements ProductApi {

    private final ProductService productService;
    private final CatalogService catalogService;

    @Override
    public ResponseEntity<ProductResponse> create(CreateProductRequest request) {
        ProductResponse created = productService.create(request);
        return ResponseEntity
                .created(URI.create("/api/products/" + created.id()))
                .body(created);
    }

    @Override
    public ProductResponse getProductById(String productId, HttpServletRequest request) {
        boolean manager = request.isUserInRole("PRODUCT_MANAGER");
        return productService.getProductById(productId, manager);
    }

    @Override
    public ProductResponse update(String productId, UpdateProductRequest request) {
        return productService.update(productId, request);
    }

    @Override
    public ResponseEntity<Void> deactivate(String productId) {
        productService.deactivate(productId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ProductResponse activate(String productId) {
        return productService.activate(productId);
    }

    @Override
    public ResponseEntity<ProductResponse> addVariantToProduct(String productId, VariantRequest request) {
        ProductResponse updated = productService.addVariantToProduct(productId, request);
        return ResponseEntity
                .created(URI.create("/api/products/" + productId))
                .body(updated);
    }

    @Override
    public ProductResponse updateVariant(String productId, String sku, UpdateVariantRequest request) {
        return productService.updateVariant(productId, sku, request);
    }

    @Override
    public PageResponse<CatalogItemResponse> getProductCatalog(CatalogFilter filter) {
        return catalogService.findProductsFromCatalog(filter);
    }

    @Override
    public CatalogFacetsResponse getFilters(CatalogFilter filter) {
        return catalogService.fetchCatalogFilters(filter);
    }
}
