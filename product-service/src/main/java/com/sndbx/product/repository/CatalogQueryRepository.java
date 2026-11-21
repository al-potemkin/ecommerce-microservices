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

        Document match = productMatch(filter);
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
                new Document("$addFields", new Document("matchedVariants", filterVariants(filter))),
                new Document("$addFields",
                        new Document("minPrice", new Document("$min", "$matchedVariants.price"))));

        List<Document> stages = new ArrayList<>();
        stages.add(new Document("$match", match));
        if (sortByPrice) {
            // цена считается на лету: сначала расчёт, потом сортировка
            stages.addAll(computeVariants);
            stages.addAll(page);
        } else {
            // сортировка по полям документа сразу после $match может использовать индекс,
            // а варианты считаются только для документов текущей страницы
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

    // условия на уровне товара; $elemMatch гарантирует, что все условия по вариантам
    // выполняются на одном и том же варианте
    private Document productMatch(CatalogFilter filter) {
        Document match = new Document("active", true);
        if (!filter.brand().isEmpty()) {
            match.append("brand", new Document("$in", new ArrayList<>(filter.brand())));
        }
        if (!filter.gender().isEmpty()) {
            match.append("gender", new Document("$in", names(filter.gender())));
        }
        if (!filter.scent().isEmpty()) {
            match.append("scentFamilies", new Document("$in", names(filter.scent())));
        }
        match.append("variants", new Document("$elemMatch", variantQuery(filter)));
        return match;
    }

    // условия на вариант в виде запроса (для $match / $elemMatch)
    private Document variantQuery(CatalogFilter filter) {
        Document query = new Document("active", true);
        if (!filter.volume().isEmpty()) {
            query.append("volumeMl", new Document("$in", new ArrayList<>(filter.volume())));
        }
        Document price = new Document();
        if (filter.priceMin() != null) {
            price.append("$gte", new Decimal128(filter.priceMin()));
        }
        if (filter.priceMax() != null) {
            price.append("$lte", new Decimal128(filter.priceMax()));
        }
        if (!price.isEmpty()) {
            query.append("price", price);
        }
        return query;
    }

    // те же условия, но как выражение для $filter (переменная v - текущий вариант)
    private Document filterVariants(CatalogFilter filter) {
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

    // _id в конце нужен для стабильного порядка: без него товары с одинаковой ценой
    // или датой могли бы «прыгать» между страницами
    private Document sortStage(CatalogSort sort) {
        Document order = switch (sort) {
            case NEWEST -> new Document("createdAt", -1);
            case PRICE_ASC -> new Document("minPrice", 1);
            case PRICE_DESC -> new Document("minPrice", -1);
            case NAME -> new Document("name", 1);
        };
        return new Document("$sort", order.append("_id", 1));
    }

    private static List<String> names(Collection<? extends Enum<?>> values) {
        return values.stream().map(Enum::name).toList();
    }
}