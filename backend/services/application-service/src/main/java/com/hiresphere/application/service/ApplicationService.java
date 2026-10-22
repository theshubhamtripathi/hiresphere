package com.hiresphere.application.service;

import com.hiresphere.application.client.CompanyClient;
import com.hiresphere.application.client.JobClient;
import com.hiresphere.application.client.ResumeClient;
import com.hiresphere.application.client.UserClient;
import com.hiresphere.application.domain.ApplicationNote;
import com.hiresphere.application.domain.ApplicationStatusHistory;
import com.hiresphere.application.domain.JobApplication;
import com.hiresphere.application.dto.ApplicationRequests;
import com.hiresphere.application.dto.ApplicationResponses;
import com.hiresphere.application.mapper.ApplicationMapper;
import com.hiresphere.application.repository.ApplicationNoteRepository;
import com.hiresphere.application.repository.ApplicationStatusHistoryRepository;
import com.hiresphere.application.repository.JobApplicationRepository;
import com.hiresphere.common.enums.ApplicationStatus;
import com.hiresphere.common.exception.BadRequestException;
import com.hiresphere.common.exception.ConflictException;
import com.hiresphere.common.exception.ForbiddenException;
import com.hiresphere.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final JobApplicationRepository applicationRepository;
    private final ApplicationNoteRepository noteRepository;
    private final ApplicationStatusHistoryRepository historyRepository;
    private final ApplicationMapper mapper;
    private final JobClient jobClient;
    private final ResumeClient resumeClient;
    private final UserClient userClient;
    private final CompanyClient companyClient;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public ApplicationResponses.Detail apply(Long applicantId, ApplicationRequests.Apply req) {
        if (applicationRepository.existsByJobIdAndApplicantId(req.jobId(), applicantId)) {
            throw new ConflictException("You have already applied to this job");
        }
        JobClient.JobInfo job = jobClient.getById(req.jobId());
        if (!"OPEN".equals(job.status())) {
            throw new BadRequestException("This job is not accepting applications");
        }

        JobApplication app = new JobApplication();
        app.setJobId(req.jobId());
        app.setApplicantId(applicantId);
        app.setCompanyId(job.companyId());
        app.setResumeId(req.resumeId());
        app.setResumeSnapshot(resumeClient.getSnapshot(req.resumeId()));
        app.setCoverLetter(req.coverLetter());
        app.setExpectedSalary(req.expectedSalary());
        app.setNoticePeriod(req.noticePeriod());
        app.setAvailableFrom(req.availableFrom());
        app.setAnswers(req.answers() == null ? new HashMap<>() : req.answers());
        app.setStatus(ApplicationStatus.APPLIED);
        app = applicationRepository.save(app);

        recordHistory(app, null, ApplicationStatus.APPLIED, applicantId, "Application submitted");
        jobClient.adjustApplicationsCount(req.jobId(), 1);
        eventPublisher.publishSubmitted(app, job.title(), userClient.getById(applicantId));

        return mapper.toDetail(app, history(app.getId()));
    }

    @Transactional(readOnly = true)
    public Page<ApplicationResponses.Summary> getMine(Long applicantId, Pageable pageable) {
        return applicationRepository.findByApplicantId(applicantId, pageable).map(mapper::toSummary);
    }

    @Transactional(readOnly = true)
    public ApplicationResponses.Detail getById(Long id, Long userId, boolean admin) {
        JobApplication app = find(id);
        if (!admin && !app.getApplicantId().equals(userId) && !ownsCompany(userId, app.getCompanyId())) {
            throw new ForbiddenException("You cannot view this application");
        }
        return mapper.toDetail(app, history(id));
    }

    @Transactional
    public ApplicationResponses.Detail withdraw(Long id, Long applicantId) {
        JobApplication app = find(id);
        if (!app.getApplicantId().equals(applicantId)) {
            throw new ForbiddenException("You can only withdraw your own application");
        }
        if (app.getStatus().isTerminal()) {
            throw new BadRequestException("This application can no longer be withdrawn");
        }
        ApplicationStatus previous = app.getStatus();
        app.setStatus(ApplicationStatus.WITHDRAWN);
        recordHistory(app, previous, ApplicationStatus.WITHDRAWN, applicantId, "Withdrawn by applicant");
        eventPublisher.publishStatusChanged(app, jobTitle(app), userClient.getById(applicantId),
                previous, ApplicationStatus.WITHDRAWN);
        return mapper.toDetail(app, history(id));
    }

    @Transactional(readOnly = true)
    public ApplicationResponses.Check check(Long jobId, Long applicantId) {
        return applicationRepository.findByJobIdAndApplicantId(jobId, applicantId)
                .map(a -> new ApplicationResponses.Check(true, a.getId(), a.getStatus()))
                .orElse(new ApplicationResponses.Check(false, null, null));
    }

    @Transactional(readOnly = true)
    public Page<ApplicationResponses.Summary> getForJob(Long jobId, Long employerUserId,
                                                        ApplicationStatus status, Pageable pageable) {
        Long companyId = companyClient.getByOwner(employerUserId).id();
        JobClient.JobInfo job = jobClient.getById(jobId);
        if (!companyId.equals(job.companyId())) {
            throw new ForbiddenException("This job does not belong to your company");
        }
        Page<JobApplication> page = status == null
                ? applicationRepository.findByJobId(jobId, pageable)
                : applicationRepository.findByJobIdAndStatus(jobId, status, pageable);
        return page.map(mapper::toSummary);
    }

    @Transactional(readOnly = true)
    public Page<ApplicationResponses.Summary> getForCompany(Long employerUserId,
                                                            ApplicationStatus status, Pageable pageable) {
        Long companyId = companyClient.getByOwner(employerUserId).id();
        Page<JobApplication> page = status == null
                ? applicationRepository.findByCompanyId(companyId, pageable)
                : applicationRepository.findByCompanyIdAndStatus(companyId, status, pageable);
        return page.map(mapper::toSummary);
    }

    @Transactional
    public ApplicationResponses.Detail changeStatus(Long id, Long employerUserId,
                                                    ApplicationRequests.StatusChange req) {
        JobApplication app = requireEmployerOwns(id, employerUserId);
        ApplicationStatus previous = app.getStatus();
        if (!previous.canTransitionTo(req.status())) {
            throw new BadRequestException("Cannot change status from " + previous + " to " + req.status());
        }
        app.setStatus(req.status());
        recordHistory(app, previous, req.status(), employerUserId, req.note());
        eventPublisher.publishStatusChanged(app, jobTitle(app), userClient.getById(app.getApplicantId()),
                previous, req.status());
        return mapper.toDetail(app, history(id));
    }

    // ---- Notes ----

    @Transactional
    public ApplicationResponses.Note addNote(Long applicationId, Long employerUserId,
                                             ApplicationRequests.NoteRequest req) {
        requireEmployerOwns(applicationId, employerUserId);
        ApplicationNote note = new ApplicationNote();
        note.setApplicationId(applicationId);
        note.setAuthorId(employerUserId);
        note.setContent(req.content());
        return mapper.toNote(noteRepository.save(note));
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponses.Note> listNotes(Long applicationId, Long employerUserId) {
        requireEmployerOwns(applicationId, employerUserId);
        return noteRepository.findByApplicationIdOrderByCreatedAtDesc(applicationId).stream()
                .map(mapper::toNote).toList();
    }

    @Transactional
    public void deleteNote(Long applicationId, Long noteId, Long employerUserId) {
        requireEmployerOwns(applicationId, employerUserId);
        ApplicationNote note = noteRepository.findByIdAndApplicationId(noteId, applicationId)
                .orElseThrow(() -> ResourceNotFoundException.of("Note", noteId));
        noteRepository.delete(note);
    }

    // ---- Internal views ----

    public record InternalView(Long id, Long jobId, Long companyId, Long applicantId, String resumeSnapshot) {
    }

    @Transactional(readOnly = true)
    public InternalView getInternal(Long id) {
        JobApplication a = find(id);
        return new InternalView(a.getId(), a.getJobId(), a.getCompanyId(), a.getApplicantId(), a.getResumeSnapshot());
    }

    // ---- AI result (internal) ----

    @Transactional
    public void applyAiResult(Long applicationId, ApplicationRequests.AiScreeningResult result) {
        JobApplication app = find(applicationId);
        app.setAiMatchScore(result.matchScore());
        app.setAiMatchedSkills(result.matchedSkills());
        app.setAiMissingSkills(result.missingSkills());
        app.setAiSummary(result.summary());
        app.setAiRecommendation(result.recommendation());
    }

    // ---- Helpers ----

    private JobApplication find(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Application", id));
    }

    private JobApplication requireEmployerOwns(Long applicationId, Long employerUserId) {
        JobApplication app = find(applicationId);
        if (!ownsCompany(employerUserId, app.getCompanyId())) {
            throw new ForbiddenException("This application does not belong to your company");
        }
        return app;
    }

    private boolean ownsCompany(Long userId, Long companyId) {
        try {
            return companyClient.getByOwner(userId).id().equals(companyId);
        } catch (RuntimeException ex) {
            return false;
        }
    }

    private String jobTitle(JobApplication app) {
        try {
            return jobClient.getById(app.getJobId()).title();
        } catch (RuntimeException ex) {
            return "a job";
        }
    }

    private void recordHistory(JobApplication app, ApplicationStatus from, ApplicationStatus to,
                               Long changedBy, String note) {
        ApplicationStatusHistory history = new ApplicationStatusHistory();
        history.setApplicationId(app.getId());
        history.setFromStatus(from);
        history.setToStatus(to);
        history.setChangedBy(changedBy);
        history.setNote(note);
        historyRepository.save(history);
    }

    private List<ApplicationStatusHistory> history(Long applicationId) {
        return historyRepository.findByApplicationIdOrderByCreatedAtAsc(applicationId);
    }
}
