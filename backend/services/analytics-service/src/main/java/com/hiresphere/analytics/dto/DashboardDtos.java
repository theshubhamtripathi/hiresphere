package com.hiresphere.analytics.dto;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public final class DashboardDtos {

    private DashboardDtos() {
    }

    public record DayPoint(LocalDate date, long value) implements Serializable {
    }

    public record TopJob(Long jobId, String title, long applications) implements Serializable {
    }

    public record EmployerDashboard(
            long activeJobs,
            long totalApplications,
            Map<String, Long> applicationsByStatus,
            List<DayPoint> last30Days,
            List<TopJob> topJobs) implements Serializable {
    }

    public record AdminDashboard(
            Map<String, Long> usersByRole,
            long totalCompanies,
            long totalJobs,
            long totalApplications,
            Map<String, Long> applicationsByStatus,
            List<DayPoint> signupTrend,
            long revenue) implements Serializable {
    }
}
