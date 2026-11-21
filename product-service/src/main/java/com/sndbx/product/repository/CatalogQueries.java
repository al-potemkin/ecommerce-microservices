package com.sndbx.product.repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.sndbx.product.controller.filter.CatalogFilter;
import org.bson.Document;
import org.bson.types.Decimal128;

final class CatalogQueries {

    enum Facet { BRAND, GENDER, SCENT, VOLUME, PRICE }

    private CatalogQueries() {
    }

    static Document productMatch(CatalogFilter filter, Facet excluded) {
        Document match = new Document("active", true);
        if (excluded != Facet.BRAND && !filter.brand().isEmpty()) {
            match.append("brand", new Document("$in", new ArrayList<>(filter.brand())));
        }
        if (excluded != Facet.GENDER && !filter.gender().isEmpty()) {
            match.append("gender", new Document("$in", names(filter.gender())));
        }
        if (excluded != Facet.SCENT && !filter.scent().isEmpty()) {
            match.append("scentFamilies", new Document("$in", names(filter.scent())));
        }
        match.append("variants", new Document("$elemMatch",
                variantQuery(filter, excluded != Facet.VOLUME, excluded != Facet.PRICE)));
        return match;
    }

    static Document variantQuery(CatalogFilter filter, boolean withVolume, boolean withPrice) {
        Document query = new Document("active", true);
        if (withVolume && !filter.volume().isEmpty()) {
            query.append("volumeMl", new Document("$in", new ArrayList<>(filter.volume())));
        }
        if (withPrice) {
            Document price = priceRange(filter);
            if (!price.isEmpty()) {
                query.append("price", price);
            }
        }
        return query;
    }

    static Document unwoundVariantMatch(CatalogFilter filter, boolean withVolume, boolean withPrice) {
        Document prefixed = new Document();
        variantQuery(filter, withVolume, withPrice)
                .forEach((key, value) -> prefixed.append("variants." + key, value));
        return prefixed;
    }

    static Document matchedVariants(CatalogFilter filter) {
        List<Document> and = new ArrayList<>();
        and.add(new Document("$eq", List.of("$$v.active", true)));
        if (!filter.volume().isEmpty()) {
            and.add(new Document("$in", List.of("$$v.volumeMl", new ArrayList<>(filter.volume()))));
        }
        if (filter.priceMin() != null) {
            and.add(new Document("$gte", List.of("$$v.price", new Decimal128(filter.priceMin()))));
        }
        if (filter.priceMax() != null) {
            and.add(new Document("$lte", List.of("$$v.price", new Decimal128(filter.priceMax()))));
        }
        return new Document("$filter", new Document("input", "$variants")
                .append("as", "v")
                .append("cond", new Document("$and", and)));
    }

    private static Document priceRange(CatalogFilter filter) {
        Document price = new Document();
        if (filter.priceMin() != null) {
            price.append("$gte", new Decimal128(filter.priceMin()));
        }
        if (filter.priceMax() != null) {
            price.append("$lte", new Decimal128(filter.priceMax()));
        }
        return price;
    }

    private static List<String> names(Collection<? extends Enum<?>> values) {
        return values.stream().map(Enum::name).toList();
    }
}