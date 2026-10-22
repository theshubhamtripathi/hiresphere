package com.hiresphere.ai.repository;

import com.hiresphere.ai.domain.AiRequestLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiRequestLogRepository extends JpaRepository<AiRequestLog, Long> {
}
