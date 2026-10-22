package com.hiresphere.preference.service;

import com.hiresphere.common.event.JobPostedEvent;
import com.hiresphere.preference.domain.JobPreference;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PreferenceMatcherTest {

    private JobPostedEvent job(Long categoryId, String title, String location) {
        return new JobPostedEvent(1L, 2L, title, categoryId, location, Instant.now());
    }

    private JobPreference pref(List<Long> cats, List<String> titles, List<String> locations) {
        JobPreference p = new JobPreference();
        p.setCategoryIds(cats);
        p.setDesiredTitles(titles);
        p.setLocations(locations);
        return p;
    }

    @Test
    void matchesByCategory() {
        assertThat(PreferenceMatcher.matches(pref(List.of(5L), List.of(), List.of()),
                job(5L, "Anything", "Anywhere"))).isTrue();
    }

    @Test
    void matchesByTitleSubstring() {
        assertThat(PreferenceMatcher.matches(pref(List.of(), List.of("java"), List.of()),
                job(9L, "Senior Java Engineer", "Remote"))).isTrue();
    }

    @Test
    void matchesByLocation() {
        assertThat(PreferenceMatcher.matches(pref(List.of(), List.of(), List.of("remote")),
                job(9L, "Designer", "Fully Remote"))).isTrue();
    }

    @Test
    void doesNotMatchWhenNoCriteria() {
        assertThat(PreferenceMatcher.matches(pref(List.of(), List.of(), List.of()),
                job(5L, "Java", "Remote"))).isFalse();
    }

    @Test
    void doesNotMatchUnrelatedJob() {
        assertThat(PreferenceMatcher.matches(pref(List.of(5L), List.of("python"), List.of("berlin")),
                job(9L, "Java Engineer", "Remote"))).isFalse();
    }
}
