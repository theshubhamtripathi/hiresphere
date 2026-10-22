package com.hiresphere.application.domain;

import com.hiresphere.common.domain.BaseEntity;
import com.hiresphere.common.enums.ApplicationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "job_applications",
        uniqueConstraints = @UniqueConstraint(name = "uq_job_applicant", columnNames = {"job_id", "applicant_id"}),
        indexes = {
                @Index(name = "idx_app_applicant", columnList = "applicant_id"),
                @Index(name = "idx_app_job", columnList = "job_id"),
                @Index(name = "idx_app_company", columnList = "company_id")
        })
@Getter
@Setter
@NoArgsConstructor
public class JobApplication extends BaseEntity {

    @Column(name = "job_id", nullable = false)
    private Long jobId;

    @Column(name = "applicant_id", nullable = false)
    private Long applicantId;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "resume_id")
    private Long resumeId;

    @Column(name = "resume_snapshot", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String resumeSnapshot;

    @Column(name = "cover_letter", columnDefinition = "text")
    private String coverLetter;

    @Column(name = "expected_salary", precision = 15, scale = 2)
    private BigDecimal expectedSalary;

    @Column(name = "notice_period")
    private String noticePeriod;

    @Column(name = "available_from")
    private LocalDate availableFrom;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, String> answers = new HashMap<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApplicationStatus status = ApplicationStatus.APPLIED;

    @Column(name = "ai_match_score")
    private Integer aiMatchScore;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "ai_matched_skills", columnDefinition = "jsonb")
    private List<String> aiMatchedSkills;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "ai_missing_skills", columnDefinition = "jsonb")
    private List<String> aiMissingSkills;

    @Column(name = "ai_summary", columnDefinition = "text")
    private String aiSummary;

    @Column(name = "ai_recommendation")
    private String aiRecommendation;
}
