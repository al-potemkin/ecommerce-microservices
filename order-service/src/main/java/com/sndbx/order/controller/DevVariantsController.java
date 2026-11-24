package com.sndbx.order.controller;

import com.sndbx.order.config.client.ProductClient;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/orders/dev")
@RequiredArgsConstructor
public class DevVariantsController {

    private final ProductClient productApi;

    @GetMapping("/variants")
    public List<VariantInfo> variants(@RequestParam Set<String> skus) {
        return productApi.getVariants(skus);
    }

    public record VariantInfo(
            String sku,
            String productId,
            String name,
            int volumeMl,
            BigDecimal price,
            boolean active,
            boolean inStock) {
    }
}