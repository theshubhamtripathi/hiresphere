package com.hiresphere.resume.domain;

import com.hiresphere.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "awards")
@Getter
@Setter
@NoArgsConstructor
public class Award extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    @Column(nullable = false)
    private String title;

    private String issuer;

    @Column(name = "award_date")
    private LocalDate date;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "display_order", nullable = false)
    private int displayOrder = 0;
}
