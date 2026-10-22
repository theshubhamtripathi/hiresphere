package com.hiresphere.analytics.repository;

import com.hiresphere.analytics.domain.MetricCounter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MetricCounterRepository extends JpaRepository<MetricCounter, Long> {

    Optional<MetricCounter> findByScopeIdAndMetricAndDay(Long scopeId, String metric, LocalDate day);

    List<MetricCounter> findByScopeIdAndMetricAndDayGreaterThanEqualOrderByDayAsc(
            Long scopeId, String metric, LocalDate since);

    @Query("select coalesce(sum(m.value), 0) from MetricCounter m where m.scopeId = :scopeId and m.metric = :metric")
    long sum(@Param("scopeId") Long scopeId, @Param("metric") String metric);
}
