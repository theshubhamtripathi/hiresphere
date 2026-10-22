package com.hiresphere.notification.service;

import com.hiresphere.common.exception.ForbiddenException;
import com.hiresphere.common.exception.ResourceNotFoundException;
import com.hiresphere.notification.domain.Notification;
import com.hiresphere.notification.domain.NotificationType;
import com.hiresphere.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository repository;

    /** Creates an in-app notification unless one with the same event key already exists. */
    @Transactional
    public Optional<Notification> notify(String eventKey, Long userId, NotificationType type,
                                         String title, String message, String link) {
        if (userId == null || repository.existsByEventKey(eventKey)) {
            return Optional.empty();
        }
        Notification n = new Notification();
        n.setUserId(userId);
        n.setType(type);
        n.setTitle(title);
        n.setMessage(message);
        n.setLink(link);
        n.setEventKey(eventKey);
        try {
            return Optional.of(repository.save(n));
        } catch (DataIntegrityViolationException duplicate) {
            return Optional.empty(); // concurrent delivery of the same event
        }
    }

    @Transactional(readOnly = true)
    public Page<Notification> list(Long userId, Pageable pageable) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    @Transactional(readOnly = true)
    public long unreadCount(Long userId) {
        return repository.countByUserIdAndReadFalse(userId);
    }

    @Transactional
    public void markRead(Long id, Long userId) {
        Notification n = repository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Notification", id));
        if (!n.getUserId().equals(userId)) {
            throw new ForbiddenException("Not your notification");
        }
        n.setRead(true);
    }

    @Transactional
    public void markAllRead(Long userId) {
        repository.markAllRead(userId);
    }
}
