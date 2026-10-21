package com.hiresphere.company.domain;

import com.hiresphere.common.domain.BaseEntity;
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

import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "companies", indexes = {
        @Index(name = "idx_companies_slug", columnList = "slug", unique = true),
        @Index(name = "idx_companies_owner", columnList = "owner_user_id", unique = true),
        @Index(name = "idx_companies_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
public class Company extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(name = "logo_url")
    private String logoUrl;

    private String website;

    private String industry;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private CompanySize size;

    @Column(name = "founded_year")
    private Integer foundedYear;

    @Column(columnDefinition = "text")
    private String description;

    private String headquarters;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "social_links", columnDefinition = "jsonb")
    private Map<String, String> socialLinks = new HashMap<>();

    @Column(nullable = false)
    private boolean verified = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CompanyStatus status = CompanyStatus.ACTIVE;

    @Column(name = "owner_user_id", nullable = false, unique = true)
    private Long ownerUserId;
}
