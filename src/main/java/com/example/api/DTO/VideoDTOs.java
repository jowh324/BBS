package com.example.api.DTO;

import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

public class VideoDTOs {

    public record InitRequest(
            @NotBlank String contentType,   // "video/mp4"
            String fileExt,                 // "mp4" (옵션)
            String title                    // 영상 제목 (옵션, 미입력 시 업로드 시각으로 자동 설정)
    ) {}

    public record InitResponse(
            String videoId,
            String objectKey,
            String uploadUrl,
            long expiresInSeconds
    ) {}

    public record CompleteRequest(
            Long sizeBytes
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
            Long sizeBytes,
            Instant createdAt
    ) {}
}
