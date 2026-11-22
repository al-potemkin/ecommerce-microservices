package com.sndbx.product.service;

import com.sndbx.product.dto.response.VariantInfoResponse;
import com.sndbx.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class VariantLookupService {

    private final ProductRepository repository;

    public List<VariantInfoResponse> lookup(Set<String> skus) {
        return repository.findByVariantsSkuIn(skus).stream()
                .flatMap(product -> product.getVariants().stream()
                        .filter(variant -> skus.contains(variant.sku()))
                        .map(variant -> new VariantInfoResponse(
                                variant.sku(),
                                product.getId(),
                                product.getName(),
                                variant.volumeMl(),
                                variant.price(),
                                product.isActive() && variant.active(),
                                variant.inStock())))
                .toList();
    }
}