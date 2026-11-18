package com.sndbx.inventory.repository;

import com.sndbx.inventory.entity.OutboxEvent;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, String> {
    List<OutboxEvent> findBySentFalseOrderByCreatedAtAsc(Pageable pageable);
}