package com.hiresphere.job.web;

import com.hiresphere.job.dto.JobResponse;
import com.hiresphere.job.dto.JobSummaryResponse;
import com.hiresphere.job.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Service-to-service endpoints. Never exposed through the gateway. */
@RestController
@RequestMapping("/api/internal/jobs")
@RequiredArgsConstructor
public class InternalJobController {

    private final JobService jobService;

    @GetMapping("/{id}")
    public JobResponse getById(@PathVariable Long id) {
        return jobService.getById(id);
    }

    @GetMapping
    public List<JobSummaryResponse> batch(@RequestParam List<Long> ids) {
        return jobService.getByIds(ids);
    }

    @PatchMapping("/{id}/applications-count")
    public void adjust(@PathVariable Long id, @RequestParam long delta) {
        jobService.adjustApplicationsCount(id, delta);
    }

    @GetMapping("/company/{companyId}/active-count")
    public long activeCount(@PathVariable Long companyId) {
        return jobService.activeJobCount(companyId);
    }
}
