package com.sndbx.product.repository;

import com.mongodb.client.MongoCollection;
import com.sndbx.product.controller.filter.CatalogFilter;
import com.sndbx.product.dto.CatalogSort;
import com.sndbx.product.model.CatalogItemView;
import com.sndbx.product.model.Product;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.bson.types.Decimal128;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class CatalogQueryRepository {

    private final MongoTemplate mongoTemplate;

    public Page<CatalogItemView> search(CatalogFilter filter) {
        MongoCollection<Document> products =
                mongoTemplate.getCollection(mongoTemplate.getCollectionName(Product.class));
        PageRequest pageRequest = PageRequest.of(filter.page(), filter.size());

        Document match = CatalogQueries.productMatch(filter, null);
        long total = products.countDocuments(match);
        if (total == 0) {
            return Page.empty(pageRequest);
        }

        List<CatalogItemView> items = products.aggregate(pipeline(filter, match))
                .into(new ArrayList<>())
                .stream()
                .map(doc -> mongoTemplate.getConverter().read(CatalogItemView.class, doc))
                .toList();

        return new PageImpl<>(items, pageRequest, total);
    }

    private List<Document> pipeline(CatalogFilter filter, Document match) {
        boolean sortByPrice = filter.sort() == CatalogSort.PRICE_ASC
                || filter.sort() == CatalogSort.PRICE_DESC;

        List<Document> page = List.of(
                sortStage(filter.sort()),
                new Document("$skip", (long) filter.page() * filter.size()),
                new Document("$limit", filter.size()));

        List<Document> computeVariants = List.of(
                new Document("$addFields", new Document("matchedVariants", CatalogQueries.matchedVariants(filter))),
                new Document("$addFields",
                        new Document("minPrice", new Document("$min", "$matchedVariants.price"))));

        List<Document> stages = new ArrayList<>();
        stages.add(new Document("$match", match));
        if (sortByPrice) {
            stages.addAll(computeVariants);
            stages.addAll(page);
        } else {
            stages.addAll(page);
            stages.addAll(computeVariants);
        }
        stages.add(new Document("$project", new Document("name", 1)
                .append("brand", 1)
                .append("gender", 1)
                .append("mainImage", 1)
                .append("matchedVariants", 1)
                .append("minPrice", 1)));
        return stages;
    }

    private Document sortStage(CatalogSort sort) {
        Document order = switch (sort) {
            case NEWEST -> new Document("createdAt", -1);
            case PRICE_ASC -> new Document("minPrice", 1);
            case PRICE_DESC -> new Document("minPrice", -1);
            case NAME -> new Document("name", 1);
        };
        return new Document("$sort", order.append("_id", 1));
    }
}