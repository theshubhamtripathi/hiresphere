package com.hiresphere.analytics.domain;

import com.hiresphere.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Current application-status distribution per scope (platform = scope_id 0). */
@Entity
@Table(name = "status_counters",
        uniqueConstraints = @UniqueConstraint(name = "uq_status", columnNames = {"scope_id", "status"}))
@Getter
@Setter
@NoArgsConstructor
public class StatusCounter extends BaseEntity {

    @Column(name = "scope_id", nullable = false)
    private Long scopeId;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(nullable = false)
    private long value;
}
