package com.hiresphere.common.event;

/** Canonical Kafka topic names shared by producers and consumers. */
public final class Topics {

    public static final String USER_REGISTERED = "user.registered";
    public static final String JOB_POSTED = "job.posted";
    public static final String JOB_ALERT = "job.alert";
    public static final String APPLICATION_SUBMITTED = "application.submitted";
    public static final String APPLICATION_STATUS_CHANGED = "application.status-changed";
    public static final String PAYMENT_COMPLETED = "payment.completed";

    /** Dead-letter topic suffix appended by consumers' error handlers. */
    public static final String DLT_SUFFIX = ".DLT";

    private Topics() {
    }
}
