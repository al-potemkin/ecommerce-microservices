package com.sndbx.product.repository;

import com.sndbx.product.model.outbox.OutboxEvent;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OutboxRepository extends MongoRepository<OutboxEvent, String> {
}