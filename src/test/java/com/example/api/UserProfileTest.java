package com.example.api;

import com.example.api.DTO.UserInfoResponse;
import com.example.api.JWT.JwtAuthFilter;
import com.example.api.JWT.JwtProvider;
import com.example.api.Security.JwtAccessDeniedHandler;
import com.example.api.Security.JwtAuthenticationEntryPoint;
import com.example.api.Security.SecurityConfig;
import com.example.api.Service.AuthService;
import com.example.api.controller.AuthController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, JwtAuthFilter.class, JwtAuthenticationEntryPoint.class, JwtAccessDeniedHandler.class})
class UserProfileTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtProvider jwtProvider;

    private void setAuthenticatedUser(String userId) {
        var auth = new UsernamePasswordAuthenticationToken(userId, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    // ── GET /auth/me ──────────────────────────────────────────────────────────

    @Test
    void getMeShouldReturnUserInfo() throws Exception {
        setAuthenticatedUser("user-123");
        when(authService.getMe("user-123"))
                .thenReturn(new UserInfoResponse("user-123", "홍길동", "hong@example.com"));

        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("user-123"))
                .andExpect(jsonPath("$.name").value("홍길동"))
                .andExpect(jsonPath("$.email").value("hong@example.com"));
    }

    @Test
    void getMeWithoutTokenShouldReturn401() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    // ── PUT /auth/me/password ─────────────────────────────────────────────────

    @Test
    void changePasswordShouldReturn200() throws Exception {
        setAuthenticatedUser("user-123");

        mockMvc.perform(put("/auth/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"oldpass1234\",\"newPassword\":\"newpass1234\",\"newPasswordConfirm\":\"newpass1234\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void changePasswordWithoutTokenShouldReturn401() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(put("/auth/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"oldpass1234\",\"newPassword\":\"newpass1234\",\"newPasswordConfirm\":\"newpass1234\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void changePasswordWithWrongCurrentPasswordShouldReturn400() throws Exception {
        setAuthenticatedUser("user-123");
        doThrow(new IllegalArgumentException("현재 비밀번호가 올바르지 않습니다."))
                .when(authService).changePassword(eq("user-123"), any());

        mockMvc.perform(put("/auth/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"wrongpass\",\"newPassword\":\"newpass1234\",\"newPasswordConfirm\":\"newpass1234\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("현재 비밀번호가 올바르지 않습니다."));
    }

    @Test
    void changePasswordWithShortNewPasswordShouldReturn400() throws Exception {
        setAuthenticatedUser("user-123");

        mockMvc.perform(put("/auth/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"oldpass1234\",\"newPassword\":\"short\",\"newPasswordConfirm\":\"short\"}"))
                .andExpect(status().isBadRequest());
    }
}
