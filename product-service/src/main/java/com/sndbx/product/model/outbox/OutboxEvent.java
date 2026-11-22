package com.sndbx.product.model.outbox;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Document("outbox")
@CompoundIndex(name = "status_id", def = "{'status': 1, '_id': 1}")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutboxEvent {
    @Id
    private String id;

    private String eventId;
    private String aggregateId;
    private String type;
    private String payload;
    private OutboxStatus status;
    private Instant createdAt;

    @Indexed(expireAfter = "7d")
    private Instant sentAt;

    private int attempts;
}