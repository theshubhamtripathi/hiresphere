package com.hiresphere.job.service;

import com.hiresphere.common.event.JobPostedEvent;
import com.hiresphere.common.event.Topics;
import com.hiresphere.job.domain.Job;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishJobPosted(Job job) {
        JobPostedEvent event = new JobPostedEvent(
                job.getId(), job.getCompanyId(), job.getTitle(),
                job.getCategoryId(), job.getLocation(), Instant.now());
        kafkaTemplate.send(Topics.JOB_POSTED, String.valueOf(job.getId()), event);
        log.debug("Published job.posted for job {}", job.getId());
    }
}
