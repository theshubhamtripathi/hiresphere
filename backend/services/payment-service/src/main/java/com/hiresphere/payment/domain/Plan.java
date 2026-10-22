package com.hiresphere.payment.domain;

import com.hiresphere.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "plans", indexes = @Index(name = "idx_plan_code", columnList = "code", unique = true))
@Getter
@Setter
@NoArgsConstructor
public class Plan extends BaseEntity {

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(name = "price_inr", nullable = false, precision = 12, scale = 2)
    private BigDecimal priceInr;

    @Column(name = "job_credits", nullable = false)
    private int jobCredits;

    @Column(name = "featured_listings", nullable = false)
    private int featuredListings;

    @Column(columnDefinition = "text")
    private String description;
}
