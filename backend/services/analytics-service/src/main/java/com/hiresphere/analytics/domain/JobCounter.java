package com.hiresphere.analytics.domain;

import com.hiresphere.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "job_counters",
        indexes = {
                @Index(name = "idx_jobcounter_job", columnList = "job_id", unique = true),
                @Index(name = "idx_jobcounter_company", columnList = "company_id")
        })
@Getter
@Setter
@NoArgsConstructor
public class JobCounter extends BaseEntity {

    @Column(name = "job_id", nullable = false, unique = true)
    private Long jobId;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "job_title")
    private String jobTitle;

    @Column(nullable = false)
    private long applications;
}
