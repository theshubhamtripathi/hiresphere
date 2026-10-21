package com.hiresphere.job.web;

import com.hiresphere.common.enums.ExperienceLevel;
import com.hiresphere.common.enums.JobType;
import com.hiresphere.common.enums.WorkMode;
import com.hiresphere.common.exception.GlobalExceptionHandler;
import com.hiresphere.job.domain.JobStatus;
import com.hiresphere.job.dto.JobResponse;
import com.hiresphere.job.dto.JobSummaryResponse;
import com.hiresphere.job.service.JobService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(JobController.class)
@Import(GlobalExceptionHandler.class)
class JobControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean JobService jobService;

    private JobSummaryResponse summary() {
        return new JobSummaryResponse(1L, "Engineer", 3L, 1L, JobType.FULL_TIME, WorkMode.REMOTE,
                ExperienceLevel.MID, null, null, "INR", "Remote", JobStatus.OPEN, false, 0, null,
                List.of("Java"), null);
    }

    @Test
    void searchReturnsPagedJobs() throws Exception {
        when(jobService.search(any(), any()))
                .thenReturn(new PageImpl<>(List.of(summary()), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/jobs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Engineer"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getByIdRecordsViewAndReturnsJob() throws Exception {
        JobResponse response = new JobResponse(1L, "Engineer", "desc", null, null, null, 3L, 7L, 1L,
                JobType.FULL_TIME, WorkMode.REMOTE, ExperienceLevel.MID, null, null, null, null, "INR",
                "Remote", 1, null, JobStatus.OPEN, 0, 0, false, List.of("Java"), List.of(), null);
        when(jobService.getById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/jobs/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Engineer"));

        verify(jobService, times(1)).recordView(1L);
    }
}
