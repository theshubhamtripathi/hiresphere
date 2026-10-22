package com.hiresphere.preference.service;

import com.hiresphere.preference.domain.AlertFrequency;
import com.hiresphere.preference.domain.JobPreference;
import com.hiresphere.preference.dto.PreferenceDtos.PreferenceRequest;
import com.hiresphere.preference.dto.PreferenceDtos.PreferenceResponse;
import com.hiresphere.preference.repository.JobPreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PreferenceService {

    private final JobPreferenceRepository repository;

    @Transactional(readOnly = true)
    public PreferenceResponse get(Long userId) {
        return repository.findByUserId(userId).map(this::toResponse)
                .orElseGet(() -> emptyResponse(userId));
    }

    @Transactional
    public PreferenceResponse update(Long userId, PreferenceRequest req) {
        JobPreference pref = repository.findByUserId(userId).orElseGet(() -> {
            JobPreference p = new JobPreference();
            p.setUserId(userId);
            return p;
        });
        pref.setDesiredTitles(orEmpty(req.desiredTitles()));
        pref.setLocations(orEmpty(req.locations()));
        pref.setJobTypes(orEmpty(req.jobTypes()));
        pref.setWorkModes(orEmpty(req.workModes()));
        pref.setMinSalary(req.minSalary());
        pref.setCategoryIds(orEmpty(req.categoryIds()));
        pref.setSkills(orEmpty(req.skills()));
        pref.setAlertsEnabled(req.alertsEnabled() == null || req.alertsEnabled());
        pref.setAlertFrequency(req.alertFrequency() == null ? AlertFrequency.INSTANT : req.alertFrequency());
        return toResponse(repository.save(pref));
    }

    private <T> List<T> orEmpty(List<T> list) {
        return list == null ? new ArrayList<>() : list;
    }

    private PreferenceResponse toResponse(JobPreference p) {
        return new PreferenceResponse(p.getUserId(), p.getDesiredTitles(), p.getLocations(),
                p.getJobTypes(), p.getWorkModes(), p.getMinSalary(), p.getCategoryIds(), p.getSkills(),
                p.isAlertsEnabled(), p.getAlertFrequency());
    }

    private PreferenceResponse emptyResponse(Long userId) {
        return new PreferenceResponse(userId, List.of(), List.of(), List.of(), List.of(), null,
                List.of(), List.of(), true, AlertFrequency.INSTANT);
    }
}
