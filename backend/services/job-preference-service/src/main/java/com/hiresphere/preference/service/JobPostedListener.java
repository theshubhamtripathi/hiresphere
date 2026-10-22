package com.hiresphere.preference.service;

import com.hiresphere.common.event.JobAlertEvent;
import com.hiresphere.common.event.JobPostedEvent;
import com.hiresphere.common.event.Topics;
import com.hiresphere.preference.client.UserClient;
import com.hiresphere.preference.domain.JobPreference;
import com.hiresphere.preference.repository.JobPreferenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

/** Consumes job.posted, matches it against alert preferences and emits job.alert. */
@Slf4j
@Component
@RequiredArgsConstructor
public class JobPostedListener {

    private final JobPreferenceRepository preferenceRepository;
    private final UserClient userClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = Topics.JOB_POSTED, groupId = "job-preference-service")
    public void onJobPosted(JobPostedEvent event) {
        for (JobPreference pref : preferenceRepository.findByAlertsEnabledTrue()) {
            if (!PreferenceMatcher.matches(pref, event)) {
                continue;
            }
            UserClient.UserInfo user = userClient.getById(pref.getUserId());
            if (user == null) {
                continue;
            }
            JobAlertEvent alert = new JobAlertEvent(pref.getUserId(), user.email(), event.jobId(),
                    event.title(), null, Instant.now());
            kafkaTemplate.send(Topics.JOB_ALERT, String.valueOf(pref.getUserId()), alert);
            log.debug("Emitted job.alert to user {} for job {}", pref.getUserId(), event.jobId());
        }
    }
}
