package com.hiresphere.resume.web;

import com.hiresphere.common.dto.ApiResponse;
import com.hiresphere.common.security.UserContextHolder;
import com.hiresphere.resume.dto.ResumeRequests;
import com.hiresphere.resume.dto.ResumeResponses;
import com.hiresphere.resume.service.ResumeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/resumes")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;

    private Long uid() {
        return UserContextHolder.currentUserId();
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ResumeResponses.ResumeDetail>> create(
            @Valid @RequestBody ResumeRequests.CreateResume req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Resume created", resumeService.create(uid(), req)));
    }

    @GetMapping
    public ApiResponse<List<ResumeResponses.ResumeSummary>> list() {
        return ApiResponse.ok(resumeService.list(uid()));
    }

    @GetMapping("/{id}")
    public ApiResponse<ResumeResponses.ResumeDetail> get(@PathVariable Long id) {
        return ApiResponse.ok(resumeService.getDetail(id, uid()));
    }

    @PutMapping("/{id}")
    public ApiResponse<ResumeResponses.ResumeDetail> update(@PathVariable Long id,
            @Valid @RequestBody ResumeRequests.CreateResume req) {
        return ApiResponse.ok(resumeService.updateMeta(id, uid(), req));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        resumeService.delete(id, uid());
        return ApiResponse.message("Resume deleted");
    }

    @PatchMapping("/{id}/default")
    public ApiResponse<ResumeResponses.ResumeDetail> setDefault(@PathVariable Long id) {
        return ApiResponse.ok(resumeService.setDefault(id, uid()));
    }

    @PutMapping("/{id}/personal-info")
    public ApiResponse<ResumeResponses.ResumeDetail> personalInfo(@PathVariable Long id,
            @Valid @RequestBody ResumeRequests.PersonalInfo req) {
        return ApiResponse.ok(resumeService.updatePersonalInfo(id, uid(), req));
    }

    @PutMapping("/{id}/summary")
    public ApiResponse<ResumeResponses.ResumeDetail> summary(@PathVariable Long id,
            @Valid @RequestBody ResumeRequests.Summary req) {
        return ApiResponse.ok(resumeService.updateSummary(id, uid(), req));
    }

    // ---- Experiences ----
    @PostMapping("/{id}/experiences")
    public ApiResponse<ResumeResponses.ResumeDetail> addExperience(@PathVariable Long id,
            @Valid @RequestBody ResumeRequests.Experience req) {
        return ApiResponse.ok(resumeService.addExperience(id, uid(), req));
    }

    @PutMapping("/{id}/experiences/{sectionId}")
    public ApiResponse<ResumeResponses.ResumeDetail> updateExperience(@PathVariable Long id,
            @PathVariable Long sectionId, @Valid @RequestBody ResumeRequests.Experience req) {
        return ApiResponse.ok(resumeService.updateExperience(id, uid(), sectionId, req));
    }

    @DeleteMapping("/{id}/experiences/{sectionId}")
    public ApiResponse<ResumeResponses.ResumeDetail> deleteExperience(@PathVariable Long id,
            @PathVariable Long sectionId) {
        return ApiResponse.ok(resumeService.deleteExperience(id, uid(), sectionId));
    }

    // ---- Education ----
    @PostMapping("/{id}/education")
    public ApiResponse<ResumeResponses.ResumeDetail> addEducation(@PathVariable Long id,
            @Valid @RequestBody ResumeRequests.Education req) {
        return ApiResponse.ok(resumeService.addEducation(id, uid(), req));
    }

    @PutMapping("/{id}/education/{sectionId}")
    public ApiResponse<ResumeResponses.ResumeDetail> updateEducation(@PathVariable Long id,
            @PathVariable Long sectionId, @Valid @RequestBody ResumeRequests.Education req) {
        return ApiResponse.ok(resumeService.updateEducation(id, uid(), sectionId, req));
    }

    @DeleteMapping("/{id}/education/{sectionId}")
    public ApiResponse<ResumeResponses.ResumeDetail> deleteEducation(@PathVariable Long id,
            @PathVariable Long sectionId) {
        return ApiResponse.ok(resumeService.deleteEducation(id, uid(), sectionId));
    }

    // ---- Skills ----
    @PostMapping("/{id}/skills")
    public ApiResponse<ResumeResponses.ResumeDetail> addSkill(@PathVariable Long id,
            @Valid @RequestBody ResumeRequests.Skill req) {
        return ApiResponse.ok(resumeService.addSkill(id, uid(), req));
    }

    @PutMapping("/{id}/skills/{sectionId}")
    public ApiResponse<ResumeResponses.ResumeDetail> updateSkill(@PathVariable Long id,
            @PathVariable Long sectionId, @Valid @RequestBody ResumeRequests.Skill req) {
        return ApiResponse.ok(resumeService.updateSkill(id, uid(), sectionId, req));
    }

    @DeleteMapping("/{id}/skills/{sectionId}")
    public ApiResponse<ResumeResponses.ResumeDetail> deleteSkill(@PathVariable Long id,
            @PathVariable Long sectionId) {
        return ApiResponse.ok(resumeService.deleteSkill(id, uid(), sectionId));
    }

    // ---- Projects ----
    @PostMapping("/{id}/projects")
    public ApiResponse<ResumeResponses.ResumeDetail> addProject(@PathVariable Long id,
            @Valid @RequestBody ResumeRequests.Project req) {
        return ApiResponse.ok(resumeService.addProject(id, uid(), req));
    }

    @PutMapping("/{id}/projects/{sectionId}")
    public ApiResponse<ResumeResponses.ResumeDetail> updateProject(@PathVariable Long id,
            @PathVariable Long sectionId, @Valid @RequestBody ResumeRequests.Project req) {
        return ApiResponse.ok(resumeService.updateProject(id, uid(), sectionId, req));
    }

    @DeleteMapping("/{id}/projects/{sectionId}")
    public ApiResponse<ResumeResponses.ResumeDetail> deleteProject(@PathVariable Long id,
            @PathVariable Long sectionId) {
        return ApiResponse.ok(resumeService.deleteProject(id, uid(), sectionId));
    }

    // ---- Certifications ----
    @PostMapping("/{id}/certifications")
    public ApiResponse<ResumeResponses.ResumeDetail> addCertification(@PathVariable Long id,
            @Valid @RequestBody ResumeRequests.Certification req) {
        return ApiResponse.ok(resumeService.addCertification(id, uid(), req));
    }

    @PutMapping("/{id}/certifications/{sectionId}")
    public ApiResponse<ResumeResponses.ResumeDetail> updateCertification(@PathVariable Long id,
            @PathVariable Long sectionId, @Valid @RequestBody ResumeRequests.Certification req) {
        return ApiResponse.ok(resumeService.updateCertification(id, uid(), sectionId, req));
    }

    @DeleteMapping("/{id}/certifications/{sectionId}")
    public ApiResponse<ResumeResponses.ResumeDetail> deleteCertification(@PathVariable Long id,
            @PathVariable Long sectionId) {
        return ApiResponse.ok(resumeService.deleteCertification(id, uid(), sectionId));
    }

    // ---- Awards ----
    @PostMapping("/{id}/awards")
    public ApiResponse<ResumeResponses.ResumeDetail> addAward(@PathVariable Long id,
            @Valid @RequestBody ResumeRequests.Award req) {
        return ApiResponse.ok(resumeService.addAward(id, uid(), req));
    }

    @PutMapping("/{id}/awards/{sectionId}")
    public ApiResponse<ResumeResponses.ResumeDetail> updateAward(@PathVariable Long id,
            @PathVariable Long sectionId, @Valid @RequestBody ResumeRequests.Award req) {
        return ApiResponse.ok(resumeService.updateAward(id, uid(), sectionId, req));
    }

    @DeleteMapping("/{id}/awards/{sectionId}")
    public ApiResponse<ResumeResponses.ResumeDetail> deleteAward(@PathVariable Long id,
            @PathVariable Long sectionId) {
        return ApiResponse.ok(resumeService.deleteAward(id, uid(), sectionId));
    }

    // ---- Languages ----
    @PostMapping("/{id}/languages")
    public ApiResponse<ResumeResponses.ResumeDetail> addLanguage(@PathVariable Long id,
            @Valid @RequestBody ResumeRequests.Language req) {
        return ApiResponse.ok(resumeService.addLanguage(id, uid(), req));
    }

    @PutMapping("/{id}/languages/{sectionId}")
    public ApiResponse<ResumeResponses.ResumeDetail> updateLanguage(@PathVariable Long id,
            @PathVariable Long sectionId, @Valid @RequestBody ResumeRequests.Language req) {
        return ApiResponse.ok(resumeService.updateLanguage(id, uid(), sectionId, req));
    }

    @DeleteMapping("/{id}/languages/{sectionId}")
    public ApiResponse<ResumeResponses.ResumeDetail> deleteLanguage(@PathVariable Long id,
            @PathVariable Long sectionId) {
        return ApiResponse.ok(resumeService.deleteLanguage(id, uid(), sectionId));
    }
}
