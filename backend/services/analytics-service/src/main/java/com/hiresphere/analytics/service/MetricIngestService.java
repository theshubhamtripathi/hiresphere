package com.hiresphere.analytics.service;

import com.hiresphere.analytics.domain.JobCounter;
import com.hiresphere.analytics.domain.MetricCounter;
import com.hiresphere.analytics.domain.RoleCounter;
import com.hiresphere.analytics.domain.StatusCounter;
import com.hiresphere.analytics.repository.JobCounterRepository;
import com.hiresphere.analytics.repository.MetricCounterRepository;
import com.hiresphere.analytics.repository.RoleCounterRepository;
import com.hiresphere.analytics.repository.StatusCounterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/** Applies event deltas to the aggregate counters. */
@Service
@RequiredArgsConstructor
public class MetricIngestService {

    public static final String APPLICATIONS = "APPLICATIONS";
    public static final String JOBS = "JOBS";
    public static final String SIGNUPS = "SIGNUPS";
    public static final String REVENUE = "REVENUE";

    private final MetricCounterRepository metricRepository;
    private final StatusCounterRepository statusRepository;
    private final RoleCounterRepository roleRepository;
    private final JobCounterRepository jobRepository;

    @Transactional
    public void addMetric(Long scopeId, String metric, long delta) {
        LocalDate day = LocalDate.now();
        MetricCounter counter = metricRepository.findByScopeIdAndMetricAndDay(scopeId, metric, day)
                .orElseGet(() -> {
                    MetricCounter c = new MetricCounter();
                    c.setScopeId(scopeId);
                    c.setMetric(metric);
                    c.setDay(day);
                    c.setValue(0);
                    return c;
                });
        counter.setValue(counter.getValue() + delta);
        metricRepository.save(counter);
    }

    @Transactional
    public void incrementRole(String role) {
        RoleCounter counter = roleRepository.findByRole(role).orElseGet(() -> {
            RoleCounter c = new RoleCounter();
            c.setRole(role);
            c.setValue(0);
            return c;
        });
        counter.setValue(counter.getValue() + 1);
        roleRepository.save(counter);
    }

    @Transactional
    public void adjustStatus(Long scopeId, String status, long delta) {
        StatusCounter counter = statusRepository.findByScopeIdAndStatus(scopeId, status).orElseGet(() -> {
            StatusCounter c = new StatusCounter();
            c.setScopeId(scopeId);
            c.setStatus(status);
            c.setValue(0);
            return c;
        });
        counter.setValue(Math.max(0, counter.getValue() + delta));
        statusRepository.save(counter);
    }

    @Transactional
    public void ensureJob(Long jobId, Long companyId, String title) {
        if (jobRepository.findByJobId(jobId).isEmpty()) {
            JobCounter c = new JobCounter();
            c.setJobId(jobId);
            c.setCompanyId(companyId);
            c.setJobTitle(title);
            c.setApplications(0);
            jobRepository.save(c);
        }
    }

    @Transactional
    public void incrementJobApplications(Long jobId, Long companyId, String title) {
        JobCounter counter = jobRepository.findByJobId(jobId).orElseGet(() -> {
            JobCounter c = new JobCounter();
            c.setJobId(jobId);
            c.setCompanyId(companyId);
            c.setJobTitle(title);
            c.setApplications(0);
            return c;
        });
        if (title != null && counter.getJobTitle() == null) {
            counter.setJobTitle(title);
        }
        counter.setApplications(counter.getApplications() + 1);
        jobRepository.save(counter);
    }
}
