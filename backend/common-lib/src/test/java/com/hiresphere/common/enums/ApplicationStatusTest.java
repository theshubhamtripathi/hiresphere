package com.hiresphere.common.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationStatusTest {

    @Test
    void appliedCanMoveToReviewOrReject() {
        assertThat(ApplicationStatus.APPLIED.canTransitionTo(ApplicationStatus.UNDER_REVIEW)).isTrue();
        assertThat(ApplicationStatus.APPLIED.canTransitionTo(ApplicationStatus.REJECTED)).isTrue();
        assertThat(ApplicationStatus.APPLIED.canTransitionTo(ApplicationStatus.HIRED)).isFalse();
    }

    @Test
    void offeredCanOnlyBeHiredRejectedOrWithdrawn() {
        assertThat(ApplicationStatus.OFFERED.canTransitionTo(ApplicationStatus.HIRED)).isTrue();
        assertThat(ApplicationStatus.OFFERED.canTransitionTo(ApplicationStatus.REJECTED)).isTrue();
        assertThat(ApplicationStatus.OFFERED.canTransitionTo(ApplicationStatus.INTERVIEW)).isFalse();
    }

    @Test
    void terminalStatesCannotTransition() {
        assertThat(ApplicationStatus.HIRED.isTerminal()).isTrue();
        assertThat(ApplicationStatus.HIRED.canTransitionTo(ApplicationStatus.OFFERED)).isFalse();
        assertThat(ApplicationStatus.REJECTED.canTransitionTo(ApplicationStatus.APPLIED)).isFalse();
    }

    @Test
    void sameStateIsNotATransition() {
        assertThat(ApplicationStatus.APPLIED.canTransitionTo(ApplicationStatus.APPLIED)).isFalse();
    }
}
