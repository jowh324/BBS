package com.example.api.DTO;

import com.example.api.jetson.JetsonHeartbeatStatus;
import com.example.api.jetson.JetsonMobileStatus;
import com.example.api.jetson.JetsonPowerTarget;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public class JetsonDTOs {
    public record PowerRequest(@NotNull JetsonPowerTarget power) {
    }

    public record HeartbeatRequest(
            @NotNull JetsonHeartbeatStatus state,
            String message
    ) {
    }

    public record MobileStatusResponse(
            String userId,
            JetsonMobileStatus status,
            JetsonHeartbeatStatus heartbeatStatus,
            JetsonPowerTarget targetPower,
            Instant lastHeartbeatAt,
            Instant lastCommandAt,
            long disconnectedAfterSeconds
    ) {
    }

    public record CommandResponse(
            String userId,
            JetsonPowerTarget targetPower,
            boolean shouldRun,
            Instant commandUpdatedAt,
            Instant serverTime
    ) {
    }
}
