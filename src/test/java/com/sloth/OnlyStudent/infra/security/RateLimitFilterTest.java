package com.sloth.OnlyStudent.infra.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class RateLimitFilterTest {

    private static final String RATE_LIMIT_MESSAGE = "Muitas tentativas. Tente novamente mais tarde.";

    @Test
    void allowsFiveSupportRequestsAndRejectsTheSixthWithPlainText() throws Exception {
        RateLimitFilter filter = filter(new MutableClock(Instant.parse("2026-10-15T17:00:00Z")));

        for (int requestNumber = 1; requestNumber <= 5; requestNumber++) {
            MockFilterChain chain = new MockFilterChain();
            filter.doFilter(request("/support/send"), new MockHttpServletResponse(), chain);
            assertTrue(chain.getRequest() != null);
        }

        MockHttpServletResponse limitedResponse = new MockHttpServletResponse();
        filter.doFilter(request("/support/send"), limitedResponse, new MockFilterChain());

        assertEquals(429, limitedResponse.getStatus());
        assertTrue(limitedResponse.getContentType().startsWith("text/plain"));
        assertEquals(RATE_LIMIT_MESSAGE, limitedResponse.getContentAsString());
    }

    @Test
    void releasesSupportLimitAfterItsWindowExpires() throws Exception {
        MutableClock clock = new MutableClock(Instant.parse("2026-10-15T17:00:00Z"));
        RateLimitFilter filter = filter(clock);
        for (int requestNumber = 1; requestNumber <= 5; requestNumber++) {
            filter.doFilter(request("/support/send"), new MockHttpServletResponse(), new MockFilterChain());
        }

        MockHttpServletResponse limitedResponse = new MockHttpServletResponse();
        filter.doFilter(request("/support/send"), limitedResponse, new MockFilterChain());
        assertEquals(429, limitedResponse.getStatus());

        clock.advance(Duration.ofHours(1));
        MockFilterChain afterWindowChain = new MockFilterChain();
        filter.doFilter(request("/support/send"), new MockHttpServletResponse(), afterWindowChain);
        assertTrue(afterWindowChain.getRequest() != null);
    }

    @Test
    void keepsAuthenticationLimitIndependentAndDoesNotLimitOtherEndpoints() throws Exception {
        RateLimitFilter filter = filter(new MutableClock(Instant.parse("2026-10-15T17:00:00Z")));

        for (int requestNumber = 1; requestNumber <= 10; requestNumber++) {
            MockFilterChain chain = new MockFilterChain();
            filter.doFilter(request("/auth/login"), new MockHttpServletResponse(), chain);
            assertTrue(chain.getRequest() != null);
        }

        MockHttpServletResponse authLimitedResponse = new MockHttpServletResponse();
        filter.doFilter(request("/auth/login"), authLimitedResponse, new MockFilterChain());
        assertEquals(429, authLimitedResponse.getStatus());
        assertEquals(RATE_LIMIT_MESSAGE, authLimitedResponse.getContentAsString());

        for (int requestNumber = 1; requestNumber <= 7; requestNumber++) {
            MockFilterChain chain = new MockFilterChain();
            filter.doFilter(request("/turma/register"), new MockHttpServletResponse(), chain);
            assertTrue(chain.getRequest() != null);
        }
    }

    private RateLimitFilter filter(MutableClock clock) {
        return new RateLimitFilter(10, Duration.ofSeconds(60), 5, Duration.ofHours(1), clock);
    }

    private MockHttpServletRequest request(String path) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", path);
        request.setServletPath(path);
        request.setRemoteAddr("192.0.2.10");
        return request;
    }

    private static final class MutableClock extends Clock {
        private Instant instant;

        private MutableClock(Instant instant) {
            this.instant = instant;
        }

        @Override
        public ZoneId getZone() {
            return ZoneId.of("UTC");
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }

        private void advance(Duration duration) {
            instant = instant.plus(duration);
        }
    }
}
