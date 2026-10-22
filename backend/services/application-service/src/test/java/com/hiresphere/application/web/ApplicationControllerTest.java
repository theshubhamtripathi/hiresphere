package com.hiresphere.application.web;

import com.hiresphere.application.service.ApplicationService;
import com.hiresphere.common.exception.GlobalExceptionHandler;
import com.hiresphere.common.security.UserContextFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ApplicationController.class)
@Import({GlobalExceptionHandler.class, ApplicationControllerTest.FilterConfig.class})
class ApplicationControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean ApplicationService applicationService;

    @TestConfiguration
    static class FilterConfig {
        @Bean
        UserContextFilter userContextFilter() {
            return new UserContextFilter();
        }
    }

    private static final String BODY = "{\"jobId\":10,\"resumeId\":2}";

    @Test
    void applyingWithoutAuthIsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/applications")
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void employerCannotApply() throws Exception {
        mockMvc.perform(post("/api/applications")
                        .header("X-User-Id", "7").header("X-User-Role", "EMPLOYER")
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isForbidden());
    }
}
