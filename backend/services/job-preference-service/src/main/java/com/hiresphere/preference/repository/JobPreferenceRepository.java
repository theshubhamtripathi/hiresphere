package com.hiresphere.preference.repository;

import com.hiresphere.preference.domain.JobPreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JobPreferenceRepository extends JpaRepository<JobPreference, Long> {

    Optional<JobPreference> findByUserId(Long userId);

    List<JobPreference> findByAlertsEnabledTrue();
}
