package com.sndbx.product.repository;

import com.mongodb.client.MongoCollection;
import com.sndbx.product.controller.filter.CatalogFilter;
import com.sndbx.product.dto.FilterUniverse;
import com.sndbx.product.dto.PriceRange;
import com.sndbx.product.model.Product;
import com.sndbx.product.repository.CatalogQueries.Facet;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.bson.types.Decimal128;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class CatalogFacetRepository {

    private final MongoTemplate mongoTemplate;

    public FacetCounts counts(CatalogFilter filter) {
        MongoCollection<Document> products =
                mongoTemplate.getCollection(mongoTemplate.getCollectionName(Product.class));
        Document result = products.aggregate(pipeline(filter)).first();
        return parse(result);
    }

    public FilterUniverse loadUniverse() {
        MongoCollection<Document> products =
                mongoTemplate.getCollection(mongoTemplate.getCollectionName(Product.class));
        Document base = new Document("active", true)
                .append("variants", new Document("$elemMatch", new Document("active", true)));

        List<String> brands = new ArrayList<>();
        products.distinct("brand", base, String.class).into(brands);
        brands.sort(String.CASE_INSENSITIVE_ORDER);

        List<Integer> volumes = new ArrayList<>();
        for (Document row : products.aggregate(List.of(
                new Document("$match", base),
                new Document("$unwind", "$variants"),
                new Document("$match", new Document("variants.active", true)),
                new Document("$group", new Document("_id", "$variants.volumeMl"))))) {
            volumes.add(row.get("_id", Integer.class));
        }
        Collections.sort(volumes);

        return new FilterUniverse(brands, volumes);
    }

    private List<Document> pipeline(CatalogFilter filter) {
        Document facets = new Document()
                .append("brands", countBy(filter, Facet.BRAND, "brand"))
                .append("genders", countBy(filter, Facet.GENDER, "gender"))
                .append("scents", scentCounts(filter))
                .append("volumes", volumeCounts(filter))
                .append("price", priceBounds(filter))
                .append("inStock", inStockBranch(filter));

        return List.of(
                new Document("$match", new Document("active", true)
                        .append("variants", new Document("$elemMatch", new Document("active", true)))),
                new Document("$project", new Document("active", 1)
                        .append("brand", 1)
                        .append("gender", 1)
                        .append("scentFamilies", 1)
                        .append("variants.active", 1)
                        .append("variants.inStock", 1)
                        .append("variants.volumeMl", 1)
                        .append("variants.price", 1)),
                new Document("$facet", facets));
    }

    private List<Document> countBy(CatalogFilter filter, Facet facet, String field) {
        return List.of(
                new Document("$match", CatalogQueries.productMatch(filter, facet)),
                new Document("$group", new Document("_id", "$" + field)
                        .append("count", new Document("$sum", 1))));
    }

    private List<Document> scentCounts(CatalogFilter filter) {
        return List.of(
                new Document("$match", CatalogQueries.productMatch(filter, Facet.SCENT)),
                new Document("$unwind", "$scentFamilies"),
                new Document("$group", new Document("_id", "$scentFamilies")
                        .append("count", new Document("$sum", 1))));
    }

    private List<Document> volumeCounts(CatalogFilter filter) {
        return List.of(
                new Document("$match", CatalogQueries.productMatch(filter, Facet.VOLUME)),
                new Document("$unwind", "$variants"),
                new Document("$match", CatalogQueries.unwoundVariantMatch(filter, false, true)),
                new Document("$group", new Document("_id", "$variants.volumeMl")
                        .append("count", new Document("$sum", 1))));
    }

    private List<Document> priceBounds(CatalogFilter filter) {
        return List.of(
                new Document("$match", CatalogQueries.productMatch(filter, Facet.PRICE)),
                new Document("$unwind", "$variants"),
                new Document("$match", CatalogQueries.unwoundVariantMatch(filter, true, false)),
                new Document("$group", new Document("_id", null)
                        .append("min", new Document("$min", "$variants.price"))
                        .append("max", new Document("$max", "$variants.price"))));
    }

    private List<Document> inStockBranch(CatalogFilter filter) {
        return List.of(
                new Document("$match", CatalogQueries.productMatch(filter, Facet.STOCK)),
                new Document("$match", new Document("variants", new Document("$elemMatch",
                        CatalogQueries.variantQuery(filter, true, true, true)))),
                new Document("$count", "count"));
    }

    private FacetCounts parse(Document result) {
        return new FacetCounts(
                counts(result, "brands", String.class),
                counts(result, "genders", String.class),
                counts(result, "scents", String.class),
                counts(result, "volumes", Integer.class),
                price(result),
                inStockCount(result));
    }

    private <K> Map<K, Long> counts(Document result, String branch, Class<K> keyType) {
        Map<K, Long> map = new HashMap<>();
        for (Document row : result.getList(branch, Document.class)) {
            map.put(row.get("_id", keyType), ((Number) row.get("count")).longValue());
        }
        return map;
    }

    private PriceRange price(Document result) {
        List<Document> rows = result.getList("price", Document.class);
        if (rows.isEmpty()) {
            return null;
        }
        Document row = rows.getFirst();
        return new PriceRange(decimal(row.get("min")), decimal(row.get("max")));
    }

    private long inStockCount(Document result) {
        List<Document> rows = result.getList("inStock", Document.class);
        return rows.isEmpty() ? 0 : ((Number) rows.get(0).get("count")).longValue();
    }

    private BigDecimal decimal(Object value) {
        return ((Decimal128) value).bigDecimalValue();
    }

    public record FacetCounts(
            Map<String, Long> brands,
            Map<String, Long> genders,
            Map<String, Long> scents,
            Map<Integer, Long> volumes,
            PriceRange price,
            long inStock
    ) {
    }
}