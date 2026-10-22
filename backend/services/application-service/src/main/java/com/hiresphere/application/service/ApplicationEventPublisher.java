package com.hiresphere.application.service;

import com.hiresphere.application.client.UserClient;
import com.hiresphere.application.domain.JobApplication;
import com.hiresphere.common.enums.ApplicationStatus;
import com.hiresphere.common.event.ApplicationStatusChangedEvent;
import com.hiresphere.common.event.ApplicationSubmittedEvent;
import com.hiresphere.common.event.Topics;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class ApplicationEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishSubmitted(JobApplication app, String jobTitle, UserClient.UserInfo applicant) {
        ApplicationSubmittedEvent event = new ApplicationSubmittedEvent(
                app.getId(), app.getJobId(), jobTitle, app.getApplicantId(),
                applicant == null ? null : applicant.email(),
                applicant == null ? null : applicant.fullName(),
                app.getCompanyId(), Instant.now());
        kafkaTemplate.send(Topics.APPLICATION_SUBMITTED, String.valueOf(app.getId()), event);
    }

    public void publishStatusChanged(JobApplication app, String jobTitle, UserClient.UserInfo applicant,
                                     ApplicationStatus oldStatus, ApplicationStatus newStatus) {
        ApplicationStatusChangedEvent event = new ApplicationStatusChangedEvent(
                app.getId(), app.getJobId(), jobTitle, app.getApplicantId(),
                applicant == null ? null : applicant.email(),
                applicant == null ? null : applicant.fullName(),
                app.getCompanyId(), oldStatus, newStatus, Instant.now());
        kafkaTemplate.send(Topics.APPLICATION_STATUS_CHANGED, String.valueOf(app.getId()), event);
    }
}
