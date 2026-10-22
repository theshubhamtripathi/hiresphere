package com.hiresphere.preference.service;

import com.hiresphere.common.event.JobPostedEvent;
import com.hiresphere.preference.domain.JobPreference;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Locale;

/** Decides whether a newly posted job matches a user's alert preferences. */
public final class PreferenceMatcher {

    private PreferenceMatcher() {
    }

    public static boolean matches(JobPreference pref, JobPostedEvent job) {
        boolean hasCriteria = !CollectionUtils.isEmpty(pref.getCategoryIds())
                || !CollectionUtils.isEmpty(pref.getDesiredTitles())
                || !CollectionUtils.isEmpty(pref.getLocations());
        if (!hasCriteria) {
            return false; // avoid alerting users who set no criteria
        }
        if (job.categoryId() != null && pref.getCategoryIds().contains(job.categoryId())) {
            return true;
        }
        if (titleMatches(pref, job.title())) {
            return true;
        }
        return locationMatches(pref, job.location());
    }

    private static boolean titleMatches(JobPreference pref, String title) {
        if (!StringUtils.hasText(title)) {
            return false;
        }
        String lower = title.toLowerCase(Locale.ROOT);
        return pref.getDesiredTitles().stream()
                .filter(StringUtils::hasText)
                .anyMatch(t -> lower.contains(t.toLowerCase(Locale.ROOT)));
    }

    private static boolean locationMatches(JobPreference pref, String location) {
        if (!StringUtils.hasText(location)) {
            return false;
        }
        String lower = location.toLowerCase(Locale.ROOT);
        return pref.getLocations().stream()
                .filter(StringUtils::hasText)
                .anyMatch(l -> lower.contains(l.toLowerCase(Locale.ROOT)));
    }
}
