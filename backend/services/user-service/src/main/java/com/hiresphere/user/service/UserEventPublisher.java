package com.hiresphere.user.service;

import com.hiresphere.common.event.Topics;
import com.hiresphere.common.event.UserRegisteredEvent;
import com.hiresphere.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class UserEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishRegistered(User user) {
        UserRegisteredEvent event = new UserRegisteredEvent(
                user.getId(), user.getEmail(), user.getFullName(), user.getRole(), Instant.now());
        kafkaTemplate.send(Topics.USER_REGISTERED, String.valueOf(user.getId()), event);
    }
}
