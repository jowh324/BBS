package com.example.api.Service;

import com.example.api.DTO.JetsonDTOs;
import com.example.api.Entity.JetsonDeviceState;
import com.example.api.Repository.JetsonDeviceStateRepository;
import com.example.api.jetson.JetsonHeartbeatStatus;
import com.example.api.jetson.JetsonMobileStatus;
import com.example.api.jetson.JetsonPowerTarget;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class JetsonService {

    private final JetsonDeviceStateRepository jetsonDeviceStateRepository;

    @Value("${app.jetson.token:change-me}")
    private String jetsonToken;

    @Value("${app.jetson.disconnect-seconds:30}")
    private long disconnectSeconds;

    @Transactional(readOnly = true)
    public JetsonDTOs.MobileStatusResponse getMobileStatus(String userId) {
        return jetsonDeviceStateRepository.findById(userId)
                .map(this::toMobileStatus)
                .orElseGet(() -> new JetsonDTOs.MobileStatusResponse(
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
        JetsonDeviceState state = findOrCreate(userId);
        state.setTargetPower(power);
        state.setLastCommandAt(Instant.now());
        jetsonDeviceStateRepository.save(state);
        return toMobileStatus(state);
    }

    @Transactional(readOnly = true)
    public JetsonDTOs.CommandResponse getCommand(String userId, String token) {
        validateToken(token);
        JetsonDeviceState state = jetsonDeviceStateRepository.findById(userId)
                .orElseGet(() -> defaultState(userId));

        return new JetsonDTOs.CommandResponse(
                state.getTargetPower(),
                state.getTargetPower() == JetsonPowerTarget.ON,
                state.getLastCommandAt(),
                Instant.now()
        );
    }

    @Transactional
    public JetsonDTOs.CommandResponse reportHeartbeat(String userId, String token, JetsonDTOs.HeartbeatRequest req) {
        validateToken(token);
        JetsonDeviceState state = findOrCreate(userId);
        state.setHeartbeatStatus(req.state());
        state.setMessage(trimMessage(req.message()));
        state.setLastHeartbeatAt(Instant.now());
        jetsonDeviceStateRepository.save(state);

        return new JetsonDTOs.CommandResponse(
                state.getTargetPower(),
                state.getTargetPower() == JetsonPowerTarget.ON,
                state.getLastCommandAt(),
                Instant.now()
        );
    }

    private JetsonDeviceState findOrCreate(String userId) {
        return jetsonDeviceStateRepository.findById(userId)
                .orElseGet(() -> jetsonDeviceStateRepository.save(defaultState(userId)));
    }

    private JetsonDeviceState defaultState(String userId) {
        JetsonDeviceState state = new JetsonDeviceState();
        state.setUserId(userId);
        state.setTargetPower(JetsonPowerTarget.OFF);
        return state;
    }

    private JetsonDTOs.MobileStatusResponse toMobileStatus(JetsonDeviceState state) {
        return new JetsonDTOs.MobileStatusResponse(
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

    private void validateToken(String token) {
        if (jetsonToken == null || jetsonToken.isBlank() || "change-me".equals(jetsonToken)) {
            throw new BadCredentialsException("jetson token is not configured");
        }
        if (token == null || !MessageDigest.isEqual(
                token.getBytes(StandardCharsets.UTF_8),
                jetsonToken.getBytes(StandardCharsets.UTF_8)
        )) {
            throw new BadCredentialsException("invalid jetson token");
        }
    }

    private String trimMessage(String message) {
        if (message == null || message.isBlank()) {
            return null;
        }
        String trimmed = message.trim();
        return trimmed.length() > 500 ? trimmed.substring(0, 500) : trimmed;
    }
}
