package com.hiresphere.analytics.domain;

import com.hiresphere.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/** Daily counter per metric and scope (platform = scope_id 0). */
@Entity
@Table(name = "metric_counters",
        uniqueConstraints = @UniqueConstraint(name = "uq_metric", columnNames = {"scope_id", "metric", "day"}),
        indexes = @Index(name = "idx_metric_lookup", columnList = "scope_id,metric"))
@Getter
@Setter
@NoArgsConstructor
public class MetricCounter extends BaseEntity {

    public static final long PLATFORM = 0L;

    @Column(name = "scope_id", nullable = false)
    private Long scopeId;

    @Column(nullable = false, length = 30)
    private String metric;

    @Column(nullable = false)
    private LocalDate day;

    @Column(nullable = false)
    private long value;
}
