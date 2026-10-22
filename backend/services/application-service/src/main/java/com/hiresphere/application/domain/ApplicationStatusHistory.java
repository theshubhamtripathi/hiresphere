package com.hiresphere.application.domain;

import com.hiresphere.common.domain.BaseEntity;
import com.hiresphere.common.enums.ApplicationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "application_status_history",
        indexes = @Index(name = "idx_history_application", columnList = "application_id"))
@Getter
@Setter
@NoArgsConstructor
public class ApplicationStatusHistory extends BaseEntity {

    @Column(name = "application_id", nullable = false)
    private Long applicationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", length = 20)
    private ApplicationStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false, length = 20)
    private ApplicationStatus toStatus;

    @Column(name = "changed_by")
    private Long changedBy;

    @Column(columnDefinition = "text")
    private String note;
}
