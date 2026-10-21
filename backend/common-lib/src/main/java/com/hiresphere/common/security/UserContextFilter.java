package com.hiresphere.common.security;

import com.hiresphere.common.enums.Role;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Populates {@link UserContextHolder} from the {@code X-User-*} headers the
 * gateway sets after validating the JWT. Services trust these headers because
 * the gateway strips any client-supplied copies.
 */
@Slf4j
public class UserContextFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            UserContext context = resolve(request);
            if (context != null) {
                UserContextHolder.set(context);
            }
            filterChain.doFilter(request, response);
        } finally {
            UserContextHolder.clear();
        }
    }

    private UserContext resolve(HttpServletRequest request) {
        String idHeader = request.getHeader(GatewayHeaders.USER_ID);
        String email = request.getHeader(GatewayHeaders.USER_EMAIL);
        String roleHeader = request.getHeader(GatewayHeaders.USER_ROLE);

        if (!StringUtils.hasText(idHeader) || !StringUtils.hasText(roleHeader)) {
            return null;
        }
        try {
            return new UserContext(Long.valueOf(idHeader.trim()), email, Role.valueOf(roleHeader.trim()));
        } catch (IllegalArgumentException ex) {
            log.warn("Ignoring malformed user-context headers: id='{}', role='{}'", idHeader, roleHeader);
            return null;
        }
    }
}
