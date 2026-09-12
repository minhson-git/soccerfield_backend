package com.ms.test_api.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "invalidated_tokens",
       indexes = @Index(name = "idx_invalidated_tokens_expires_at", columnList = "expires_at"))
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InvalidatedToken {

    @Id
    @Column(name = "token_id", length = 36)
    String id;

    @Column(name = "expires_at", nullable = false)
    Instant expiresAt;

    public InvalidatedToken(String id, Instant expiresAt) {
        this.id = id;
        this.expiresAt = expiresAt;
    }
}