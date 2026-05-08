package com.example.api;

import com.example.api.DTO.JetsonDTOs;
import com.example.api.Entity.JetsonDeviceState;
import com.example.api.Repository.JetsonDeviceStateRepository;
import com.example.api.Service.JetsonService;
import com.example.api.jetson.JetsonHeartbeatStatus;
import com.example.api.jetson.JetsonMobileStatus;
import com.example.api.jetson.JetsonPowerTarget;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JetsonServiceTest {

    @Mock
    private JetsonDeviceStateRepository repository;

    private JetsonService jetsonService;

    @BeforeEach
    void setUp() {
        jetsonService = new JetsonService(repository);
        ReflectionTestUtils.setField(jetsonService, "jetsonToken", "test-token");
        ReflectionTestUtils.setField(jetsonService, "disconnectSeconds", 30L);
    }

    @Test
    void mobileStatusIsDisconnectedWhenHeartbeatIsStale() {
        JetsonDeviceState state = new JetsonDeviceState();
        state.setUserId("user-1");
        state.setHeartbeatStatus(JetsonHeartbeatStatus.ON);
        state.setLastHeartbeatAt(Instant.now().minusSeconds(31));
        when(repository.findById("user-1")).thenReturn(Optional.of(state));

        JetsonDTOs.MobileStatusResponse response = jetsonService.getMobileStatus("user-1");

        assertThat(response.status()).isEqualTo(JetsonMobileStatus.DISCONNECTED);
        assertThat(response.heartbeatStatus()).isEqualTo(JetsonHeartbeatStatus.ON);
    }

    @Test
    void mobileStatusMapsStartingAndRunningToOn() {
        JetsonDeviceState state = new JetsonDeviceState();
        state.setUserId("user-1");
        state.setHeartbeatStatus(JetsonHeartbeatStatus.STARTING);
        state.setLastHeartbeatAt(Instant.now());
        when(repository.findById("user-1")).thenReturn(Optional.of(state));

        JetsonDTOs.MobileStatusResponse response = jetsonService.getMobileStatus("user-1");

        assertThat(response.status()).isEqualTo(JetsonMobileStatus.ON);
    }

    @Test
    void requestPowerStoresTargetPower() {
        JetsonDeviceState state = new JetsonDeviceState();
        state.setUserId("user-1");
        when(repository.findById("user-1")).thenReturn(Optional.of(state));

        JetsonDTOs.MobileStatusResponse response = jetsonService.requestPower("user-1", JetsonPowerTarget.ON);

        assertThat(state.getTargetPower()).isEqualTo(JetsonPowerTarget.ON);
        assertThat(state.getLastCommandAt()).isNotNull();
        assertThat(response.targetPower()).isEqualTo(JetsonPowerTarget.ON);
    }

    @Test
    void heartbeatRequiresJetsonToken() {
        assertThatThrownBy(() -> jetsonService.reportHeartbeat(
                "user-1",
                "wrong-token",
                new JetsonDTOs.HeartbeatRequest(JetsonHeartbeatStatus.ON, null)
        )).isInstanceOf(BadCredentialsException.class);
    }
}
