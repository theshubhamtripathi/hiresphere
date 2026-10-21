package com.hiresphere.user.domain;

import com.hiresphere.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "refresh_tokens", indexes = {
        @Index(name = "idx_refresh_token_hash", columnList = "token_hash", unique = true),
        @Index(name = "idx_refresh_user", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
public class RefreshToken extends BaseEntity {

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private boolean revoked = false;

    public boolean isActive() {
        return !revoked && expiresAt.isAfter(Instant.now());
    }
}
