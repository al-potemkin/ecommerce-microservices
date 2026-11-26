package com.sndbx.product.utility;

import com.sndbx.common.exception.data.ApiProblem;
import com.sndbx.product.exception.VariantConflictException;
import com.sndbx.product.model.Product;
import com.sndbx.product.model.Variant;
import com.sndbx.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class VariantValidator {

    private final ProductRepository repository;

    public void checkNewProduct(List<Variant> variants) {
        List<ApiProblem.FieldError> errors = new ArrayList<>();

        Set<String> skus = new HashSet<>();
        Set<Integer> volumes = new HashSet<>();
        for (int i = 0; i < variants.size(); i++) {
            Variant v = variants.get(i);
            if (!skus.add(v.sku())) {
                errors.add(error("variants[" + i + "].sku",
                        "SKU '" + v.sku() + "' is repeated in the request"));
            }
            if (!volumes.add(v.volumeMl())) {
                errors.add(error("variants[" + i + "].volumeMl",
                        "Volume " + v.volumeMl() + " ml is repeated in the request"));
            }
        }

        Set<String> taken = skusUsedByOtherProducts(variants, null);
        for (int i = 0; i < variants.size(); i++) {
            String sku = variants.get(i).sku();
            if (taken.contains(sku)) {
                errors.add(error("variants[" + i + "].sku",
                        "SKU '" + sku + "' is already used by another product"));
            }
        }

        throwIfAny(errors);
    }

    public void checkNewVariant(Product product, Variant candidate) {
        List<ApiProblem.FieldError> errors = new ArrayList<>();

        for (Variant existing : product.getVariants()) {
            if (existing.sku().equals(candidate.sku())) {
                errors.add(error("sku",
                        "SKU '" + candidate.sku() + "' already exists in this product"));
            }
            if (existing.volumeMl() == candidate.volumeMl()) {
                errors.add(error("volumeMl",
                        "Product already has a variant with volume " + candidate.volumeMl() + " ml"));
            }
        }

        if (skusUsedByOtherProducts(List.of(candidate), product.getId()).contains(candidate.sku())) {
            errors.add(error("sku",
                    "SKU '" + candidate.sku() + "' is already used by another product"));
        }

        throwIfAny(errors);
    }

    private Set<String> skusUsedByOtherProducts(List<Variant> variants, String ownerId) {
        List<String> skus = variants.stream().map(Variant::sku).toList();
        return repository.findByVariantsSkuIn(skus).stream()
                .filter(p -> !p.getId().equals(ownerId))
                .flatMap(p -> p.getVariants().stream())
                .map(Variant::sku)
                .filter(skus::contains)
                .collect(Collectors.toSet());
    }

    private void throwIfAny(List<ApiProblem.FieldError> errors) {
        if (!errors.isEmpty()) {
            throw new VariantConflictException(errors);
        }
    }

    private ApiProblem.FieldError error(String field, String message) {
        return new ApiProblem.FieldError(field, message);
    }
}