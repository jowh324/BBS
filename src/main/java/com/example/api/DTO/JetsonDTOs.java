package com.example.api.DTO;

import com.example.api.jetson.JetsonHeartbeatStatus;
import com.example.api.jetson.JetsonMobileStatus;
import com.example.api.jetson.JetsonPowerTarget;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public class JetsonDTOs {
    public record PowerRequest(@NotNull JetsonPowerTarget power) {
    }

    public record HeartbeatRequest(
            @NotNull JetsonHeartbeatStatus state,
            String message
    ) {
    }

    public record LogRequest(
            @NotBlank
            @Size(max = 80)
            String domain,

            @NotBlank
            @Size(max = 30)
            String level,

            @NotBlank
            @Size(max = 2000)
            String message
    ) {
    }

    public record LogResponse(
            String logId,
            String domain,
            String level,
            String message,
            Instant createdAt
    ) {
    }

    public record FcmTokenRequest(
            @Size(max = 2048)
            String fcmToken
    ) {
    }

    public record FcmTokenResponse(
            String fcmToken
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
            String fcmToken,
            Instant commandUpdatedAt,
            Instant serverTime
    ) {
    }
}
