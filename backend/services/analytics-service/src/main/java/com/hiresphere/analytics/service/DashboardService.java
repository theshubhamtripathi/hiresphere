package com.hiresphere.analytics.service;

import com.hiresphere.analytics.client.AnalyticsClients;
import com.hiresphere.analytics.config.CacheConfig;
import com.hiresphere.analytics.domain.JobCounter;
import com.hiresphere.analytics.domain.MetricCounter;
import com.hiresphere.analytics.domain.RoleCounter;
import com.hiresphere.analytics.domain.StatusCounter;
import com.hiresphere.analytics.dto.DashboardDtos.AdminDashboard;
import com.hiresphere.analytics.dto.DashboardDtos.DayPoint;
import com.hiresphere.analytics.dto.DashboardDtos.EmployerDashboard;
import com.hiresphere.analytics.dto.DashboardDtos.TopJob;
import com.hiresphere.analytics.repository.JobCounterRepository;
import com.hiresphere.analytics.repository.MetricCounterRepository;
import com.hiresphere.analytics.repository.RoleCounterRepository;
import com.hiresphere.analytics.repository.StatusCounterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final MetricCounterRepository metricRepository;
    private final StatusCounterRepository statusRepository;
    private final RoleCounterRepository roleRepository;
    private final JobCounterRepository jobRepository;
    private final AnalyticsClients clients;

    @Cacheable(value = CacheConfig.DASHBOARDS, key = "'employer:' + #ownerUserId")
    @Transactional(readOnly = true)
    public EmployerDashboard employer(Long ownerUserId) {
        Long companyId = clients.companyIdForOwner(ownerUserId);
        long activeJobs = clients.activeJobCount(companyId);
        long totalApplications = metricRepository.sum(companyId, MetricIngestService.APPLICATIONS);
        Map<String, Long> byStatus = statusMap(statusRepository.findByScopeId(companyId));
        List<DayPoint> trend = trend(companyId, MetricIngestService.APPLICATIONS);
        List<TopJob> topJobs = jobRepository.findTop5ByCompanyIdOrderByApplicationsDesc(companyId).stream()
                .map(this::toTopJob).toList();
        return new EmployerDashboard(activeJobs, totalApplications, byStatus, trend, topJobs);
    }

    @Cacheable(value = CacheConfig.DASHBOARDS, key = "'admin'")
    @Transactional(readOnly = true)
    public AdminDashboard admin() {
        Map<String, Long> usersByRole = new LinkedHashMap<>();
        for (RoleCounter rc : roleRepository.findAll()) {
            usersByRole.put(rc.getRole(), rc.getValue());
        }
        long totalJobs = metricRepository.sum(MetricCounter.PLATFORM, MetricIngestService.JOBS);
        long totalApplications = metricRepository.sum(MetricCounter.PLATFORM, MetricIngestService.APPLICATIONS);
        long revenue = metricRepository.sum(MetricCounter.PLATFORM, MetricIngestService.REVENUE);
        Map<String, Long> byStatus = statusMap(statusRepository.findByScopeId(MetricCounter.PLATFORM));
        List<DayPoint> signupTrend = trend(MetricCounter.PLATFORM, MetricIngestService.SIGNUPS);
        return new AdminDashboard(usersByRole, clients.totalCompanies(), totalJobs, totalApplications,
                byStatus, signupTrend, revenue);
    }

    private List<DayPoint> trend(Long scopeId, String metric) {
        LocalDate since = LocalDate.now().minusDays(29);
        return metricRepository.findByScopeIdAndMetricAndDayGreaterThanEqualOrderByDayAsc(scopeId, metric, since)
                .stream().map(m -> new DayPoint(m.getDay(), m.getValue())).toList();
    }

    private Map<String, Long> statusMap(List<StatusCounter> counters) {
        Map<String, Long> map = new LinkedHashMap<>();
        for (StatusCounter c : counters) {
            if (c.getValue() > 0) {
                map.put(c.getStatus(), c.getValue());
            }
        }
        return map;
    }

    private TopJob toTopJob(JobCounter j) {
        return new TopJob(j.getJobId(), j.getJobTitle(), j.getApplications());
    }
}
