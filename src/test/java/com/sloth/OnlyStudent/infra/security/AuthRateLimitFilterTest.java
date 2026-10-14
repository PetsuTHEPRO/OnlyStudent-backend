package com.sloth.OnlyStudent.infra.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class AuthRateLimitFilterTest {

    @Test
    void limitsOnlyLoginAndRegistrationRequestsWithPlainTextResponse() throws Exception {
        AuthRateLimitFilter filter = new AuthRateLimitFilter(1, Duration.ofMinutes(1));
        MockFilterChain firstChain = new MockFilterChain();
        filter.doFilter(request("/auth/login"), new MockHttpServletResponse(), firstChain);
        assertTrue(firstChain.getRequest() != null);

        MockHttpServletResponse limitedResponse = new MockHttpServletResponse();
        filter.doFilter(request("/auth/login"), limitedResponse, new MockFilterChain());

        assertEquals(429, limitedResponse.getStatus());
        assertTrue(limitedResponse.getContentType().startsWith("text/plain"));
        assertEquals("Muitas tentativas. Tente novamente mais tarde.", limitedResponse.getContentAsString());

        MockHttpServletResponse otherEndpointResponse = new MockHttpServletResponse();
        filter.doFilter(request("/turma/allClassroom"), otherEndpointResponse, new MockFilterChain());
        assertEquals(200, otherEndpointResponse.getStatus());
    }

    private MockHttpServletRequest request(String path) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", path);
        request.setServletPath(path);
        request.setRemoteAddr("192.0.2.10");
        return request;
    }
}
