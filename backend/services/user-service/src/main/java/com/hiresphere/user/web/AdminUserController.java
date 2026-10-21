package com.hiresphere.user.web;

import com.hiresphere.common.dto.ApiResponse;
import com.hiresphere.common.dto.PageResponse;
import com.hiresphere.common.enums.Role;
import com.hiresphere.common.exception.ForbiddenException;
import com.hiresphere.common.security.UserContextHolder;
import com.hiresphere.user.dto.UpdateUserStatusRequest;
import com.hiresphere.user.dto.UserResponse;
import com.hiresphere.user.dto.UserStatsResponse;
import com.hiresphere.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    @GetMapping
    public PageResponse<UserResponse> list(@RequestParam(required = false) Role role,
                                           @RequestParam(required = false) String q,
                                           @RequestParam(required = false) Boolean enabled,
                                           @PageableDefault(size = 20) Pageable pageable) {
        requireAdmin();
        Page<UserResponse> page = userService.search(role, enabled, q, pageable);
        return PageResponse.from(page);
    }

    @GetMapping("/stats")
    public ApiResponse<UserStatsResponse> stats() {
        requireAdmin();
        return ApiResponse.ok(userService.stats());
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<UserResponse> updateStatus(@PathVariable Long id,
                                                  @Valid @RequestBody UpdateUserStatusRequest request) {
        requireAdmin();
        return ApiResponse.ok(userService.updateStatus(id, request.enabled()));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        requireAdmin();
        userService.delete(id);
        return ApiResponse.message("User deleted");
    }

    private void requireAdmin() {
        if (!UserContextHolder.require().isAdmin()) {
            throw new ForbiddenException("Administrator access required");
        }
    }
}
