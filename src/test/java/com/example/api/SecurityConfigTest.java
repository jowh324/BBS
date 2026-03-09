package com.example.api;

import com.example.api.Security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityConfigTest {

    @Test
    void corsAllowsHttpsFrontendOrigin() {
        SecurityConfig securityConfig = new SecurityConfig(null);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/videos/init");

        var corsConfiguration = securityConfig.corsConfigurationSource().getCorsConfiguration(request);

        assertThat(corsConfiguration).isNotNull();
        assertThat(corsConfiguration.checkOrigin("https://homecam.r-e.kr"))
                .isEqualTo("https://homecam.r-e.kr");
    }
}
