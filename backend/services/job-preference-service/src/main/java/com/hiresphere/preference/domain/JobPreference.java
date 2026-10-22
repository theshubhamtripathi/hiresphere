package com.hiresphere.preference.domain;

import com.hiresphere.common.domain.BaseEntity;
import com.hiresphere.common.enums.JobType;
import com.hiresphere.common.enums.WorkMode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "job_preferences", indexes = @Index(name = "idx_pref_user", columnList = "user_id", unique = true))
@Getter
@Setter
@NoArgsConstructor
public class JobPreference extends BaseEntity {

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "desired_titles", columnDefinition = "jsonb")
    private List<String> desiredTitles = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> locations = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "job_types", columnDefinition = "jsonb")
    private List<JobType> jobTypes = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "work_modes", columnDefinition = "jsonb")
    private List<WorkMode> workModes = new ArrayList<>();

    @Column(name = "min_salary", precision = 15, scale = 2)
    private BigDecimal minSalary;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "category_ids", columnDefinition = "jsonb")
    private List<Long> categoryIds = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> skills = new ArrayList<>();

    @Column(name = "alerts_enabled", nullable = false)
    private boolean alertsEnabled = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "alert_frequency", length = 20)
    private AlertFrequency alertFrequency = AlertFrequency.INSTANT;
}
