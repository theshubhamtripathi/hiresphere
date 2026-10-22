package com.hiresphere.ai.domain;

import com.hiresphere.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ai_request_logs", indexes = @Index(name = "idx_ai_log_user", columnList = "user_id"))
@Getter
@Setter
@NoArgsConstructor
public class AiRequestLog extends BaseEntity {

    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false, length = 60)
    private String feature;

    @Column(nullable = false)
    private boolean success;

    @Column(length = 20)
    private String provider;
}
