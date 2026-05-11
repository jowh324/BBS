package com.example.api;

import com.example.api.DTO.VideoDTOs;
import com.example.api.Entity.VideoObject;
import com.example.api.Repository.VideoRepository;
import com.example.api.Service.VideoService;
import com.example.api.video.VideoRiskStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VideoServiceTest {

    @Test
    void completeWithoutRiskStatusKeepsExistingRiskStatus() {
        VideoRepository repository = mock(VideoRepository.class);
        VideoService videoService = new VideoService(repository, null);

        VideoObject video = new VideoObject();
        video.setId("video-1");
        video.setUserId("user-1");
        video.setRiskStatus(VideoRiskStatus.COVERED);

        when(repository.findByIdAndUserId("video-1", "user-1")).thenReturn(Optional.of(video));

        videoService.completeUpload("user-1", "video-1", new VideoDTOs.CompleteRequest(123L, null));

        assertThat(video.getStatus()).isEqualTo("UPLOADED");
        assertThat(video.getSizeBytes()).isEqualTo(123L);
        assertThat(video.getRiskStatus()).isEqualTo(VideoRiskStatus.COVERED);
    }

    @Test
    void listVideosReturnsRiskStatus() {
        VideoRepository repository = mock(VideoRepository.class);
        VideoService videoService = new VideoService(repository, null);

        Instant createdAt = Instant.parse("2026-05-11T12:00:00Z");
        VideoObject video = new VideoObject();
        video.setId("video-1");
        video.setUserId("user-1");
        video.setRiskStatus(VideoRiskStatus.TURNOVER);
        video.setCreatedAt(createdAt);

        when(repository.findByUserIdOrderByCreatedAtDesc("user-1")).thenReturn(List.of(video));

        List<VideoDTOs.VideoListItem> videos = videoService.listVideos("user-1");

        assertThat(videos).hasSize(1);
        assertThat(videos.get(0).videoId()).isEqualTo("video-1");
        assertThat(videos.get(0).riskStatus()).isEqualTo(VideoRiskStatus.TURNOVER);
        assertThat(videos.get(0).createdAt()).isEqualTo(createdAt);
    }
}
