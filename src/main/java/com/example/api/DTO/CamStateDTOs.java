package com.example.api.DTO;

import com.example.api.Entity.CamStateType;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public class CamStateDTOs {

    public record UpdateRequest(
            @NotNull
            CamStateType state
    ) {}

    public record StateResponse(
            String state,
            Instant updatedAt
    ) {}
}
