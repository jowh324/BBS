package com.example.api.video;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum VideoRiskStatus {
    COVERED,
    TURNOVER,
    MISSING;

    @JsonCreator
    public static VideoRiskStatus from(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return VideoRiskStatus.valueOf(value.trim().toUpperCase());
    }
}
