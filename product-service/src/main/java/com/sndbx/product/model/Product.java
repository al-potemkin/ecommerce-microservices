package com.sndbx.product.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Document(collection = "products")
@CompoundIndexes({
        @CompoundIndex(name = "active_createdAt_id",
                def = "{'active': 1, 'createdAt': -1, '_id': 1}"),
        @CompoundIndex(name = "active_brand",
                def = "{'active': 1, 'brand': 1}"),
        @CompoundIndex(name = "active_gender",
                def = "{'active': 1, 'gender': 1}"),
        @CompoundIndex(name = "active_scentFamilies",
                def = "{'active': 1, 'scentFamilies': 1}"),
        @CompoundIndex(name = "variants_volume_price",
                def = "{'variants.volumeMl': 1, 'variants.price': 1}")
})
public class Product {

    @Id
    private String id;

    private String name;

    private String brand;

    private String description;

    private Gender gender;

    private Set<ScentFamily> scentFamilies = new HashSet<>();

    private String mainImage;
    private List<String> gallery = new ArrayList<>();

    private List<Variant> variants = new ArrayList<>();

    private boolean active = true;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @Version
    private Long version;
}