package com.sndbx.product.service;

import com.sndbx.product.config.CacheConfig;
import com.sndbx.product.dto.response.ProductResponse;
import com.sndbx.product.exception.ProductNotFoundException;
import com.sndbx.product.mapper.ProductMapper;
import com.sndbx.product.model.Product;
import com.sndbx.product.model.Variant;
import com.sndbx.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductCardService {

    private final ProductRepository repository;
    private final ProductMapper mapper;

    @Cacheable(cacheNames = CacheConfig.PRODUCT_CARD, key = "#id")
    public ProductResponse publicCard(String id) {
        Product product = repository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        List<Variant> visibleVariants = product.getVariants().stream()
                .filter(Variant::active)
                .toList();
        if (!product.isActive() || visibleVariants.isEmpty()) {
            throw new ProductNotFoundException(id);
        }
        product.setVariants(visibleVariants);
        return mapper.toResponse(product);
    }
}