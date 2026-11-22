package com.sndbx.product.service;

import com.sndbx.product.config.CacheConfig;
import com.sndbx.product.dto.request.CreateProductRequest;
import com.sndbx.product.dto.request.UpdateProductRequest;
import com.sndbx.product.dto.request.UpdateVariantRequest;
import com.sndbx.product.dto.request.VariantRequest;
import com.sndbx.product.dto.response.ProductResponse;
import com.sndbx.product.exception.EmptyUpdateException;
import com.sndbx.product.exception.NoActiveVariantsException;
import com.sndbx.product.exception.ProductNotFoundException;
import com.sndbx.product.exception.VariantNotFoundException;
import com.sndbx.product.mapper.ProductMapper;
import com.sndbx.product.model.Product;
import com.sndbx.product.model.Variant;
import com.sndbx.product.model.outbox.ProductEventType;
import com.sndbx.product.repository.ProductRepository;
import com.sndbx.product.utility.VariantValidator;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class ProductService {

    private final ProductCardService productCardService;
    private final VariantValidator variantValidator;
    private final ProductRepository repository;
    private final OutboxRecorder outbox;
    private final ProductMapper mapper;

    public ProductService(ProductCardService productCardService, VariantValidator variantValidator, ProductRepository repository, OutboxRecorder outbox, ProductMapper mapper) {
        this.productCardService = productCardService;
        this.variantValidator = variantValidator;
        this.repository = repository;
        this.outbox = outbox;
        this.mapper = mapper;
    }

    @Transactional
    public ProductResponse create(CreateProductRequest request) {
        Product product = mapper.toEntity(request);
        variantValidator.checkNewProduct(product.getVariants());
        Product saved = repository.save(product);
        outbox.record(ProductEventType.PRODUCT_CREATED, saved, saved.getVariants());
        return mapper.toResponse(saved);
    }

    public ProductResponse getProductById(String id, boolean manager) {
        return manager
                ? mapper.toResponse(load(id))
                : productCardService.publicCard(id);
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = CacheConfig.PRODUCT_CARD, key = "#id"),
            @CacheEvict(cacheNames = CacheConfig.FILTER_UNIVERSE, allEntries = true)
    })
    public ProductResponse update(String id, UpdateProductRequest request) {
        Product product = load(id);
        mapper.updateGeneral(request, product);
        return mapper.toResponse(repository.save(product));
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheConfig.PRODUCT_CARD, key = "#id"),
            @CacheEvict(cacheNames = CacheConfig.FILTER_UNIVERSE, allEntries = true)
    })
    public void deactivate(String id) {
        Product product = load(id);
        if (product.isActive()) {
            product.setActive(false);
            product = repository.save(product);
            outbox.record(ProductEventType.PRODUCT_DEACTIVATED, product, product.getVariants());
        }
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheConfig.PRODUCT_CARD, key = "#id"),
            @CacheEvict(cacheNames = CacheConfig.FILTER_UNIVERSE, allEntries = true)
    })
    public ProductResponse activate(String id) {
        Product product = load(id);
        if (!hasActiveVariant(product.getVariants())) {
            throw new NoActiveVariantsException(id);
        }
        if (!product.isActive()) {
            product.setActive(true);
            product = repository.save(product);
            outbox.record(ProductEventType.PRODUCT_ACTIVATED, product, product.getVariants());
        }
        return mapper.toResponse(product);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheConfig.PRODUCT_CARD, key = "#id"),
            @CacheEvict(cacheNames = CacheConfig.FILTER_UNIVERSE, allEntries = true)
    })
    public ProductResponse addVariant(String id, VariantRequest request) {
        Product product = load(id);
        Variant variant = mapper.toVariant(request);
        variantValidator.checkNewVariant(product, variant);

        List<Variant> all = new ArrayList<>(product.getVariants());
        all.add(variant);
        product.setVariants(all);
        Product saved = repository.save(product);
        outbox.record(ProductEventType.VARIANT_ADDED, saved, List.of(variant));
        return mapper.toResponse(saved);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheConfig.PRODUCT_CARD, key = "#id"),
            @CacheEvict(cacheNames = CacheConfig.FILTER_UNIVERSE, allEntries = true)
    })
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
        Variant before = variants.get(index);
        Variant after = changed(before, request);
        variants.set(index, after);
        product.setVariants(variants);

        boolean productWasActive = product.isActive();
        if (productWasActive && !hasActiveVariant(variants)) {
            product.setActive(false);
        }
        Product saved = repository.save(product);

        if (before.active() != after.active()) {
            outbox.record(after.active()
                    ? ProductEventType.VARIANT_ACTIVATED
                    : ProductEventType.VARIANT_DEACTIVATED, saved, List.of(after));
        }
        if (productWasActive && !saved.isActive()) {
            outbox.record(ProductEventType.PRODUCT_DEACTIVATED, saved, saved.getVariants());
        }
        return mapper.toResponse(saved);
    }

    private boolean hasActiveVariant(List<Variant> variants) {
        return variants.stream().anyMatch(Variant::active);
    }

    @CacheEvict(cacheNames = CacheConfig.PRODUCT_CARD, key = "#result")
    public String setVariantStock(String sku, boolean inStock) {
        Product product = repository.findByVariantsSkuIn(Set.of(sku)).stream()
                .findFirst()
                .orElseThrow(() -> new VariantNotFoundException(sku));

        List<Variant> variants = new ArrayList<>(product.getVariants());
        for (int i = 0; i < variants.size(); i++) {
            Variant variant = variants.get(i);
            if (variant.sku().equals(sku)) {
                if (variant.inStock() != inStock) {
                    variants.set(i, variant.withInStock(inStock));
                    product.setVariants(variants);
                    repository.save(product);
                }
                break;
            }
        }
        return product.getId();
    }

    private Product load(String id) {
        return repository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));
    }

    private Variant changed(Variant current, UpdateVariantRequest request) {
        Variant result = current;
        if (request.price() != null) {
            result = result.withPrice(request.price());
        }
        if (request.active() != null) {
            result = result.withActive(request.active());
        }
        return result;
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