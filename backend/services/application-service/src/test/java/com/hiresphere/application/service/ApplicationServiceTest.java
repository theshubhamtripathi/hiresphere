package com.hiresphere.application.service;

import com.hiresphere.application.client.CompanyClient;
import com.hiresphere.application.client.JobClient;
import com.hiresphere.application.client.ResumeClient;
import com.hiresphere.application.client.UserClient;
import com.hiresphere.application.domain.JobApplication;
import com.hiresphere.application.dto.ApplicationRequests;
import com.hiresphere.application.mapper.ApplicationMapper;
import com.hiresphere.application.repository.ApplicationNoteRepository;
import com.hiresphere.application.repository.ApplicationStatusHistoryRepository;
import com.hiresphere.application.repository.JobApplicationRepository;
import com.hiresphere.common.enums.ApplicationStatus;
import com.hiresphere.common.exception.BadRequestException;
import com.hiresphere.common.exception.ConflictException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ApplicationServiceTest {

    private final JobApplicationRepository appRepo = mock(JobApplicationRepository.class);
    private final ApplicationNoteRepository noteRepo = mock(ApplicationNoteRepository.class);
    private final ApplicationStatusHistoryRepository historyRepo = mock(ApplicationStatusHistoryRepository.class);
    private final ApplicationMapper mapper = new ApplicationMapper();
    private final JobClient jobClient = mock(JobClient.class);
    private final ResumeClient resumeClient = mock(ResumeClient.class);
    private final UserClient userClient = mock(UserClient.class);
    private final CompanyClient companyClient = mock(CompanyClient.class);
    private final ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);

    private final ApplicationService service = new ApplicationService(appRepo, noteRepo, historyRepo,
            mapper, jobClient, resumeClient, userClient, companyClient, publisher);

    private ApplicationRequests.Apply applyReq() {
        return new ApplicationRequests.Apply(10L, 2L, "cover", null, null, null, null);
    }

    @Test
    void duplicateApplicationIsRejected() {
        when(appRepo.existsByJobIdAndApplicantId(10L, 1L)).thenReturn(true);
        assertThatThrownBy(() -> service.apply(1L, applyReq())).isInstanceOf(ConflictException.class);
    }

    @Test
    void applyingToClosedJobIsRejected() {
        when(appRepo.existsByJobIdAndApplicantId(10L, 1L)).thenReturn(false);
        when(jobClient.getById(10L)).thenReturn(new JobClient.JobInfo(10L, "Engineer", "CLOSED", 3L));
        assertThatThrownBy(() -> service.apply(1L, applyReq())).isInstanceOf(BadRequestException.class);
    }

    @Test
    void invalidStatusTransitionIsRejected() {
        JobApplication app = new JobApplication();
        app.setId(5L);
        app.setCompanyId(3L);
        app.setApplicantId(9L);
        app.setJobId(10L);
        app.setStatus(ApplicationStatus.APPLIED);
        when(appRepo.findById(5L)).thenReturn(Optional.of(app));
        when(companyClient.getByOwner(7L)).thenReturn(new CompanyClient.CompanyInfo(3L, "Acme"));

        assertThatThrownBy(() -> service.changeStatus(5L, 7L,
                new ApplicationRequests.StatusChange(ApplicationStatus.HIRED, null)))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void validStatusTransitionPublishesEvent() {
        JobApplication app = new JobApplication();
        app.setId(5L);
        app.setCompanyId(3L);
        app.setApplicantId(9L);
        app.setJobId(10L);
        app.setStatus(ApplicationStatus.APPLIED);
        when(appRepo.findById(5L)).thenReturn(Optional.of(app));
        when(companyClient.getByOwner(7L)).thenReturn(new CompanyClient.CompanyInfo(3L, "Acme"));
        when(jobClient.getById(10L)).thenReturn(new JobClient.JobInfo(10L, "Engineer", "OPEN", 3L));
        when(userClient.getById(9L)).thenReturn(new UserClient.UserInfo(9L, "a@x.com", "Alice"));
        when(historyRepo.findByApplicationIdOrderByCreatedAtAsc(5L)).thenReturn(List.of());

        service.changeStatus(5L, 7L, new ApplicationRequests.StatusChange(ApplicationStatus.UNDER_REVIEW, "reviewing"));

        verify(publisher, times(1)).publishStatusChanged(any(), any(), any(),
                org.mockito.ArgumentMatchers.eq(ApplicationStatus.APPLIED),
                org.mockito.ArgumentMatchers.eq(ApplicationStatus.UNDER_REVIEW));
        verify(historyRepo, times(1)).save(any());
    }
}
