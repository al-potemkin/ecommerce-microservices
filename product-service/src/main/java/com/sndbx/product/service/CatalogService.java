package com.sndbx.product.service;

import com.sndbx.product.controller.filter.CatalogFilter;
import com.sndbx.product.dto.response.CatalogItemResponse;
import com.sndbx.product.dto.response.PageResponse;
import com.sndbx.product.mapper.CatalogMapper;
import com.sndbx.product.repository.CatalogQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CatalogService {

    private final CatalogQueryRepository repository;
    private final CatalogMapper mapper;

    public PageResponse<CatalogItemResponse> search(CatalogFilter filter) {
        return mapper.toPageResponse(repository.search(filter));
    }
}