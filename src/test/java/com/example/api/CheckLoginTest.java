package com.example.api;

import com.example.api.JWT.JwtProvider;
import com.example.api.Security.SecurityConfig;
import com.example.api.Service.AuthService;
import com.example.api.controller.AuthController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class CheckLoginTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtProvider jwtProvider;

    @Test
    void checkLoginWithValidTokenShouldReturn200() throws Exception {
        var auth = new UsernamePasswordAuthenticationToken("test-user-id", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);

        mockMvc.perform(get("/auth/checklogin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticated").value(true))
                .andExpect(jsonPath("$.userId").value("test-user-id"));
    }

    @Test
    void checkLoginWithoutTokenShouldReturn401() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(get("/auth/checklogin"))
                .andExpect(status().isUnauthorized());
    }
}
