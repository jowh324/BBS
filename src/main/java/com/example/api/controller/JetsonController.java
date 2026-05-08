package com.example.api.controller;

import com.example.api.DTO.JetsonDTOs;
import com.example.api.Service.JetsonService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/jetson")
public class JetsonController {

    private final JetsonService jetsonService;

    @GetMapping("/status")
    public ResponseEntity<JetsonDTOs.MobileStatusResponse> status() {
        return ResponseEntity.ok(jetsonService.getMobileStatus(currentUserId()));
    }

    @PostMapping("/power")
    public ResponseEntity<JetsonDTOs.MobileStatusResponse> power(@Valid @RequestBody JetsonDTOs.PowerRequest req) {
        return ResponseEntity.ok(jetsonService.requestPower(currentUserId(), req.power()));
    }

    @GetMapping("/device/command")
    public ResponseEntity<JetsonDTOs.CommandResponse> deviceCommand(
            @RequestHeader("X-Jetson-User-Id") String userId,
            @RequestHeader("X-Jetson-Token") String token
    ) {
        return ResponseEntity.ok(jetsonService.getCommand(userId, token));
    }

    @PostMapping("/device/heartbeat")
    public ResponseEntity<JetsonDTOs.CommandResponse> deviceHeartbeat(
            @RequestHeader("X-Jetson-User-Id") String userId,
            @RequestHeader("X-Jetson-Token") String token,
            @Valid @RequestBody JetsonDTOs.HeartbeatRequest req
    ) {
        return ResponseEntity.ok(jetsonService.reportHeartbeat(userId, token, req));
    }

    private String currentUserId() {
        return (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
