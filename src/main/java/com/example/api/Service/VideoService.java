package com.example.api.Service;

import com.example.api.DTO.VideoDTOs;
import com.example.api.Entity.VideoObject;
import com.example.api.Repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VideoService {

    private final VideoRepository videoRepository;
    private final S3Presigner presigner;

    @Value("${app.s3.bucket}") private String bucket;
    @Value("${app.s3.prefix}") private String prefix;
    @Value("${app.s3.presignMinutes:10}") private long presignMinutes;

    private static final DateTimeFormatter TITLE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneOffset.UTC);

    public VideoDTOs.InitResponse initUpload(String userId, VideoDTOs.InitRequest req) {
        String id = UUID.randomUUID().toString();
        String ext = (req.fileExt() == null || req.fileExt().isBlank()) ? "mp4" : req.fileExt().trim();
        String key = "%s/%s/%s.%s".formatted(prefix, userId, id, ext);
        Instant now = Instant.now();

        String title = (req.title() == null || req.title().isBlank())
                ? TITLE_FORMATTER.format(now)
                : req.title().trim();

        // DB 저장 (INIT)
        VideoObject v = new VideoObject();
        v.setId(id);
        v.setUserId(userId);
        v.setS3Key(key);
        v.setContentType(req.contentType());
        v.setStatus("INIT");
        v.setTitle(title);
        v.setCreatedAt(now);
        v.setUpdatedAt(now);
        videoRepository.save(v);

        // Presigned PUT 생성
        PutObjectRequest por = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(req.contentType())
                .build();

        Duration sig = Duration.ofMinutes(presignMinutes);

        PutObjectPresignRequest presignReq = PutObjectPresignRequest.builder()
                .signatureDuration(sig)
                .putObjectRequest(por)
                .build();

        String uploadUrl = presigner.presignPutObject(presignReq).url().toString();

        return new VideoDTOs.InitResponse(id, key, uploadUrl, sig.toSeconds());
    }

    public void completeUpload(String userId, String videoId, VideoDTOs.CompleteRequest req) {
        VideoObject v = videoRepository.findByIdAndUserId(videoId, userId)
                .orElseThrow(() -> new IllegalArgumentException("video not found"));

        v.setStatus("UPLOADED");
        v.setSizeBytes(req.sizeBytes());
        v.setUpdatedAt(Instant.now());
        videoRepository.save(v);
    }

    public VideoDTOs.DownloadUrlResponse issueDownloadUrl(String userId, String videoId) {
        VideoObject v = videoRepository.findByIdAndUserId(videoId, userId)
                .orElseThrow(() -> new IllegalArgumentException("video not found"));

        if (!"UPLOADED".equals(v.getStatus())) {
            throw new IllegalStateException("video not uploaded yet");
        }

        GetObjectRequest gor = GetObjectRequest.builder()
                .bucket(bucket)
                .key(v.getS3Key())
                .responseContentType(v.getContentType())
                .build();

        Duration sig = Duration.ofMinutes(presignMinutes);

        GetObjectPresignRequest presignReq = GetObjectPresignRequest.builder()
                .signatureDuration(sig)
                .getObjectRequest(gor)
                .build();

        String downloadUrl = presigner.presignGetObject(presignReq).url().toString();

        return new VideoDTOs.DownloadUrlResponse(v.getId(), v.getS3Key(), downloadUrl, sig.toSeconds());
    }

    public List<VideoDTOs.VideoListItem> listVideos(String userId) {
        return videoRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(v -> new VideoDTOs.VideoListItem(
                        v.getId(),
                        v.getTitle(),
                        v.getStatus(),
                        v.getSizeBytes(),
                        v.getCreatedAt()
                ))
                .toList();
    }
}