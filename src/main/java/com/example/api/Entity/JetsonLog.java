package com.example.api.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "jetson_logs", indexes = {
        @Index(name = "ix_jetson_logs_created_at", columnList = "createdAt")
})
@Getter @Setter
public class JetsonLog {

    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false, length = 80)
    private String domain;

    @Column(nullable = false, length = 30)
    private String level;

    @Column(nullable = false, length = 2000)
    private String message;

    @Column(nullable = false)
    private Instant createdAt;
}
