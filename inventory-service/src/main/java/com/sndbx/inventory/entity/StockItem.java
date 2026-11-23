package com.sndbx.inventory.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "stock_item")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StockItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sku", nullable = false, updatable = false, length = 64)
    private String sku;

    @Column(name = "product_id", nullable = false, length = 64)
    private String productId;

    @Column(name = "on_hand", nullable = false)
    private int onHand = 0;

    @Column(name = "reserved", nullable = false)
    private int reserved = 0;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public StockItem(String sku, String productId) {
        this.sku = sku;
        this.productId = productId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StockItem other)) return false;
        return sku != null && sku.equals(other.getSku());
    }

    @Override
    public int hashCode() {
        return Objects.hash(sku);
    }
}