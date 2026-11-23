package com.sndbx.inventory.repository;

import com.sndbx.inventory.entity.StockItem;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface StockItemRepository extends JpaRepository<StockItem, String> {
    Optional<StockItem> findBySku(String sku);

    boolean existsBySku(String sku);

    Page<StockItem> findByProductId(String productId, Pageable pageable);

    @Modifying
    @Query(value = """
            INSERT INTO stock_item (sku, product_id)
            VALUES (:sku, :productId)
            ON CONFLICT (sku) DO NOTHING
            """, nativeQuery = true)
    int insertIfAbsent(@Param("sku") String sku, @Param("productId") String productId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from StockItem s where s.sku in :skus order by s.sku")
    List<StockItem> lockBySkus(@Param("skus") Collection<String> skus);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select s from StockItem s
            where s.id in (select r.stockItem.id from Reservation r where r.orderId = :orderId)
            order by s.sku
            """)
    List<StockItem> lockByOrderId(@Param("orderId") String orderId);
}