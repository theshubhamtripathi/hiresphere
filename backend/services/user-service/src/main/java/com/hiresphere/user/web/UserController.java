package com.hiresphere.user.web;

import com.hiresphere.common.dto.ApiResponse;
import com.hiresphere.common.security.UserContextHolder;
import com.hiresphere.user.dto.ChangePasswordRequest;
import com.hiresphere.user.dto.UpdateProfileRequest;
import com.hiresphere.user.dto.UserResponse;
import com.hiresphere.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ApiResponse<UserResponse> me() {
        return ApiResponse.ok(userService.getById(UserContextHolder.currentUserId()));
    }

    @PutMapping("/me")
    public ApiResponse<UserResponse> updateMe(@Valid @RequestBody UpdateProfileRequest request) {
        return ApiResponse.ok(userService.updateProfile(UserContextHolder.currentUserId(), request));
    }

    @PutMapping("/me/password")
    public ApiResponse<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(UserContextHolder.currentUserId(), request);
        return ApiResponse.message("Password updated");
    }
}
