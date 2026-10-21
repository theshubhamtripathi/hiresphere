package com.hiresphere.job.repository;

import com.hiresphere.job.domain.Job;
import com.hiresphere.job.domain.JobStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface JobRepository extends JpaRepository<Job, Long>, JpaSpecificationExecutor<Job> {

    Page<Job> findByCompanyId(Long companyId, Pageable pageable);

    long countByCompanyIdAndStatus(Long companyId, JobStatus status);

    @Modifying
    @Query("update Job j set j.viewsCount = j.viewsCount + 1 where j.id = :id")
    void incrementViews(@Param("id") Long id);

    @Modifying
    @Query("update Job j set j.applicationsCount = j.applicationsCount + :delta where j.id = :id")
    void adjustApplicationsCount(@Param("id") Long id, @Param("delta") long delta);

    @Modifying
    @Query("update Job j set j.status = 'EXPIRED' where j.status = 'OPEN' and j.deadline < :today")
    int expirePastDeadline(@Param("today") LocalDate today);

    List<Job> findTop6ByCategoryIdAndStatusAndIdNot(Long categoryId, JobStatus status, Long excludeId);
}
