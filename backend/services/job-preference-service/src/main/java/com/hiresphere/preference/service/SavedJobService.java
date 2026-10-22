package com.hiresphere.preference.service;

import com.hiresphere.preference.client.JobClient;
import com.hiresphere.preference.domain.SavedJob;
import com.hiresphere.preference.dto.PreferenceDtos.SavedJobResponse;
import com.hiresphere.preference.repository.SavedJobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class SavedJobService {

    private final SavedJobRepository savedJobRepository;
    private final JobClient jobClient;

    @Transactional
    public void save(Long userId, Long jobId) {
        if (!savedJobRepository.existsByUserIdAndJobId(userId, jobId)) {
            SavedJob saved = new SavedJob();
            saved.setUserId(userId);
            saved.setJobId(jobId);
            savedJobRepository.save(saved);
        }
    }

    @Transactional
    public void unsave(Long userId, Long jobId) {
        savedJobRepository.deleteByUserIdAndJobId(userId, jobId);
    }

    @Transactional(readOnly = true)
    public List<Long> savedIds(Long userId) {
        return savedJobRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(SavedJob::getJobId).toList();
    }

    @Transactional(readOnly = true)
    public List<SavedJobResponse> listSaved(Long userId) {
        List<SavedJob> saved = savedJobRepository.findByUserIdOrderByCreatedAtDesc(userId);
        if (saved.isEmpty()) {
            return List.of();
        }
        Map<Long, JobClient.JobSummary> jobs = jobClient
                .batch(saved.stream().map(SavedJob::getJobId).toList()).stream()
                .collect(java.util.stream.Collectors.toMap(JobClient.JobSummary::id, Function.identity()));

        return saved.stream()
                .filter(s -> jobs.containsKey(s.getJobId()))
                .map(s -> {
                    JobClient.JobSummary j = jobs.get(s.getJobId());
                    return new SavedJobResponse(s.getId(), j.id(), j.title(), j.companyId(), j.location(),
                            j.jobType(), j.workMode(), j.salaryMin(), j.salaryMax(), j.currency(),
                            j.status(), s.getCreatedAt());
                })
                .toList();
    }
}
