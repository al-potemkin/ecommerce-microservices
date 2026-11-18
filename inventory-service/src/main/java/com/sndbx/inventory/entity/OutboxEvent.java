package com.sndbx.inventory.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "outbox_event")
public class OutboxEvent {
    @Id
    @Column(name = "key", length = 36)
    private UUID key;
    @Column(name = "topic", nullable = false)
    private String topic;
    @Column(name = "payload", nullable = false)
    private String payload;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "sent")
    private Boolean sent;

    public OutboxEvent(UUID key, String topic, String payload) {
        this.key = key;
        this.topic = topic;
        this.payload = payload;
        this.createdAt = LocalDateTime.now();
        this.sent = false;
    }

    public OutboxEvent() {

    }

    public UUID getKey() {
        return key;
    }

    public void setKey(UUID key) {
        this.key = key;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Boolean getSent() {
        return sent;
    }

    public void setSent(Boolean sent) {
        this.sent = sent;
    }
}