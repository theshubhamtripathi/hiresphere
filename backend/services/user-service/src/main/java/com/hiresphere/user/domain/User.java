package com.hiresphere.user.domain;

import com.hiresphere.common.domain.BaseEntity;
import com.hiresphere.common.enums.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_users_email", columnList = "email", unique = true),
        @Index(name = "idx_users_role", columnList = "role")
})
@Getter
@Setter
@NoArgsConstructor
public class User extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(name = "avatar_url")
    private String avatarUrl;

    private String headline;

    private String location;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;
}
