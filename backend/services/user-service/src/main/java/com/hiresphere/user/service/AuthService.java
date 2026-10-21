package com.hiresphere.user.service;

import com.hiresphere.common.enums.Role;
import com.hiresphere.common.exception.ConflictException;
import com.hiresphere.common.exception.ForbiddenException;
import com.hiresphere.common.exception.UnauthorizedException;
import com.hiresphere.user.config.JwtProperties;
import com.hiresphere.user.domain.RefreshToken;
import com.hiresphere.user.domain.User;
import com.hiresphere.user.dto.AuthResponse;
import com.hiresphere.user.dto.LoginRequest;
import com.hiresphere.user.dto.RegisterRequest;
import com.hiresphere.user.dto.TokenRequest;
import com.hiresphere.user.dto.UserResponse;
import com.hiresphere.user.mapper.UserMapper;
import com.hiresphere.user.repository.RefreshTokenRepository;
import com.hiresphere.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final UserMapper userMapper;

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (request.role() == Role.ADMIN) {
            throw new ForbiddenException("Administrator accounts cannot be self-registered");
        }
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new ConflictException("An account with this email already exists");
        }
        User user = new User();
        user.setEmail(request.email().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFullName(request.fullName());
        user.setRole(request.role());
        user.setEnabled(true);
        return userMapper.toResponse(userRepository.save(user));
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }
        if (!user.isEnabled()) {
            throw new ForbiddenException("This account has been disabled");
        }
        user.setLastLoginAt(Instant.now());
        return issueTokens(user);
    }

    @Transactional
    public AuthResponse refresh(TokenRequest request) {
        String hash = TokenHasher.sha256Hex(request.refreshToken());
        RefreshToken token = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));
        if (!token.isActive()) {
            throw new UnauthorizedException("Refresh token is expired or revoked");
        }
        token.setRevoked(true); // rotation
        User user = userRepository.findById(token.getUserId())
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));
        if (!user.isEnabled()) {
            throw new ForbiddenException("This account has been disabled");
        }
        return issueTokens(user);
    }

    @Transactional
    public void logout(TokenRequest request) {
        String hash = TokenHasher.sha256Hex(request.refreshToken());
        refreshTokenRepository.findByTokenHash(hash).ifPresent(token -> token.setRevoked(true));
    }

    private AuthResponse issueTokens(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String rawRefresh = TokenHasher.randomToken();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setTokenHash(TokenHasher.sha256Hex(rawRefresh));
        refreshToken.setUserId(user.getId());
        refreshToken.setExpiresAt(Instant.now().plusMillis(jwtProperties.refreshTtl()));
        refreshTokenRepository.save(refreshToken);

        return AuthResponse.of(accessToken, rawRefresh, jwtService.accessTtlSeconds(),
                userMapper.toResponse(user));
    }
}
