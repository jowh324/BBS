package com.example.api.jetson;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum JetsonPowerTarget {
    ON,
    OFF;

    @JsonCreator
    public static JetsonPowerTarget from(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("power is required");
        }
        return JetsonPowerTarget.valueOf(value.trim().toUpperCase());
    }
}
