package com.sndbx.product.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter
@Document(collection = "products")
public class Product {

    @Id
    private String id;

    private String name;

    @Indexed
    private String brand;

    private String description;

    @Indexed
    private Gender gender;

    @Indexed
    private List<ScentFamily> scentFamilies = new ArrayList<>();

    private String mainImage;                          // для каталога
    private List<String> gallery = new ArrayList<>();  // карусель в карточке

    private List<Variant> variants = new ArrayList<>();

    @Indexed
    private boolean active = true;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @Version
    private Long version;
}