package com.sndbx.product.service;

import com.sndbx.product.controller.filter.CatalogFilter;
import com.sndbx.product.dto.FacetValue;
import com.sndbx.product.dto.FilterUniverse;
import com.sndbx.product.dto.PriceRange;
import com.sndbx.product.dto.response.CatalogFacetsResponse;
import com.sndbx.product.dto.response.CatalogItemResponse;
import com.sndbx.product.dto.response.PageResponse;
import com.sndbx.product.mapper.CatalogMapper;
import com.sndbx.product.model.Gender;
import com.sndbx.product.model.ScentFamily;
import com.sndbx.product.repository.CatalogFacetRepository;
import com.sndbx.product.repository.CatalogQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.RoundingMode;
import java.util.List;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class CatalogService {

    private final CatalogQueryRepository queryRepository;
    private final CatalogFacetRepository facetRepository;
    private final FilterUniverseService universeService;
    private final CatalogMapper mapper;

    public PageResponse<CatalogItemResponse> findProductsFromCatalog(CatalogFilter filter) {
        return mapper.toPageResponse(queryRepository.search(filter));
    }

    public CatalogFacetsResponse fetchCatalogFilters(CatalogFilter filter) {
        CatalogFacetRepository.FacetCounts counts = facetRepository.counts(filter);
        FilterUniverse universe = universeService.get();

        return new CatalogFacetsResponse(
                facetValues(universe.brands(), b -> counts.brands().getOrDefault(b, 0L)),
                facetValues(List.of(Gender.values()), g -> counts.genders().getOrDefault(g.name(), 0L)),
                facetValues(List.of(ScentFamily.values()), s -> counts.scents().getOrDefault(s.name(), 0L)),
                facetValues(universe.volumes(), v -> counts.volumes().getOrDefault(v, 0L)),
                roundPriceRange(counts.price()),
                counts.inStock()
        );
    }

    private static <T> List<FacetValue<T>> facetValues(List<T> values, Function<T, Long> count) {
        return values.stream()
                .map(v -> new FacetValue<>(v, count.apply(v)))
                .toList();
    }

    private static PriceRange roundPriceRange(PriceRange price) {
        if (price == null) {
            return null;
        }
        return new PriceRange(
                price.min().setScale(0, RoundingMode.FLOOR),
                price.max().setScale(0, RoundingMode.CEILING)
        );
    }
}