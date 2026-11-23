package com.sndbx.inventory.repository;

import com.sndbx.inventory.entity.Reservation;
import com.sndbx.inventory.entity.ReservationStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("""
            select r from Reservation r
            join fetch r.stockItem s
            where r.orderId = :orderId
            order by s.sku
            """)
    List<Reservation> findByOrderId(@Param("orderId") String orderId);

    @Query("""
            select distinct r.orderId from Reservation r
            where r.status = :status and r.expiresAt < :now
            """)
    List<String> findExpiredOrderIds(@Param("status") ReservationStatus status,
                                     @Param("now") Instant now,
                                     Pageable pageable);

    @Modifying
    @Query("delete from Reservation r where r.status in :statuses and r.updatedAt < :before")
    int deleteFinishedBefore(@Param("statuses") Collection<ReservationStatus> statuses,
                             @Param("before") Instant before);
}