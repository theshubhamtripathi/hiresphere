package com.hiresphere.job.service;

import com.hiresphere.job.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/** Closes OPEN jobs whose deadline has passed. */
@Slf4j
@Component
@RequiredArgsConstructor
public class JobExpiryScheduler {

    private final JobRepository jobRepository;

    @Scheduled(cron = "${hiresphere.jobs.expiry-cron:0 0 * * * *}")
    @Transactional
    public void expireJobs() {
        int expired = jobRepository.expirePastDeadline(LocalDate.now());
        if (expired > 0) {
            log.info("Expired {} job(s) past their deadline", expired);
        }
    }
}
