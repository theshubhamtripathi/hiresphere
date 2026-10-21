package com.hiresphere.company.web;

import com.hiresphere.common.enums.Role;
import com.hiresphere.common.exception.GlobalExceptionHandler;
import com.hiresphere.common.security.UserContextFilter;
import com.hiresphere.company.domain.CompanyStatus;
import com.hiresphere.company.dto.CompanyResponse;
import com.hiresphere.company.service.CompanyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CompanyController.class)
@Import({GlobalExceptionHandler.class, CompanyControllerTest.FilterConfig.class})
class CompanyControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean CompanyService companyService;

    @TestConfiguration
    static class FilterConfig {
        @Bean
        UserContextFilter userContextFilter() {
            return new UserContextFilter();
        }
    }

    @Test
    void getBySlugReturnsCompany() throws Exception {
        var company = new CompanyResponse(1L, "Acme", "acme", null, null, null, null, null, null, null,
                null, true, CompanyStatus.ACTIVE, 7L, null);
        when(companyService.getByIdOrSlug("acme")).thenReturn(company);

        mockMvc.perform(get("/api/companies/acme"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.slug").value("acme"));
    }

    @Test
    void createWithoutAuthIsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Acme\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createAsJobSeekerIsForbidden() throws Exception {
        mockMvc.perform(post("/api/companies")
                        .header("X-User-Id", "7")
                        .header("X-User-Role", Role.JOB_SEEKER.name())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Acme\"}"))
                .andExpect(status().isForbidden());
    }
}
