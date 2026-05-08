package com.example.api.Entity;

import com.example.api.jetson.JetsonHeartbeatStatus;
import com.example.api.jetson.JetsonPowerTarget;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "jetson_device_states")
@Getter @Setter
public class JetsonDeviceState {

    @Id
    @Column(length = 36)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private JetsonPowerTarget targetPower = JetsonPowerTarget.OFF;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private JetsonHeartbeatStatus heartbeatStatus;

    @Column(length = 500)
    private String message;

    private Instant lastHeartbeatAt;

    private Instant lastCommandAt;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
