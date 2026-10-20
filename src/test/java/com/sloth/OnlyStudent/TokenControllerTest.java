package com.sloth.OnlyStudent;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.sloth.OnlyStudent.controller.TokenController;
import com.sloth.OnlyStudent.infra.security.TokenService;

class TokenControllerTest {

    private TokenService tokenService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        tokenService = mock(TokenService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new TokenController(tokenService)).build();
    }

    @Test
    void keepsCurrentValidTokenResponse() throws Exception {
        when(tokenService.validateToken("valid-token")).thenReturn("user@example.com");

        mockMvc.perform(get("/token/validate").header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(content().string("Token is valid."));
    }

    @Test
    void keepsCurrentInvalidAndExceptionalTokenResponses() throws Exception {
        when(tokenService.validateToken("invalid-token")).thenReturn(null);
        doThrow(new IllegalStateException("verification unavailable"))
                .when(tokenService).validateToken("exception-token");

        mockMvc.perform(get("/token/validate").header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Token is invalid or expired."));
        mockMvc.perform(get("/token/validate").header("Authorization", "Bearer exception-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Token is invalid or expired."));
    }
}
