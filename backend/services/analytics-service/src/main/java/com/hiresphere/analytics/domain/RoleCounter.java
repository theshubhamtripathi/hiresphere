package com.hiresphere.analytics.domain;

import com.hiresphere.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "role_counters")
@Getter
@Setter
@NoArgsConstructor
public class RoleCounter extends BaseEntity {

    @Column(nullable = false, unique = true, length = 20)
    private String role;

    @Column(nullable = false)
    private long value;
}
