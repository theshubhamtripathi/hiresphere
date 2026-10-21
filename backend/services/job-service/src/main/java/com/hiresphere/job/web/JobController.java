package com.hiresphere.job.web;

import com.hiresphere.common.dto.ApiResponse;
import com.hiresphere.common.dto.PageResponse;
import com.hiresphere.common.enums.ExperienceLevel;
import com.hiresphere.common.enums.JobType;
import com.hiresphere.common.enums.WorkMode;
import com.hiresphere.common.exception.ForbiddenException;
import com.hiresphere.common.security.UserContext;
import com.hiresphere.common.security.UserContextHolder;
import com.hiresphere.job.domain.JobStatus;
import com.hiresphere.job.dto.CreateJobRequest;
import com.hiresphere.job.dto.JobResponse;
import com.hiresphere.job.dto.JobSearchCriteria;
import com.hiresphere.job.dto.JobStatusRequest;
import com.hiresphere.job.dto.JobSummaryResponse;
import com.hiresphere.job.dto.UpdateJobRequest;
import com.hiresphere.job.service.JobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    @GetMapping
    public PageResponse<JobSummaryResponse> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) JobType jobType,
            @RequestParam(required = false) WorkMode workMode,
            @RequestParam(required = false) ExperienceLevel experienceLevel,
            @RequestParam(required = false) BigDecimal salaryMin,
            @RequestParam(required = false) BigDecimal salaryMax,
            @RequestParam(required = false) List<String> skills,
            @RequestParam(required = false) List<String> tags,
            @RequestParam(required = false) Long companyId,
            @RequestParam(required = false) Integer postedWithinDays,
            @PageableDefault(size = 20) Pageable pageable) {
        JobSearchCriteria criteria = new JobSearchCriteria(q, categoryId, location, jobType, workMode,
                experienceLevel, salaryMin, salaryMax, skills, tags, companyId, postedWithinDays, JobStatus.OPEN);
        return PageResponse.from(jobService.search(criteria, pageable));
    }

    @GetMapping("/my")
    public PageResponse<JobSummaryResponse> myJobs(@PageableDefault(size = 20) Pageable pageable) {
        return PageResponse.from(jobService.getCompanyJobs(requireEmployer().userId(), pageable));
    }

    @GetMapping("/{id}")
    public ApiResponse<JobResponse> get(@PathVariable Long id) {
        jobService.recordView(id);
        return ApiResponse.ok(jobService.getById(id));
    }

    @GetMapping("/{id}/similar")
    public ApiResponse<List<JobSummaryResponse>> similar(@PathVariable Long id) {
        return ApiResponse.ok(jobService.similar(id));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<JobResponse>> create(@Valid @RequestBody CreateJobRequest request) {
        JobResponse job = jobService.create(requireEmployer().userId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Job created", job));
    }

    @PutMapping("/{id}")
    public ApiResponse<JobResponse> update(@PathVariable Long id, @Valid @RequestBody UpdateJobRequest request) {
        return ApiResponse.ok(jobService.update(id, requireEmployer().userId(), request));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<JobResponse> status(@PathVariable Long id, @Valid @RequestBody JobStatusRequest request) {
        return ApiResponse.ok(jobService.changeStatus(id, requireEmployer().userId(), request.status()));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        jobService.delete(id, requireEmployer().userId());
        return ApiResponse.message("Job deleted");
    }

    private UserContext requireEmployer() {
        UserContext ctx = UserContextHolder.require();
        if (!ctx.isEmployer()) {
            throw new ForbiddenException("Only employers can manage jobs");
        }
        return ctx;
    }
}
