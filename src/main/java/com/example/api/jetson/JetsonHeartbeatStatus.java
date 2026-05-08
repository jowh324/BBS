package com.example.api.jetson;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum JetsonHeartbeatStatus {
    STARTING,
    ON,
    RUNNING,
    STOPPING,
    OFF,
    ERROR;

    @JsonCreator
    public static JetsonHeartbeatStatus from(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("state is required");
        }
        return JetsonHeartbeatStatus.valueOf(value.trim().toUpperCase());
    }
}
