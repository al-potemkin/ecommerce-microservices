package com.sndbx.order.repository;

import com.sndbx.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserId(String userId);
    Optional<Order> findByOrderIdAndUserId(UUID id, String userId);

    @Modifying
    @Query("UPDATE Order o SET o.deleted = true WHERE o.id = :id AND o.deleted = false")
    boolean markOrderAsDeleted(@Param("id") Long id);
}
