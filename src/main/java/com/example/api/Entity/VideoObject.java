package com.example.api.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "videos", indexes = {
        @Index(name = "ix_videos_user", columnList = "userId")
})
@Getter @Setter
public class VideoObject {

    @Id
    @Column(length = 36)
    private String id;           // UUID

    @Column(nullable = false, length = 36)
    private String userId;       // JWT subject(=userId)

    @Column(nullable = false, length = 512)
    private String s3Key;        // videos/{userId}/{id}.mp4

    @Column(nullable = false, length = 80)
    private String contentType;  // video/mp4 등

    @Column(nullable = false, length = 20)
    private String status;       // INIT, UPLOADED

    private Long sizeBytes;

    @Column(length = 200)
    private String title;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;
}
