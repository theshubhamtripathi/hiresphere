package com.hiresphere.preference.domain;

import com.hiresphere.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "saved_jobs",
        uniqueConstraints = @UniqueConstraint(name = "uq_saved_job", columnNames = {"user_id", "job_id"}),
        indexes = @Index(name = "idx_saved_user", columnList = "user_id"))
@Getter
@Setter
@NoArgsConstructor
public class SavedJob extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "job_id", nullable = false)
    private Long jobId;
}
