package com.sndbx.product.service;

import com.sndbx.product.config.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.sndbx.product.dto.FilterUniverse;
import com.sndbx.product.repository.CatalogFacetRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FilterUniverseService {

    private final CatalogFacetRepository repository;

    @Cacheable(cacheNames = CacheConfig.FILTER_UNIVERSE, key = "'all'")
    public FilterUniverse get() {
        return repository.loadUniverse();
    }
}