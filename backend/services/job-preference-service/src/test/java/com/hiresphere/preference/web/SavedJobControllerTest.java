package com.hiresphere.preference.web;

import com.hiresphere.common.exception.GlobalExceptionHandler;
import com.hiresphere.common.security.UserContextFilter;
import com.hiresphere.preference.service.SavedJobService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SavedJobController.class)
@Import({GlobalExceptionHandler.class, SavedJobControllerTest.FilterConfig.class})
class SavedJobControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean SavedJobService savedJobService;

    @TestConfiguration
    static class FilterConfig {
        @Bean
        UserContextFilter userContextFilter() {
            return new UserContextFilter();
        }
    }

    @Test
    void savingWithoutAuthIsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/saved-jobs/10")).andExpect(status().isUnauthorized());
    }

    @Test
    void savingWithAuthSucceeds() throws Exception {
        mockMvc.perform(post("/api/saved-jobs/10")
                        .header("X-User-Id", "5").header("X-User-Role", "JOB_SEEKER"))
                .andExpect(status().isOk());
        Mockito.verify(savedJobService).save(5L, 10L);
    }
}
