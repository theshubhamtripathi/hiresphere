package com.hiresphere.notification.web;

import com.hiresphere.common.dto.ApiResponse;
import com.hiresphere.common.dto.PageResponse;
import com.hiresphere.common.security.UserContextHolder;
import com.hiresphere.notification.dto.NotificationResponse;
import com.hiresphere.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    private Long uid() {
        return UserContextHolder.currentUserId();
    }

    @GetMapping
    public PageResponse<NotificationResponse> list(@PageableDefault(size = 20) Pageable pageable) {
        return PageResponse.from(notificationService.list(uid(), pageable).map(NotificationResponse::from));
    }

    @GetMapping("/unread-count")
    public ApiResponse<Map<String, Long>> unreadCount() {
        return ApiResponse.ok(Map.of("count", notificationService.unreadCount(uid())));
    }

    @PatchMapping("/{id}/read")
    public ApiResponse<Void> markRead(@PathVariable Long id) {
        notificationService.markRead(id, uid());
        return ApiResponse.message("Marked as read");
    }

    @PatchMapping("/read-all")
    public ApiResponse<Void> markAllRead() {
        notificationService.markAllRead(uid());
        return ApiResponse.message("All marked as read");
    }
}
