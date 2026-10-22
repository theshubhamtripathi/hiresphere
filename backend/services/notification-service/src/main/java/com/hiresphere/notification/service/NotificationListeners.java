package com.hiresphere.notification.service;

import com.hiresphere.common.event.ApplicationStatusChangedEvent;
import com.hiresphere.common.event.ApplicationSubmittedEvent;
import com.hiresphere.common.event.JobAlertEvent;
import com.hiresphere.common.event.PaymentCompletedEvent;
import com.hiresphere.common.event.Topics;
import com.hiresphere.common.event.UserRegisteredEvent;
import com.hiresphere.notification.client.DirectoryClient;
import com.hiresphere.notification.domain.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/** Consumes platform events and produces in-app notifications plus emails. */
@Component
@RequiredArgsConstructor
public class NotificationListeners {

    private final NotificationService notificationService;
    private final EmailService emailService;
    private final DirectoryClient directoryClient;

    @Value("${hiresphere.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    @KafkaListener(topics = Topics.USER_REGISTERED, groupId = "notification-service")
    public void onUserRegistered(UserRegisteredEvent event) {
        boolean created = notificationService.notify("welcome:" + event.userId(), event.userId(),
                NotificationType.WELCOME, "Welcome to HireSphere",
                "Your account is ready. Start exploring opportunities.", frontendUrl).isPresent();
        if (created) {
            emailService.send(event.email(), "Welcome to HireSphere", "welcome",
                    Map.of("name", safe(event.fullName()), "url", frontendUrl));
        }
    }

    @KafkaListener(topics = Topics.APPLICATION_SUBMITTED, groupId = "notification-service")
    public void onApplicationSubmitted(ApplicationSubmittedEvent event) {
        // Applicant confirmation
        boolean seeker = notificationService.notify("app-received-seeker:" + event.applicationId(),
                event.applicantId(), NotificationType.APPLICATION_RECEIVED, "Application submitted",
                "Your application for " + event.jobTitle() + " was received.",
                frontendUrl + "/applications").isPresent();
        if (seeker) {
            emailService.send(event.applicantEmail(), "Application received — " + event.jobTitle(),
                    "application-received", Map.of("name", safe(event.applicantName()),
                            "jobTitle", safe(event.jobTitle()), "forEmployer", false,
                            "url", frontendUrl + "/applications"));
        }
        // Employer notification
        Long ownerId = directoryClient.companyOwnerUserId(event.companyId());
        if (ownerId != null) {
            boolean employer = notificationService.notify("app-received-employer:" + event.applicationId(),
                    ownerId, NotificationType.APPLICATION_RECEIVED, "New application",
                    event.applicantName() + " applied for " + event.jobTitle() + ".",
                    frontendUrl + "/employer/applications").isPresent();
            if (employer) {
                DirectoryClient.UserInfo owner = directoryClient.user(ownerId);
                if (owner != null) {
                    emailService.send(owner.email(), "New application — " + event.jobTitle(),
                            "application-received", Map.of("name", safe(owner.fullName()),
                                    "jobTitle", safe(event.jobTitle()), "forEmployer", true,
                                    "applicant", safe(event.applicantName()),
                                    "url", frontendUrl + "/employer/applications"));
                }
            }
        }
    }

    @KafkaListener(topics = Topics.APPLICATION_STATUS_CHANGED, groupId = "notification-service")
    public void onStatusChanged(ApplicationStatusChangedEvent event) {
        String status = event.newStatus().name();
        boolean created = notificationService.notify(
                "app-status:" + event.applicationId() + ":" + status, event.applicantId(),
                NotificationType.APPLICATION_STATUS, "Application update",
                "Your application for " + event.jobTitle() + " is now " + prettify(status) + ".",
                frontendUrl + "/applications").isPresent();
        if (created) {
            emailService.send(event.applicantEmail(), "Update on your application — " + event.jobTitle(),
                    "application-status", Map.of("name", safe(event.applicantName()),
                            "jobTitle", safe(event.jobTitle()), "status", prettify(status),
                            "url", frontendUrl + "/applications"));
        }
    }

    @KafkaListener(topics = Topics.JOB_ALERT, groupId = "notification-service")
    public void onJobAlert(JobAlertEvent event) {
        boolean created = notificationService.notify("job-alert:" + event.userId() + ":" + event.jobId(),
                event.userId(), NotificationType.JOB_ALERT, "New job matching your preferences",
                event.jobTitle() + " was just posted.", frontendUrl + "/jobs/" + event.jobId()).isPresent();
        if (created) {
            emailService.send(event.email(), "New job alert — " + event.jobTitle(), "job-alert",
                    Map.of("jobTitle", safe(event.jobTitle()),
                            "url", frontendUrl + "/jobs/" + event.jobId()));
        }
    }

    @KafkaListener(topics = Topics.PAYMENT_COMPLETED, groupId = "notification-service")
    public void onPaymentCompleted(PaymentCompletedEvent event) {
        Long ownerId = directoryClient.companyOwnerUserId(event.companyId());
        notificationService.notify("payment:" + event.orderId(), ownerId,
                NotificationType.PAYMENT_RECEIPT, "Payment successful",
                "Your payment for the " + event.planCode() + " plan was received.",
                frontendUrl + "/employer/billing");
        emailService.send(event.ownerEmail(), "Payment receipt — HireSphere", "payment-receipt",
                Map.of("plan", safe(event.planCode()), "amount", String.valueOf(event.amount()),
                        "currency", safe(event.currency()), "url", frontendUrl + "/employer/billing"));
    }

    private String prettify(String status) {
        return status.charAt(0) + status.substring(1).toLowerCase().replace('_', ' ');
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }
}
