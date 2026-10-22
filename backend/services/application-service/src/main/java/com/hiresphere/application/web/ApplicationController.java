package com.hiresphere.application.web;

import com.hiresphere.application.dto.ApplicationRequests;
import com.hiresphere.application.dto.ApplicationResponses;
import com.hiresphere.application.service.ApplicationService;
import com.hiresphere.common.dto.ApiResponse;
import com.hiresphere.common.dto.PageResponse;
import com.hiresphere.common.enums.ApplicationStatus;
import com.hiresphere.common.exception.ForbiddenException;
import com.hiresphere.common.security.UserContext;
import com.hiresphere.common.security.UserContextHolder;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    // ---- Seeker ----

    @PostMapping
    public ResponseEntity<ApiResponse<ApplicationResponses.Detail>> apply(
            @Valid @RequestBody ApplicationRequests.Apply req) {
        UserContext ctx = requireSeeker();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Application submitted", applicationService.apply(ctx.userId(), req)));
    }

    @GetMapping("/my")
    public PageResponse<ApplicationResponses.Summary> my(@PageableDefault(size = 20) Pageable pageable) {
        return PageResponse.from(applicationService.getMine(UserContextHolder.require().userId(), pageable));
    }

    @GetMapping("/check")
    public ApiResponse<ApplicationResponses.Check> check(@RequestParam Long jobId) {
        return ApiResponse.ok(applicationService.check(jobId, UserContextHolder.require().userId()));
    }

    @PatchMapping("/{id}/withdraw")
    public ApiResponse<ApplicationResponses.Detail> withdraw(@PathVariable Long id) {
        return ApiResponse.ok(applicationService.withdraw(id, UserContextHolder.require().userId()));
    }

    // ---- Employer ----

    @GetMapping("/job/{jobId}")
    public PageResponse<ApplicationResponses.Summary> forJob(@PathVariable Long jobId,
            @RequestParam(required = false) ApplicationStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        return PageResponse.from(applicationService.getForJob(jobId, requireEmployer().userId(), status, pageable));
    }

    @GetMapping("/company")
    public PageResponse<ApplicationResponses.Summary> forCompany(
            @RequestParam(required = false) ApplicationStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        return PageResponse.from(applicationService.getForCompany(requireEmployer().userId(), status, pageable));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<ApplicationResponses.Detail> changeStatus(@PathVariable Long id,
            @Valid @RequestBody ApplicationRequests.StatusChange req) {
        return ApiResponse.ok(applicationService.changeStatus(id, requireEmployer().userId(), req));
    }

    // ---- Shared read ----

    @GetMapping("/{id}")
    public ApiResponse<ApplicationResponses.Detail> get(@PathVariable Long id) {
        UserContext ctx = UserContextHolder.require();
        return ApiResponse.ok(applicationService.getById(id, ctx.userId(), ctx.isAdmin()));
    }

    // ---- Notes (employer) ----

    @PostMapping("/{id}/notes")
    public ResponseEntity<ApiResponse<ApplicationResponses.Note>> addNote(@PathVariable Long id,
            @Valid @RequestBody ApplicationRequests.NoteRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Note added", applicationService.addNote(id, requireEmployer().userId(), req)));
    }

    @GetMapping("/{id}/notes")
    public ApiResponse<List<ApplicationResponses.Note>> notes(@PathVariable Long id) {
        return ApiResponse.ok(applicationService.listNotes(id, requireEmployer().userId()));
    }

    @DeleteMapping("/{id}/notes/{noteId}")
    public ApiResponse<Void> deleteNote(@PathVariable Long id, @PathVariable Long noteId) {
        applicationService.deleteNote(id, noteId, requireEmployer().userId());
        return ApiResponse.message("Note deleted");
    }

    private UserContext requireSeeker() {
        UserContext ctx = UserContextHolder.require();
        if (!ctx.isJobSeeker()) {
            throw new ForbiddenException("Only job seekers can apply to jobs");
        }
        return ctx;
    }

    private UserContext requireEmployer() {
        UserContext ctx = UserContextHolder.require();
        if (!ctx.isEmployer()) {
            throw new ForbiddenException("Only employers can access this resource");
        }
        return ctx;
    }
}
