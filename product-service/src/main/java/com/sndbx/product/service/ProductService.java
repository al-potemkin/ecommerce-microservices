package com.sndbx.product.service;

import com.sndbx.product.dto.request.CreateProductRequest;
import com.sndbx.product.dto.request.UpdateProductRequest;
import com.sndbx.product.dto.request.UpdateVariantRequest;
import com.sndbx.product.dto.request.VariantRequest;
import com.sndbx.product.dto.response.ProductResponse;
import com.sndbx.product.exception.EmptyUpdateException;
import com.sndbx.product.exception.ProductNotFoundException;
import com.sndbx.product.exception.VariantNotFoundException;
import com.sndbx.product.mapper.ProductMapper;
import com.sndbx.product.model.Product;
import com.sndbx.product.model.Variant;
import com.sndbx.product.repository.ProductRepository;
import com.sndbx.product.utility.VariantValidator;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService {

    private final VariantValidator variantValidator;
    private final ProductRepository repository;
    private final ProductMapper mapper;

    public ProductService(VariantValidator variantValidator, ProductRepository repository, ProductMapper mapper) {
        this.variantValidator = variantValidator;
        this.repository = repository;
        this.mapper = mapper;
    }

    public ProductResponse create(CreateProductRequest request) {
        Product product = mapper.toEntity(request);
        variantValidator.checkNewProduct(product.getVariants());
        return mapper.toResponse(repository.save(product));
    }

    public ProductResponse get(String id, boolean manager) {
        Product product = load(id);
        if (manager) {
            return mapper.toResponse(product);
        }

        List<Variant> visibleVariants = product.getVariants().stream()
                .filter(Variant::active)
                .toList();
        if (!product.isActive() || visibleVariants.isEmpty()) {
            throw new ProductNotFoundException(id);
        }
        product.setVariants(visibleVariants);
        return mapper.toResponse(product);
    }

    public ProductResponse update(String id, UpdateProductRequest request) {
        Product product = load(id);
        mapper.updateGeneral(request, product);
        return mapper.toResponse(repository.save(product));
    }

    public void deactivate(String id) {
        setActive(id, false);
    }

    public ProductResponse activate(String id) {
        return setActive(id, true);
    }

    public ProductResponse addVariant(String id, VariantRequest request) {
        Product product = load(id);
        Variant variant = mapper.toVariant(request);
        variantValidator.checkNewVariant(product, variant);

        List<Variant> all = new ArrayList<>(product.getVariants());
        all.add(variant);
        product.setVariants(all);
        return mapper.toResponse(repository.save(product));
    }

    public ProductResponse updateVariant(String id, String sku, UpdateVariantRequest request) {
        if (request.price() == null && request.active() == null) {
            throw new EmptyUpdateException();
        }
        Product product = load(id);

        List<Variant> variants = new ArrayList<>(product.getVariants());
        int index = indexOfSku(variants, sku);
        if (index < 0) {
            throw new VariantNotFoundException(id, sku);
        }
        variants.set(index, changed(variants.get(index), request));

        product.setVariants(variants);
        return mapper.toResponse(repository.save(product));
    }

    private Product load(String id) {
        return repository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));
    }

    private ProductResponse setActive(String id, boolean active) {
        Product product = load(id);
        product.setActive(active);
        return mapper.toResponse(repository.save(product));
    }

    private Variant changed(Variant current, UpdateVariantRequest request) {
        BigDecimal price = request.price() != null ? request.price() : current.price();
        boolean active = request.active() != null ? request.active() : current.active();
        return new Variant(current.sku(), current.volumeMl(), price, active);
    }

    private int indexOfSku(List<Variant> variants, String sku) {
        for (int i = 0; i < variants.size(); i++) {
            if (variants.get(i).sku().equals(sku)) {
                return i;
            }
        }
        return -1;
    }
}