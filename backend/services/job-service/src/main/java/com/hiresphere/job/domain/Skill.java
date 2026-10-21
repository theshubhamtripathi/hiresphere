package com.hiresphere.job.domain;

import com.hiresphere.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "skills", indexes = {
        @Index(name = "idx_skill_name", columnList = "name", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
public class Skill extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String name;
}
