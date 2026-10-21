package com.hiresphere.common.security;

import com.hiresphere.common.exception.UnauthorizedException;

/** Thread-bound access to the current {@link UserContext}. */
public final class UserContextHolder {

    private static final ThreadLocal<UserContext> HOLDER = new ThreadLocal<>();

    private UserContextHolder() {
    }

    static void set(UserContext context) {
        HOLDER.set(context);
    }

    static void clear() {
        HOLDER.remove();
    }

    /** The current caller, or {@code null} when the request is anonymous. */
    public static UserContext get() {
        return HOLDER.get();
    }

    /** The current caller, or 401 when absent. */
    public static UserContext require() {
        UserContext context = HOLDER.get();
        if (context == null) {
            throw new UnauthorizedException("Authentication is required");
        }
        return context;
    }

    public static Long currentUserId() {
        return require().userId();
    }
}
