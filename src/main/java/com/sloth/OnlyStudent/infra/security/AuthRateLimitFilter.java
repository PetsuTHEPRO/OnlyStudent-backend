package com.sloth.OnlyStudent.infra.security;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.springframework.http.HttpMethod;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class AuthRateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_TRACKED_CLIENTS = 10_000;
    private final int limit;
    private final Duration window;
    private final Map<String, RequestWindow> clients = new HashMap<>();

    public AuthRateLimitFilter(int limit, Duration window) {
        if (limit < 1 || window.isZero() || window.isNegative()) {
            throw new IllegalArgumentException("Rate limit and window must be positive.");
        }
        this.limit = limit;
        this.window = window;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        if (!isRateLimitedEndpoint(request) || allowRequest(request.getRemoteAddr() + request.getServletPath())) {
            filterChain.doFilter(request, response);
            return;
        }

        response.setStatus(429);
        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("Muitas tentativas. Tente novamente mais tarde.");
    }

    private boolean isRateLimitedEndpoint(HttpServletRequest request) {
        if (!HttpMethod.POST.matches(request.getMethod())) {
            return false;
        }
        String path = request.getServletPath();
        return "/auth/login".equals(path) || "/auth/register".equals(path);
    }

    private synchronized boolean allowRequest(String clientAddress) {
        Instant now = Instant.now();
        RequestWindow current = clients.get(clientAddress);
        if (current == null || !current.startedAt().plus(window).isAfter(now)) {
            if (clients.size() >= MAX_TRACKED_CLIENTS) {
                removeExpiredWindows(now);
            }
            if (clients.size() >= MAX_TRACKED_CLIENTS) {
                return false;
            }
            clients.put(clientAddress, new RequestWindow(now, 1));
            return true;
        }
        if (current.requestCount() >= limit) {
            return false;
        }
        clients.put(clientAddress, new RequestWindow(current.startedAt(), current.requestCount() + 1));
        return true;
    }

    private void removeExpiredWindows(Instant now) {
        Iterator<RequestWindow> windows = clients.values().iterator();
        while (windows.hasNext()) {
            if (!windows.next().startedAt().plus(window).isAfter(now)) {
                windows.remove();
            }
        }
    }

    private record RequestWindow(Instant startedAt, int requestCount) {
    }
}
