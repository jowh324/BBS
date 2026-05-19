package com.example.api.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "cam_state")
@Getter @Setter
public class CamState {

    @Id
    private Long id; // Always 1 — singleton record

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CamStateType state; // WATCHING, NOTWATCHING

    @Column(nullable = false)
    private Instant updatedAt;
}
