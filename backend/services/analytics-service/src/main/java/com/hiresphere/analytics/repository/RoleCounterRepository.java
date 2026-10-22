package com.hiresphere.analytics.repository;

import com.hiresphere.analytics.domain.RoleCounter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleCounterRepository extends JpaRepository<RoleCounter, Long> {

    Optional<RoleCounter> findByRole(String role);
}
