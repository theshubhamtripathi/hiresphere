package com.hiresphere.application.mapper;

import com.hiresphere.application.domain.ApplicationNote;
import com.hiresphere.application.domain.ApplicationStatusHistory;
import com.hiresphere.application.domain.JobApplication;
import com.hiresphere.application.dto.ApplicationResponses;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ApplicationMapper {

    public ApplicationResponses.Summary toSummary(JobApplication a) {
        return new ApplicationResponses.Summary(a.getId(), a.getJobId(), a.getApplicantId(),
                a.getCompanyId(), a.getStatus(), a.getAiMatchScore(), a.getCreatedAt(), a.getUpdatedAt());
    }

    public ApplicationResponses.Detail toDetail(JobApplication a, List<ApplicationStatusHistory> history) {
        return new ApplicationResponses.Detail(a.getId(), a.getJobId(), a.getApplicantId(), a.getCompanyId(),
                a.getResumeId(), a.getResumeSnapshot(), a.getCoverLetter(), a.getExpectedSalary(),
                a.getNoticePeriod(), a.getAvailableFrom(), a.getAnswers(), a.getStatus(), a.getAiMatchScore(),
                a.getAiMatchedSkills(), a.getAiMissingSkills(), a.getAiSummary(), a.getAiRecommendation(),
                history.stream().map(this::toHistory).toList(), a.getCreatedAt(), a.getUpdatedAt());
    }

    public ApplicationResponses.StatusHistoryItem toHistory(ApplicationStatusHistory h) {
        return new ApplicationResponses.StatusHistoryItem(h.getFromStatus(), h.getToStatus(),
                h.getChangedBy(), h.getNote(), h.getCreatedAt());
    }

    public ApplicationResponses.Note toNote(ApplicationNote n) {
        return new ApplicationResponses.Note(n.getId(), n.getAuthorId(), n.getContent(), n.getCreatedAt());
    }
}
