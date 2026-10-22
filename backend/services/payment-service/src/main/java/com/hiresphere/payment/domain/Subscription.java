package com.hiresphere.payment.domain;

import com.hiresphere.common.domain.BaseEntity;
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
@Table(name = "subscriptions", indexes = @Index(name = "idx_sub_company", columnList = "company_id", unique = true))
@Getter
@Setter
@NoArgsConstructor
public class Subscription extends BaseEntity {

    @Column(name = "company_id", nullable = false, unique = true)
    private Long companyId;

    @Column(name = "plan_code", nullable = false, length = 20)
    private String planCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SubscriptionStatus status = SubscriptionStatus.ACTIVE;

    @Column(name = "valid_until")
    private Instant validUntil;

    @Column(name = "credits_left", nullable = false)
    private int creditsLeft;
}
