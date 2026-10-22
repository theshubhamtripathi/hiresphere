package com.hiresphere.analytics.service;

import com.hiresphere.analytics.domain.MetricCounter;
import com.hiresphere.common.event.ApplicationStatusChangedEvent;
import com.hiresphere.common.event.ApplicationSubmittedEvent;
import com.hiresphere.common.event.JobPostedEvent;
import com.hiresphere.common.event.PaymentCompletedEvent;
import com.hiresphere.common.event.Topics;
import com.hiresphere.common.event.UserRegisteredEvent;
import com.hiresphere.common.enums.ApplicationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/** Feeds dashboard counters from the platform event stream. */
@Component
@RequiredArgsConstructor
public class AnalyticsListeners {

    private static final long PLATFORM = MetricCounter.PLATFORM;

    private final MetricIngestService ingest;

    @KafkaListener(topics = Topics.USER_REGISTERED, groupId = "analytics-service")
    public void onUserRegistered(UserRegisteredEvent event) {
        ingest.addMetric(PLATFORM, MetricIngestService.SIGNUPS, 1);
        ingest.incrementRole(event.role() == null ? "UNKNOWN" : event.role().name());
    }

    @KafkaListener(topics = Topics.JOB_POSTED, groupId = "analytics-service")
    public void onJobPosted(JobPostedEvent event) {
        ingest.addMetric(PLATFORM, MetricIngestService.JOBS, 1);
        ingest.addMetric(event.companyId(), MetricIngestService.JOBS, 1);
        ingest.ensureJob(event.jobId(), event.companyId(), event.title());
    }

    @KafkaListener(topics = Topics.APPLICATION_SUBMITTED, groupId = "analytics-service")
    public void onApplicationSubmitted(ApplicationSubmittedEvent event) {
        ingest.addMetric(PLATFORM, MetricIngestService.APPLICATIONS, 1);
        ingest.addMetric(event.companyId(), MetricIngestService.APPLICATIONS, 1);
        ingest.adjustStatus(PLATFORM, ApplicationStatus.APPLIED.name(), 1);
        ingest.adjustStatus(event.companyId(), ApplicationStatus.APPLIED.name(), 1);
        ingest.incrementJobApplications(event.jobId(), event.companyId(), event.jobTitle());
    }

    @KafkaListener(topics = Topics.APPLICATION_STATUS_CHANGED, groupId = "analytics-service")
    public void onStatusChanged(ApplicationStatusChangedEvent event) {
        if (event.oldStatus() != null) {
            ingest.adjustStatus(PLATFORM, event.oldStatus().name(), -1);
            ingest.adjustStatus(event.companyId(), event.oldStatus().name(), -1);
        }
        ingest.adjustStatus(PLATFORM, event.newStatus().name(), 1);
        ingest.adjustStatus(event.companyId(), event.newStatus().name(), 1);
    }

    @KafkaListener(topics = Topics.PAYMENT_COMPLETED, groupId = "analytics-service")
    public void onPaymentCompleted(PaymentCompletedEvent event) {
        long amount = event.amount() == null ? 0 : event.amount().longValue();
        ingest.addMetric(PLATFORM, MetricIngestService.REVENUE, amount);
    }
}
