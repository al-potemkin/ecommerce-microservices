package com.sndbx.product.controller;

import java.util.List;
import java.util.Set;

import com.sndbx.product.dto.request.StockStatusRequest;
import com.sndbx.product.dto.response.VariantInfoResponse;
import com.sndbx.product.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sndbx.product.service.VariantLookupService;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequestMapping("/internal/variants")
@RequiredArgsConstructor
public class VariantController {

    private final VariantLookupService lookupService;
    private final ProductService productService;

    @GetMapping
    public List<VariantInfoResponse> lookup(@RequestParam @NotEmpty @Size(max = 100) Set<String> skus) {
        return lookupService.lookup(skus);
    }

    @PutMapping("/{sku}/stock")
    public ResponseEntity<Void> setStock(@PathVariable String sku,
                                         @Valid @RequestBody StockStatusRequest request) {
        productService.setVariantStock(sku, request.inStock());
        return ResponseEntity.noContent().build();
    }
}