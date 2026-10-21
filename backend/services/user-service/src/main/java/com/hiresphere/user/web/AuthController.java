package com.hiresphere.user.web;

import com.hiresphere.common.dto.ApiResponse;
import com.hiresphere.user.dto.AuthResponse;
import com.hiresphere.user.dto.LoginRequest;
import com.hiresphere.user.dto.RegisterRequest;
import com.hiresphere.user.dto.TokenRequest;
import com.hiresphere.user.dto.UserResponse;
import com.hiresphere.user.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse user = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Registration successful", user));
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthResponse> refresh(@Valid @RequestBody TokenRequest request) {
        return ApiResponse.ok(authService.refresh(request));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@Valid @RequestBody TokenRequest request) {
        authService.logout(request);
        return ApiResponse.message("Logged out");
    }
}
