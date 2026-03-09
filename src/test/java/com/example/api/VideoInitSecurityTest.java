package com.example.api;

import com.example.api.DTO.VideoDTOs;
import com.example.api.Service.VideoService;
import com.example.api.JWT.JwtProvider;
import com.example.api.Security.SecurityConfig;
import com.example.api.controller.VideoController;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VideoController.class)
@Import(SecurityConfig.class)
class VideoInitSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VideoService videoService;

    @MockitoBean
    private JwtProvider jwtProvider;

    @Test
    void initEndpointWithJwtAuthShouldReturn200() throws Exception {
        // Simulate the authentication that JwtAuthFilter creates (principal is a String userId)
        var auth = new UsernamePasswordAuthenticationToken("testuser", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(videoService.initUpload(eq("testuser"), any(VideoDTOs.InitRequest.class)))
                .thenReturn(new VideoDTOs.InitResponse("vid-1", "videos/testuser/vid-1.mp4", "https://s3.example.com/upload", 600));

        mockMvc.perform(post("/api/videos/init")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"contentType\":\"video/mp4\"}"))
                .andExpect(status().isOk());
    }
}
