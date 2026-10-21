package com.hiresphere.common.exception;

import com.hiresphere.common.dto.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    private MockHttpServletRequest request(String uri) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", uri);
        request.setRequestURI(uri);
        return request;
    }

    @Test
    void mapsResourceNotFoundTo404() {
        ResponseEntity<ErrorResponse> response =
                handler.handleApi(ResourceNotFoundException.of("Job", 7), request("/api/jobs/7"));

        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().error()).isEqualTo("Not Found");
        assertThat(response.getBody().path()).isEqualTo("/api/jobs/7");
        assertThat(response.getBody().message()).contains("Job not found with id 7");
    }

    @Test
    void mapsConflictTo409() {
        ResponseEntity<ErrorResponse> response =
                handler.handleApi(new ConflictException("Already exists"), request("/api/x"));

        assertThat(response.getStatusCode().value()).isEqualTo(409);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(409);
    }

    @Test
    void mapsBadInputTo400() {
        ResponseEntity<ErrorResponse> response =
                handler.handleBadInput(new IllegalArgumentException("bad value"), request("/api/x"));

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("bad value");
    }

    @Test
    void mapsValidationErrorsTo400WithFieldErrors() throws Exception {
        Method method = Sample.class.getDeclaredMethod("handle", String.class);
        MethodParameter parameter = new MethodParameter(method, 0);
        BeanPropertyBindingResult binding = new BeanPropertyBindingResult(new Object(), "payload");
        binding.addError(new FieldError("payload", "name", "must not be blank"));
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, binding);

        ResponseEntity<ErrorResponse> response = handler.handleValidation(ex, request("/api/x"));

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().fieldErrors()).hasSize(1);
        assertThat(response.getBody().fieldErrors().getFirst().field()).isEqualTo("name");
        assertThat(response.getBody().fieldErrors().getFirst().message()).isEqualTo("must not be blank");
    }

    @SuppressWarnings("unused")
    static class Sample {
        void handle(String name) {
        }
    }
}
