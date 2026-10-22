package com.hiresphere.analytics.web;

import com.hiresphere.analytics.dto.DashboardDtos.AdminDashboard;
import com.hiresphere.analytics.dto.DashboardDtos.EmployerDashboard;
import com.hiresphere.analytics.service.DashboardService;
import com.hiresphere.common.dto.ApiResponse;
import com.hiresphere.common.exception.ForbiddenException;
import com.hiresphere.common.security.UserContext;
import com.hiresphere.common.security.UserContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final DashboardService dashboardService;

    @GetMapping("/employer/dashboard")
    public ApiResponse<EmployerDashboard> employer() {
        UserContext ctx = UserContextHolder.require();
        if (!ctx.isEmployer()) {
            throw new ForbiddenException("Only employers can view this dashboard");
        }
        return ApiResponse.ok(dashboardService.employer(ctx.userId()));
    }

    @GetMapping("/admin/dashboard")
    public ApiResponse<AdminDashboard> admin() {
        if (!UserContextHolder.require().isAdmin()) {
            throw new ForbiddenException("Administrator access required");
        }
        return ApiResponse.ok(dashboardService.admin());
    }
}
