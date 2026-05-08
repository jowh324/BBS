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
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JetsonServiceTest {

    @Mock
    private JetsonDeviceStateRepository repository;

    private JetsonService jetsonService;

    @BeforeEach
    void setUp() {
        jetsonService = new JetsonService(repository);
        ReflectionTestUtils.setField(jetsonService, "disconnectSeconds", 30L);
    }

    @Test
    void mobileStatusIsDisconnectedWhenHeartbeatIsStale() {
        JetsonDeviceState state = new JetsonDeviceState();
        state.setUserId("default");
        state.setHeartbeatStatus(JetsonHeartbeatStatus.ON);
        state.setLastHeartbeatAt(Instant.now().minusSeconds(30));
        when(repository.findById("default")).thenReturn(Optional.of(state));

        JetsonDTOs.MobileStatusResponse response = jetsonService.getMobileStatus("ignored-user");

        assertThat(response.userId()).isEqualTo("default");
        assertThat(response.status()).isEqualTo(JetsonMobileStatus.DISCONNECTED);
        assertThat(response.heartbeatStatus()).isEqualTo(JetsonHeartbeatStatus.ON);
    }

    @Test
    void oversizedDisconnectSecondsFallsBackToDefaultTimeout() {
        ReflectionTestUtils.setField(jetsonService, "disconnectSeconds", Long.MAX_VALUE);

        JetsonDeviceState state = new JetsonDeviceState();
        state.setUserId("default");
        state.setHeartbeatStatus(JetsonHeartbeatStatus.OFF);
        state.setLastHeartbeatAt(Instant.now().minusSeconds(30));
        when(repository.findById("default")).thenReturn(Optional.of(state));

        JetsonDTOs.MobileStatusResponse response = jetsonService.getMobileStatus("ignored-user");

        assertThat(response.status()).isEqualTo(JetsonMobileStatus.DISCONNECTED);
        assertThat(response.disconnectedAfterSeconds()).isEqualTo(30);
    }

    @Test
    void mobileStatusMapsStartingAndRunningToOn() {
        JetsonDeviceState state = new JetsonDeviceState();
        state.setUserId("default");
        state.setHeartbeatStatus(JetsonHeartbeatStatus.STARTING);
        state.setLastHeartbeatAt(Instant.now());
        when(repository.findById("default")).thenReturn(Optional.of(state));

        JetsonDTOs.MobileStatusResponse response = jetsonService.getMobileStatus("ignored-user");

        assertThat(response.status()).isEqualTo(JetsonMobileStatus.ON);
    }

    @Test
    void requestPowerStoresTargetPower() {
        JetsonDeviceState state = new JetsonDeviceState();
        state.setUserId("default");
        when(repository.findById("default")).thenReturn(Optional.of(state));

        JetsonDTOs.MobileStatusResponse response = jetsonService.requestPower("ignored-user", JetsonPowerTarget.ON);

        assertThat(response.userId()).isEqualTo("default");
        assertThat(state.getTargetPower()).isEqualTo(JetsonPowerTarget.ON);
        assertThat(state.getLastCommandAt()).isNotNull();
        assertThat(response.targetPower()).isEqualTo(JetsonPowerTarget.ON);
    }

    @Test
    void heartbeatStoresStateWithUserIdOnly() {
        JetsonDeviceState state = new JetsonDeviceState();
        state.setUserId("default");
        state.setTargetPower(JetsonPowerTarget.ON);
        when(repository.findById("default")).thenReturn(Optional.of(state));
        when(repository.save(any(JetsonDeviceState.class))).thenAnswer(invocation -> invocation.getArgument(0));

        JetsonDTOs.CommandResponse response = jetsonService.reportHeartbeat(
                "ignored-user",
                new JetsonDTOs.HeartbeatRequest(JetsonHeartbeatStatus.RUNNING, "ok")
        );

        assertThat(response.userId()).isEqualTo("default");
        assertThat(state.getHeartbeatStatus()).isEqualTo(JetsonHeartbeatStatus.RUNNING);
        assertThat(state.getLastHeartbeatAt()).isNotNull();
        assertThat(response.shouldRun()).isTrue();
    }
}
