package com.hiresphere.resume.web;

import com.hiresphere.common.exception.GlobalExceptionHandler;
import com.hiresphere.common.security.UserContextFilter;
import com.hiresphere.resume.dto.ResumeResponses;
import com.hiresphere.resume.service.ResumeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ResumeController.class)
@Import({GlobalExceptionHandler.class, ResumeControllerTest.FilterConfig.class})
class ResumeControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean ResumeService resumeService;

    @TestConfiguration
    static class FilterConfig {
        @Bean
        UserContextFilter userContextFilter() {
            return new UserContextFilter();
        }
    }

    private ResumeResponses.ResumeDetail detail() {
        return new ResumeResponses.ResumeDetail(1L, "My CV", true, "classic", 0, null, null, null, null,
                null, null, null, null, null, List.of(), List.of(), List.of(), List.of(), List.of(),
                List.of(), List.of(), null, null);
    }

    @Test
    void createRequiresAuth() throws Exception {
        mockMvc.perform(post("/api/resumes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"My CV\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createReturns201ForAuthenticatedUser() throws Exception {
        when(resumeService.create(eq(5L), org.mockito.ArgumentMatchers.any())).thenReturn(detail());

        mockMvc.perform(post("/api/resumes")
                        .header("X-User-Id", "5")
                        .header("X-User-Role", "JOB_SEEKER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"My CV\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.title").value("My CV"))
                .andExpect(jsonPath("$.data.isDefault").value(true));
    }
}
