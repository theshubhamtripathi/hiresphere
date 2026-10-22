package com.hiresphere.ai.web;

import com.hiresphere.ai.dto.AiRequests;
import com.hiresphere.ai.dto.AiResults;
import com.hiresphere.ai.service.AiService;
import com.hiresphere.common.dto.ApiResponse;
import com.hiresphere.common.security.UserContextHolder;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    private Long uid() {
        return UserContextHolder.currentUserId();
    }

    @PostMapping("/resume/summary")
    public ApiResponse<AiResults.TextResult> summary(@Valid @RequestBody AiRequests.Summary req) {
        return ApiResponse.ok(aiService.summary(uid(), req));
    }

    @PostMapping("/resume/improve-bullet")
    public ApiResponse<AiResults.TextResult> improveBullet(@Valid @RequestBody AiRequests.ImproveBullet req) {
        return ApiResponse.ok(aiService.improveBullet(uid(), req));
    }

    @PostMapping("/resume/analyze")
    public ApiResponse<AiResults.ResumeAnalysis> analyze(@Valid @RequestBody AiRequests.Analyze req) {
        return ApiResponse.ok(aiService.analyze(uid(), req));
    }

    @PostMapping("/jobs/description")
    public ApiResponse<AiResults.TextResult> jobDescription(@Valid @RequestBody AiRequests.JobDescription req) {
        return ApiResponse.ok(aiService.jobDescription(uid(), req));
    }

    @PostMapping("/jobs/skills-suggest")
    public ApiResponse<AiResults.SkillsResult> skills(@Valid @RequestBody AiRequests.SkillsSuggest req) {
        return ApiResponse.ok(aiService.suggestSkills(uid(), req));
    }

    @PostMapping("/applications/cover-letter")
    public ApiResponse<AiResults.TextResult> coverLetter(@Valid @RequestBody AiRequests.CoverLetter req) {
        return ApiResponse.ok(aiService.coverLetter(uid(), req));
    }

    @PostMapping("/applications/{id}/screen")
    public ApiResponse<AiResults.Screening> screen(@PathVariable Long id) {
        return ApiResponse.ok(aiService.screen(uid(), id));
    }

    @PostMapping("/search")
    public ApiResponse<AiResults.SearchResponse> search(@Valid @RequestBody AiRequests.Search req) {
        return ApiResponse.ok(aiService.search(uid(), req));
    }
}
