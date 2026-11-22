package com.sndbx.product.repository;

import com.sndbx.product.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;
import java.util.List;

public interface ProductRepository extends MongoRepository<Product, String> {
    List<Product> findByVariantsSkuIn(Collection<String> skus);
}