package com.hiresphere.preference.repository;

import com.hiresphere.preference.domain.SavedJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface SavedJobRepository extends JpaRepository<SavedJob, Long> {

    List<SavedJob> findByUserIdOrderByCreatedAtDesc(Long userId);

    boolean existsByUserIdAndJobId(Long userId, Long jobId);

    @Transactional
    void deleteByUserIdAndJobId(Long userId, Long jobId);
}
