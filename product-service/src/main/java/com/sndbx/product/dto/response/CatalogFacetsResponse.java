package com.sndbx.product.dto.response;

import com.sndbx.product.dto.FacetValue;
import com.sndbx.product.dto.PriceRange;
import com.sndbx.product.model.Gender;
import com.sndbx.product.model.ScentFamily;

import java.util.List;

public record CatalogFacetsResponse(
        List<FacetValue<String>> brands,
        List<FacetValue<Gender>> genders,
        List<FacetValue<ScentFamily>> scents,
        List<FacetValue<Integer>> volumes,
        PriceRange price,
        long inStockCount) {
}