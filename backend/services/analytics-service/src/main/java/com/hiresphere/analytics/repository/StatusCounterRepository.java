package com.hiresphere.analytics.repository;

import com.hiresphere.analytics.domain.StatusCounter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StatusCounterRepository extends JpaRepository<StatusCounter, Long> {

    Optional<StatusCounter> findByScopeIdAndStatus(Long scopeId, String status);

    List<StatusCounter> findByScopeId(Long scopeId);
}
