package com.sndbx.product.service;

import com.sndbx.product.dto.request.CreateProductRequest;
import com.sndbx.product.dto.request.UpdateVariantRequest;
import com.sndbx.product.dto.response.ProductResponse;
import com.sndbx.product.model.Product;
import com.sndbx.product.model.Variant;
import com.sndbx.product.exception.VariantNotFoundException;
import com.sndbx.product.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    public ProductResponse create(CreateProductRequest request) {
        List<Variant> variants = request.variants().stream().map(this::toVariant).toList();
        validateVariants(variants);
        ensureSkusFree(variants, null);

        Product product = new Product();
        applyGeneral(product, request.name(), request.brand(), request.description(),
                request.gender(), request.scentFamilies(), request.mainImage(), request.gallery());
        product.setVariants(variants);
        return ProductResponse.from(repository.save(product));
    }

    public ProductResponse get(String id) {
        return ProductResponse.from(load(id));
    }

    public ProductResponse update(String id, UpdateProductRequest request) {
        Product product = load(id);
        applyGeneral(product, request.name(), request.brand(), request.description(),
                request.gender(), request.scentFamilies(), request.mainImage(), request.gallery());
        return ProductResponse.from(repository.save(product));
    }

    public void deactivate(String id) { setActive(id, false); }
    public ProductResponse activate(String id) { return setActive(id, true); }

    public ProductResponse addVariant(String id, VariantRequest request) {
        Product product = load(id);
        Variant variant = toVariant(request);

        List<Variant> all = new ArrayList<>(product.getVariants());
        all.add(variant);
        validateVariants(all);          // нет дублей sku и объёма внутри товара
        ensureSkusFree(List.of(variant), product.getId());

        product.setVariants(all);
        return ProductResponse.from(repository.save(product));
    }

    public ProductResponse updateVariant(String id, String sku, UpdateVariantRequest request) {
        if (request.price() == null && request.active() == null) {
            throw new IllegalArgumentException("Nothing to update");
        }
        Product product = load(id);
        List<Variant> updated = product.getVariants().stream()
                .map(v -> v.sku().equals(sku) ? changed(v, request) : v)
                .toList();
        if (updated.equals(product.getVariants()) && !hasSku(product, sku)) {
            throw new VariantNotFoundException(id, sku);
        }
        product.setVariants(updated);
        return ProductResponse.from(repository.save(product));
    }

    // дубли внутри одного товара
    private void validateVariants(List<Variant> variants) {
        Set<String> skus = new HashSet<>();
        Set<Integer> volumes = new HashSet<>();
        for (Variant v : variants) {
            if (!skus.add(v.sku()))        throw new DuplicateSkuException(v.sku());
            if (!volumes.add(v.volumeMl())) throw new DuplicateVolumeException(v.volumeMl());
        }
    }

    // sku занят в другом товаре (ownerId, чтобы не считать занятым свой же)
    private void ensureSkusFree(List<Variant> variants, String ownerId) {
        for (Variant v : variants) {
            repository.findByVariantsSku(v.sku())
                    .filter(p -> !p.getId().equals(ownerId))
                    .ifPresent(p -> { throw new DuplicateSkuException(v.sku()); });
        }
    }

    private Product load(String id) {
        return repository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));
    }

}
