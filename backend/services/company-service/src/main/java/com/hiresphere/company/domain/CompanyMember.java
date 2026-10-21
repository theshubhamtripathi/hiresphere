package com.hiresphere.company.domain;

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
@Table(name = "company_members",
        uniqueConstraints = @UniqueConstraint(name = "uq_company_member",
                columnNames = {"company_id", "user_id"}),
        indexes = @Index(name = "idx_member_company", columnList = "company_id"))
@Getter
@Setter
@NoArgsConstructor
public class CompanyMember extends BaseEntity {

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** Owner-assigned title, e.g. "Recruiter", "Hiring Manager". */
    private String title;
}
