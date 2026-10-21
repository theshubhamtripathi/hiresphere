package com.hiresphere.common.enums;

import java.util.Set;

/**
 * Lifecycle of a job application. {@link #canTransitionTo(ApplicationStatus)}
 * encodes the allowed state machine shared by services that mutate status.
 */
public enum ApplicationStatus {
    APPLIED,
    UNDER_REVIEW,
    SHORTLISTED,
    INTERVIEW,
    OFFERED,
    HIRED,
    REJECTED,
    WITHDRAWN;

    /** Terminal states cannot transition further. */
    public boolean isTerminal() {
        return this == HIRED || this == REJECTED || this == WITHDRAWN;
    }

    public boolean canTransitionTo(ApplicationStatus target) {
        if (target == null || target == this) {
            return false;
        }
        return switch (this) {
            case APPLIED -> Set.of(UNDER_REVIEW, SHORTLISTED, REJECTED, WITHDRAWN).contains(target);
            case UNDER_REVIEW -> Set.of(SHORTLISTED, INTERVIEW, REJECTED, WITHDRAWN).contains(target);
            case SHORTLISTED -> Set.of(INTERVIEW, OFFERED, REJECTED, WITHDRAWN).contains(target);
            case INTERVIEW -> Set.of(OFFERED, REJECTED, WITHDRAWN).contains(target);
            case OFFERED -> Set.of(HIRED, REJECTED, WITHDRAWN).contains(target);
            case HIRED, REJECTED, WITHDRAWN -> false;
        };
    }
}
