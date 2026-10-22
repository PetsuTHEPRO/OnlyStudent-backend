package com.sloth.OnlyStudent.infra.security;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.springframework.http.HttpMethod;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class RateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_TRACKED_CLIENTS = 10_000;
    private static final String RATE_LIMIT_MESSAGE = "Muitas tentativas. Tente novamente mais tarde.";

    private final int authLimit;
    private final Duration authWindow;
    private final int supportLimit;
    private final Duration supportWindow;
    private final Clock clock;
    private final Map<String, RequestWindow> clients = new HashMap<>();

    public RateLimitFilter(int authLimit, Duration authWindow, int supportLimit, Duration supportWindow) {
        this(authLimit, authWindow, supportLimit, supportWindow, Clock.systemUTC());
    }

    RateLimitFilter(int authLimit, Duration authWindow, int supportLimit, Duration supportWindow, Clock clock) {
        if (authLimit < 1 || authWindow.isZero() || authWindow.isNegative()
                || supportLimit < 1 || supportWindow.isZero() || supportWindow.isNegative()) {
            throw new IllegalArgumentException("Rate limits and windows must be positive.");
        }
        this.authLimit = authLimit;
        this.authWindow = authWindow;
        this.supportLimit = supportLimit;
        this.supportWindow = supportWindow;
        this.clock = clock;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        RateLimit rateLimit = rateLimitFor(request);
        if (rateLimit == null || allowRequest(request.getRemoteAddr() + request.getServletPath(), rateLimit)) {
            filterChain.doFilter(request, response);
            return;
        }

        response.setStatus(429);
        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(RATE_LIMIT_MESSAGE);
    }

    private RateLimit rateLimitFor(HttpServletRequest request) {
        if (!HttpMethod.POST.matches(request.getMethod())) {
            return null;
        }
        String path = request.getServletPath();
        if ("/auth/login".equals(path) || "/auth/register".equals(path)) {
            return new RateLimit(authLimit, authWindow);
        }
        if ("/support/send".equals(path)) {
            return new RateLimit(supportLimit, supportWindow);
        }
        return null;
    }

    private synchronized boolean allowRequest(String clientAddress, RateLimit rateLimit) {
        Instant now = clock.instant();
        RequestWindow current = clients.get(clientAddress);
        if (current == null || !current.startedAt().plus(rateLimit.window()).isAfter(now)) {
            if (clients.size() >= MAX_TRACKED_CLIENTS) {
                removeExpiredWindows(now);
            }
            if (clients.size() >= MAX_TRACKED_CLIENTS) {
                return false;
            }
            clients.put(clientAddress, new RequestWindow(now, 1, rateLimit.window()));
            return true;
        }
        if (current.requestCount() >= rateLimit.limit()) {
            return false;
        }
        clients.put(clientAddress,
                new RequestWindow(current.startedAt(), current.requestCount() + 1, rateLimit.window()));
        return true;
    }

    private void removeExpiredWindows(Instant now) {
        Iterator<RequestWindow> windows = clients.values().iterator();
        while (windows.hasNext()) {
            RequestWindow current = windows.next();
            if (!current.startedAt().plus(current.window()).isAfter(now)) {
                windows.remove();
            }
        }
    }

    private record RateLimit(int limit, Duration window) {
    }

    private record RequestWindow(Instant startedAt, int requestCount, Duration window) {
    }
}
