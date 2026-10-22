package com.hiresphere.ai.web;

import com.hiresphere.ai.dto.AiResults;
import com.hiresphere.ai.service.AiService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AiController.class)
@Import({GlobalExceptionHandler.class, AiControllerTest.FilterConfig.class})
class AiControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean AiService aiService;

    @TestConfiguration
    static class FilterConfig {
        @Bean
        UserContextFilter userContextFilter() {
            return new UserContextFilter();
        }
    }

    @Test
    void summaryRequiresAuthentication() throws Exception {
        mockMvc.perform(post("/api/ai/resume/summary")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"Engineer\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void summaryReturnsGeneratedText() throws Exception {
        when(aiService.summary(eq(5L), any())).thenReturn(new AiResults.TextResult("A great summary"));

        mockMvc.perform(post("/api/ai/resume/summary")
                        .header("X-User-Id", "5").header("X-User-Role", "JOB_SEEKER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"Engineer\",\"skills\":[\"Java\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.text").value("A great summary"));
    }
}
