package com.sndbx.product.repository;

import com.sndbx.product.model.outbox.OutboxEvent;
import com.sndbx.product.model.outbox.OutboxStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;

import java.time.Instant;
import java.util.List;

public interface OutboxRepository extends MongoRepository<OutboxEvent, String> {
    List<OutboxEvent> findTop100ByStatusOrderByIdAsc(OutboxStatus status);

    @Query("{ '_id': ?0, 'status': 'PENDING' }")
    @Update("{ '$set': { 'status': 'SENT', 'sentAt': ?1 }, '$inc': { 'attempts': 1 } }")
    long markSent(String id, Instant sentAt);

    @Query("{ '_id': ?0 }")
    @Update("{ '$inc': { 'attempts': 1 } }")
    long incrementAttempts(String id);
}