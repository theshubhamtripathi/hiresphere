package com.hiresphere.ai.service;

import com.hiresphere.ai.client.AiClient;
import com.hiresphere.ai.client.ServiceClients;
import com.hiresphere.ai.domain.AiRequestLog;
import com.hiresphere.ai.dto.AiRequests;
import com.hiresphere.ai.dto.AiResults;
import com.hiresphere.ai.repository.AiRequestLogRepository;
import com.hiresphere.common.exception.ForbiddenException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiService {

    private final AiClient aiClient;
    private final ServiceClients serviceClients;
    private final AiQuotaService quotaService;
    private final AiRequestLogRepository logRepository;

    public AiResults.TextResult summary(Long userId, AiRequests.Summary req) {
        return run(userId, "resume-summary", () -> new AiResults.TextResult(aiClient.generateSummary(req)));
    }

    public AiResults.TextResult improveBullet(Long userId, AiRequests.ImproveBullet req) {
        return run(userId, "improve-bullet", () -> new AiResults.TextResult(aiClient.improveBullet(req)));
    }

    public AiResults.ResumeAnalysis analyze(Long userId, AiRequests.Analyze req) {
        return run(userId, "resume-analyze",
                () -> aiClient.analyzeResume(serviceClients.resumeSnapshot(req.resumeId())));
    }

    public AiResults.TextResult jobDescription(Long userId, AiRequests.JobDescription req) {
        return run(userId, "job-description",
                () -> new AiResults.TextResult(aiClient.generateJobDescription(req)));
    }

    public AiResults.SkillsResult suggestSkills(Long userId, AiRequests.SkillsSuggest req) {
        return run(userId, "skills-suggest", () -> new AiResults.SkillsResult(aiClient.suggestSkills(req)));
    }

    public AiResults.TextResult coverLetter(Long userId, AiRequests.CoverLetter req) {
        return run(userId, "cover-letter", () -> {
            String resume = serviceClients.resumeSnapshot(req.resumeId());
            String job = serviceClients.jobJson(req.jobId());
            return new AiResults.TextResult(aiClient.generateCoverLetter(resume, job));
        });
    }

    public AiResults.Screening screen(Long employerUserId, Long applicationId) {
        return run(employerUserId, "screen", () -> {
            ServiceClients.ApplicationView application = serviceClients.application(applicationId);
            Long companyId = serviceClients.companyIdForOwner(employerUserId);
            if (!application.companyId().equals(companyId)) {
                throw new ForbiddenException("This application does not belong to your company");
            }
            String job = serviceClients.jobJson(application.jobId());
            AiResults.Screening screening = aiClient.screen(application.resumeSnapshot(), job);
            serviceClients.saveScreening(applicationId, screening);
            return screening;
        });
    }

    public AiResults.SearchResponse search(Long userId, AiRequests.Search req) {
        return run(userId, "search", () -> {
            AiResults.SearchFilters filters = aiClient.parseSearch(req.query());
            Object results = serviceClients.searchJobs(filters);
            return new AiResults.SearchResponse(filters, results);
        });
    }

    private <T> T run(Long userId, String feature, java.util.function.Supplier<T> action) {
        quotaService.consume(userId);
        boolean success = false;
        try {
            T result = action.get();
            success = true;
            return result;
        } finally {
            log(userId, feature, success);
        }
    }

    private void log(Long userId, String feature, boolean success) {
        try {
            AiRequestLog entry = new AiRequestLog();
            entry.setUserId(userId);
            entry.setFeature(feature);
            entry.setSuccess(success);
            entry.setProvider(aiClient.isLive() ? "gemini" : "mock");
            logRepository.save(entry);
        } catch (RuntimeException ignored) {
            // logging must never break the request
        }
    }
}
