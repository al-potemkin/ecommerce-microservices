package com.sndbx.product.controller;

import com.sndbx.product.controller.openapi.VariantApi;
import com.sndbx.product.dto.request.StockStatusRequest;
import com.sndbx.product.dto.response.VariantInfoResponse;
import com.sndbx.product.service.ProductService;
import com.sndbx.product.service.VariantLookupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/variants")
public class VariantController implements VariantApi {

    private final VariantLookupService lookupService;
    private final ProductService productService;

    public List<VariantInfoResponse> getVariantsBySkus(Set<String> skus) {
        return lookupService.findVariantsBySkus(skus);
    }

    public ResponseEntity<Void> setStock(String sku, StockStatusRequest request) {
        productService.setVariantStock(sku, request.inStock());
        return ResponseEntity.noContent().build();
    }
}