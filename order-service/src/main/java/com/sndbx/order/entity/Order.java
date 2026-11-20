package com.sndbx.order.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.NaturalId;
import org.springframework.data.annotation.Id;

import java.util.List;

@Getter @Setter
@ToString
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter(AccessLevel.NONE)
    @NaturalId
    @Column(nullable = false, unique = true, updatable = false)
    private String orderId;
    private String email;
    private List<Product> products;

    private boolean deleted;
}