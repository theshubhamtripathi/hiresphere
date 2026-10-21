package com.hiresphere.user.service;

import com.hiresphere.common.enums.Role;
import com.hiresphere.common.exception.ConflictException;
import com.hiresphere.common.exception.ForbiddenException;
import com.hiresphere.common.exception.UnauthorizedException;
import com.hiresphere.user.config.JwtProperties;
import com.hiresphere.user.domain.User;
import com.hiresphere.user.dto.LoginRequest;
import com.hiresphere.user.dto.RegisterRequest;
import com.hiresphere.user.dto.UserResponse;
import com.hiresphere.user.mapper.UserMapper;
import com.hiresphere.user.repository.RefreshTokenRepository;
import com.hiresphere.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock RefreshTokenRepository refreshTokenRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtService jwtService;
    @Mock UserMapper userMapper;

    AuthService authService;

    @BeforeEach
    void setUp() {
        JwtProperties props = new JwtProperties("secret", 900000, 604800000, "hiresphere");
        authService = new AuthService(userRepository, refreshTokenRepository, passwordEncoder,
                jwtService, props, userMapper);
    }

    private User enabledUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail("a@x.com");
        user.setPasswordHash("hashed");
        user.setFullName("Alice");
        user.setRole(Role.JOB_SEEKER);
        user.setEnabled(true);
        return user;
    }

    @Test
    void registerRejectsAdminRole() {
        RegisterRequest request = new RegisterRequest("a@x.com", "password1", "Alice", Role.ADMIN);
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void registerRejectsDuplicateEmail() {
        when(userRepository.existsByEmailIgnoreCase("a@x.com")).thenReturn(true);
        RegisterRequest request = new RegisterRequest("a@x.com", "password1", "Alice", Role.JOB_SEEKER);
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void loginWithWrongPasswordIsUnauthorized() {
        when(userRepository.findByEmailIgnoreCase("a@x.com")).thenReturn(Optional.of(enabledUser()));
        when(passwordEncoder.matches("bad", "hashed")).thenReturn(false);
        assertThatThrownBy(() -> authService.login(new LoginRequest("a@x.com", "bad")))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void loginWithDisabledAccountIsForbidden() {
        User user = enabledUser();
        user.setEnabled(false);
        when(userRepository.findByEmailIgnoreCase("a@x.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password1", "hashed")).thenReturn(true);
        assertThatThrownBy(() -> authService.login(new LoginRequest("a@x.com", "password1")))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void loginIssuesTokens() {
        User user = enabledUser();
        when(userRepository.findByEmailIgnoreCase("a@x.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password1", "hashed")).thenReturn(true);
        when(jwtService.generateAccessToken(user)).thenReturn("access-token");
        lenient().when(jwtService.accessTtlSeconds()).thenReturn(900L);
        when(userMapper.toResponse(any(User.class))).thenReturn(
                new UserResponse(1L, "a@x.com", "Alice", null, Role.JOB_SEEKER, null, null, null, true, null, null));

        var response = authService.login(new LoginRequest("a@x.com", "password1"));

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isNotBlank();
        assertThat(response.user().email()).isEqualTo("a@x.com");
        assertThat(user.getLastLoginAt()).isNotNull();
    }
}
