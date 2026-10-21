package com.hiresphere.user.web;

import com.hiresphere.user.dto.UserResponse;
import com.hiresphere.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Service-to-service lookups. Never exposed through the gateway. */
@RestController
@RequestMapping("/api/internal/users")
@RequiredArgsConstructor
public class InternalUserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public UserResponse getById(@PathVariable Long id) {
        return userService.getById(id);
    }
}
