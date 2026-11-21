package com.sndbx.product.controller;

import com.sndbx.product.dto.request.CreateProductRequest;
import com.sndbx.product.dto.request.UpdateProductRequest;
import com.sndbx.product.dto.request.UpdateVariantRequest;
import com.sndbx.product.dto.request.VariantRequest;
import com.sndbx.product.dto.response.ProductResponse;
import com.sndbx.product.service.ProductService;
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

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody CreateProductRequest request) {
        ProductResponse created = service.create(request);
        return ResponseEntity
                .created(URI.create("/api/products/" + created.id()))
                .body(created);
    }

    @GetMapping("/{id}")
    public ProductResponse get(@PathVariable String id) {
        return service.get(id);
    }

    @PutMapping("/{id}")
    public ProductResponse update(@PathVariable String id,
                                  @Valid @RequestBody UpdateProductRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable String id) {
        service.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/activate")
    public ProductResponse activate(@PathVariable String id) {
        return service.activate(id);
    }

    @PostMapping("/{id}/variants")
    public ResponseEntity<ProductResponse> addVariant(@PathVariable String id,
                                                      @Valid @RequestBody VariantRequest request) {
        ProductResponse updated = service.addVariant(id, request);
        return ResponseEntity
                .created(URI.create("/api/products/" + id))
                .body(updated);
    }

    @PatchMapping("/{id}/variants/{sku}")
    public ProductResponse updateVariant(@PathVariable String id,
                                         @PathVariable String sku,
                                         @Valid @RequestBody UpdateVariantRequest request) {
        return service.updateVariant(id, sku, request);
    }
}
