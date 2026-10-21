package com.hiresphere.user.web;

import com.hiresphere.common.enums.Role;
import com.hiresphere.common.exception.ConflictException;
import com.hiresphere.common.exception.GlobalExceptionHandler;
import com.hiresphere.user.dto.UserResponse;
import com.hiresphere.user.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(GlobalExceptionHandler.class)
class AuthControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean AuthService authService;

    private static final String VALID_BODY = """
            {"email":"a@x.com","password":"password1","fullName":"Alice","role":"JOB_SEEKER"}""";

    @Test
    void registerReturns201() throws Exception {
        var user = new UserResponse(1L, "a@x.com", "Alice", null, Role.JOB_SEEKER, null, null, null, true, null, null);
        when(authService.register(any())).thenReturn(user);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("a@x.com"));
    }

    @Test
    void registerWithInvalidBodyReturns400() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"not-an-email\",\"password\":\"x\",\"fullName\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void duplicateRegistrationReturns409() throws Exception {
        when(authService.register(any())).thenThrow(new ConflictException("exists"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isConflict());
    }
}
