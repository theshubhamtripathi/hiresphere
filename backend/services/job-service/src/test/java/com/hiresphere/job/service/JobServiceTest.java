package com.hiresphere.job.service;

import com.hiresphere.common.enums.ExperienceLevel;
import com.hiresphere.common.enums.JobType;
import com.hiresphere.common.enums.WorkMode;
import com.hiresphere.common.exception.ForbiddenException;
import com.hiresphere.job.client.CompanyClient;
import com.hiresphere.job.client.PaymentClient;
import com.hiresphere.job.domain.Job;
import com.hiresphere.job.domain.JobStatus;
import com.hiresphere.job.dto.CreateJobRequest;
import com.hiresphere.job.dto.JobResponse;
import com.hiresphere.job.mapper.JobMapper;
import com.hiresphere.job.repository.JobRepository;
import com.hiresphere.job.repository.SkillRepository;
import com.hiresphere.job.repository.TagRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    private final JobRepository jobRepository = mock(JobRepository.class);
    private final SkillRepository skillRepository = mock(SkillRepository.class);
    private final TagRepository tagRepository = mock(TagRepository.class);
    private final JobMapper jobMapper = mock(JobMapper.class);
    private final CompanyClient companyClient = mock(CompanyClient.class);
    private final PaymentClient paymentClient = mock(PaymentClient.class);
    private final JobEventPublisher publisher = mock(JobEventPublisher.class);

    private final JobService service = new JobService(jobRepository, skillRepository, tagRepository,
            jobMapper, companyClient, paymentClient, publisher);

    private CreateJobRequest request(boolean publish) {
        return new CreateJobRequest("Engineer", "Great role", null, null, null, 1L,
                JobType.FULL_TIME, WorkMode.REMOTE, ExperienceLevel.MID, 2, 5, null, null, "INR",
                "Remote", 1, null, List.of(), List.of(), publish);
    }

    @Test
    void publishesEventWhenCreatedAsOpen() {
        when(companyClient.getByOwner(7L)).thenReturn(new CompanyClient.Company(3L, "Acme", null, "ACTIVE"));
        when(paymentClient.consumeJobCredit(3L)).thenReturn(true);
        when(jobRepository.save(any(Job.class))).thenAnswer(inv -> {
            Job j = inv.getArgument(0);
            j.setId(100L);
            return j;
        });
        when(jobMapper.toResponse(any(Job.class))).thenReturn(mock(JobResponse.class));

        service.create(7L, request(true));

        verify(publisher, times(1)).publishJobPosted(any(Job.class));
    }

    @Test
    void draftCreationDoesNotPublish() {
        when(companyClient.getByOwner(7L)).thenReturn(new CompanyClient.Company(3L, "Acme", null, "ACTIVE"));
        when(jobRepository.save(any(Job.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jobMapper.toResponse(any(Job.class))).thenReturn(mock(JobResponse.class));

        service.create(7L, request(false));

        verify(publisher, never()).publishJobPosted(any(Job.class));
    }

    @Test
    void updatingAnotherOwnersJobIsForbidden() {
        Job job = new Job();
        job.setPostedBy(999L);
        job.setStatus(JobStatus.OPEN);
        when(jobRepository.findById(5L)).thenReturn(Optional.of(job));

        assertThatThrownBy(() -> service.delete(5L, 7L)).isInstanceOf(ForbiddenException.class);
    }
}
