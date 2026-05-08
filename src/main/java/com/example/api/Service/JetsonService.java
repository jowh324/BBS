package com.example.api.Service;

import com.example.api.DTO.JetsonDTOs;
import com.example.api.Entity.JetsonDeviceState;
import com.example.api.Repository.JetsonDeviceStateRepository;
import com.example.api.jetson.JetsonHeartbeatStatus;
import com.example.api.jetson.JetsonMobileStatus;
import com.example.api.jetson.JetsonPowerTarget;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class JetsonService {

    private static final String DEFAULT_DEVICE_ID = "default";

    private final JetsonDeviceStateRepository jetsonDeviceStateRepository;

    @Value("${app.jetson.disconnect-seconds:30}")
    private long disconnectSeconds;

    @Transactional(readOnly = true)
    public JetsonDTOs.MobileStatusResponse getMobileStatus(String userId) {
        return jetsonDeviceStateRepository.findById(DEFAULT_DEVICE_ID)
                .map(this::toMobileStatus)
                .orElseGet(() -> new JetsonDTOs.MobileStatusResponse(
                        DEFAULT_DEVICE_ID,
                        JetsonMobileStatus.DISCONNECTED,
                        null,
                        JetsonPowerTarget.OFF,
                        null,
                        null,
                        disconnectSeconds
                ));
    }

    @Transactional
    public JetsonDTOs.MobileStatusResponse requestPower(String userId, JetsonPowerTarget power) {
        JetsonDeviceState state = findOrCreate();
        state.setTargetPower(power);
        state.setLastCommandAt(Instant.now());
        jetsonDeviceStateRepository.save(state);
        return toMobileStatus(state);
    }

    @Transactional(readOnly = true)
    public JetsonDTOs.CommandResponse getCommand(String userId) {
        JetsonDeviceState state = jetsonDeviceStateRepository.findById(DEFAULT_DEVICE_ID)
                .orElseGet(this::defaultState);

        return new JetsonDTOs.CommandResponse(
                DEFAULT_DEVICE_ID,
                state.getTargetPower(),
                state.getTargetPower() == JetsonPowerTarget.ON,
                state.getLastCommandAt(),
                Instant.now()
        );
    }

    @Transactional
    public JetsonDTOs.CommandResponse reportHeartbeat(String userId, JetsonDTOs.HeartbeatRequest req) {
        JetsonDeviceState state = findOrCreate();
        state.setHeartbeatStatus(req.state());
        state.setMessage(trimMessage(req.message()));
        state.setLastHeartbeatAt(Instant.now());
        jetsonDeviceStateRepository.save(state);

        return new JetsonDTOs.CommandResponse(
                DEFAULT_DEVICE_ID,
                state.getTargetPower(),
                state.getTargetPower() == JetsonPowerTarget.ON,
                state.getLastCommandAt(),
                Instant.now()
        );
    }

    private JetsonDeviceState findOrCreate() {
        return jetsonDeviceStateRepository.findById(DEFAULT_DEVICE_ID)
                .orElseGet(() -> jetsonDeviceStateRepository.save(defaultState()));
    }

    private JetsonDeviceState defaultState() {
        JetsonDeviceState state = new JetsonDeviceState();
        state.setUserId(DEFAULT_DEVICE_ID);
        state.setTargetPower(JetsonPowerTarget.OFF);
        return state;
    }

    private JetsonDTOs.MobileStatusResponse toMobileStatus(JetsonDeviceState state) {
        return new JetsonDTOs.MobileStatusResponse(
                state.getUserId(),
                resolveMobileStatus(state),
                state.getHeartbeatStatus(),
                state.getTargetPower(),
                state.getLastHeartbeatAt(),
                state.getLastCommandAt(),
                disconnectSeconds
        );
    }

    private JetsonMobileStatus resolveMobileStatus(JetsonDeviceState state) {
        if (state.getLastHeartbeatAt() == null ||
                Duration.between(state.getLastHeartbeatAt(), Instant.now()).getSeconds() > disconnectSeconds) {
            return JetsonMobileStatus.DISCONNECTED;
        }

        JetsonHeartbeatStatus heartbeatStatus = state.getHeartbeatStatus();
        if (heartbeatStatus == JetsonHeartbeatStatus.ERROR) {
            return JetsonMobileStatus.ERROR;
        }
        if (heartbeatStatus == JetsonHeartbeatStatus.STARTING ||
                heartbeatStatus == JetsonHeartbeatStatus.ON ||
                heartbeatStatus == JetsonHeartbeatStatus.RUNNING) {
            return JetsonMobileStatus.ON;
        }
        return JetsonMobileStatus.OFF;
    }

    private String trimMessage(String message) {
        if (message == null || message.isBlank()) {
            return null;
        }
        String trimmed = message.trim();
        return trimmed.length() > 500 ? trimmed.substring(0, 500) : trimmed;
    }
}
