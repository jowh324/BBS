package com.example.api.DTO;

import com.example.api.video.VideoRiskStatus;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

public class VideoDTOs {

    public record InitRequest(
            @NotBlank String contentType,
            String fileExt,
            String title,
            VideoRiskStatus riskStatus
    ) {}

    public record InitResponse(
            String videoId,
            String objectKey,
            String uploadUrl,
            long expiresInSeconds
    ) {}

    public record CompleteRequest(
            Long sizeBytes,
            VideoRiskStatus riskStatus
    ) {}

    public record DownloadUrlResponse(
            String videoId,
            String objectKey,
            String downloadUrl,
            long expiresInSeconds
    ) {}

    public record VideoListItem(
            String videoId,
            String title,
            String status,
            VideoRiskStatus riskStatus,
            Long sizeBytes,
            Instant createdAt
    ) {}
}
