package com.hiresphere.user.service;

import com.hiresphere.common.enums.Role;
import com.hiresphere.common.exception.BadRequestException;
import com.hiresphere.common.exception.ConflictException;
import com.hiresphere.common.exception.ResourceNotFoundException;
import com.hiresphere.user.domain.User;
import com.hiresphere.user.dto.ChangePasswordRequest;
import com.hiresphere.user.dto.UpdateProfileRequest;
import com.hiresphere.user.dto.UserResponse;
import com.hiresphere.user.dto.UserStatsResponse;
import com.hiresphere.user.mapper.UserMapper;
import com.hiresphere.user.repository.RefreshTokenRepository;
import com.hiresphere.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public UserResponse getById(Long id) {
        return userMapper.toResponse(find(id));
    }

    @Transactional
    public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = find(userId);
        if (StringUtils.hasText(request.fullName())) {
            user.setFullName(request.fullName());
        }
        user.setPhone(request.phone());
        user.setAvatarUrl(request.avatarUrl());
        user.setHeadline(request.headline());
        user.setLocation(request.location());
        return userMapper.toResponse(user);
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = find(userId);
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Current password is incorrect");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        refreshTokenRepository.revokeAllForUser(userId); // force re-login elsewhere
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> search(Role role, Boolean enabled, String q, Pageable pageable) {
        String pattern = StringUtils.hasText(q)
                ? "%" + q.trim().toLowerCase() + "%"
                : "%";
        return userRepository.search(role, enabled, pattern, pageable).map(userMapper::toResponse);
    }

    @Transactional
    public UserResponse updateStatus(Long id, boolean enabled) {
        User user = find(id);
        if (user.getRole() == Role.ADMIN && !enabled) {
            throw new ConflictException("Administrator accounts cannot be disabled");
        }
        user.setEnabled(enabled);
        if (!enabled) {
            refreshTokenRepository.revokeAllForUser(id);
        }
        return userMapper.toResponse(user);
    }

    @Transactional
    public void delete(Long id) {
        User user = find(id);
        if (user.getRole() == Role.ADMIN) {
            throw new ConflictException("Administrator accounts cannot be deleted");
        }
        refreshTokenRepository.revokeAllForUser(id);
        userRepository.delete(user);
    }

    @Transactional(readOnly = true)
    public UserStatsResponse stats() {
        return new UserStatsResponse(
                userRepository.count(),
                userRepository.countByRole(Role.JOB_SEEKER),
                userRepository.countByRole(Role.EMPLOYER),
                userRepository.countByRole(Role.ADMIN));
    }

    private User find(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("User", id));
    }
}
