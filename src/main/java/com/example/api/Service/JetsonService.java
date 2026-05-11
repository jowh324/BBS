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
    private static final long DEFAULT_DISCONNECT_SECONDS = 15;
    private static final long MAX_DISCONNECT_SECONDS = 300;
    private static final long ALLOWED_CLOCK_SKEW_SECONDS = 5;

    private final JetsonDeviceStateRepository jetsonDeviceStateRepository;

    @Value("${app.jetson.disconnect-seconds:15}")
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
                        effectiveDisconnectSeconds()
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
                effectiveDisconnectSeconds()
        );
    }

    private JetsonMobileStatus resolveMobileStatus(JetsonDeviceState state) {
        Instant lastHeartbeatAt = state.getLastHeartbeatAt();
        if (lastHeartbeatAt == null) {
            return JetsonMobileStatus.DISCONNECTED;
        }

        Instant now = Instant.now();
        if (lastHeartbeatAt.isAfter(now.plusSeconds(ALLOWED_CLOCK_SKEW_SECONDS))) {
            return JetsonMobileStatus.DISCONNECTED;
        }

        long timeoutSeconds = effectiveDisconnectSeconds();
        if (Duration.between(lastHeartbeatAt, now).getSeconds() >= timeoutSeconds) {
            return JetsonMobileStatus.DISCONNECTED;
        }

        JetsonHeartbeatStatus heartbeatStatus = state.getHeartbeatStatus();
        return JetsonMobileStatus.valueOf(heartbeatStatus.name());
    }

    private long effectiveDisconnectSeconds() {
        if (disconnectSeconds <= 0 || disconnectSeconds > MAX_DISCONNECT_SECONDS) {
            return DEFAULT_DISCONNECT_SECONDS;
        }
        return disconnectSeconds;
    }

    private String trimMessage(String message) {
        if (message == null || message.isBlank()) {
            return null;
        }
        String trimmed = message.trim();
        return trimmed.length() > 500 ? trimmed.substring(0, 500) : trimmed;
    }
}
