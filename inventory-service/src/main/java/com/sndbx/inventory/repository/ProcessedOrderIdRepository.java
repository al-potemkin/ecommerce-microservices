package com.sndbx.inventory.repository;

import com.sndbx.inventory.entity.ProcessedOrderId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProcessedOrderIdRepository extends JpaRepository<ProcessedOrderId, UUID> {
}
