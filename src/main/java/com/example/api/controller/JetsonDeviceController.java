package com.example.api.controller;

import com.example.api.DTO.JetsonDTOs;
import com.example.api.Service.JetsonService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/jetson/device")
@Tag(name = "jetson-controller-for-jetson")
public class JetsonDeviceController {

    private final JetsonService jetsonService;

    @GetMapping("/command")
    public ResponseEntity<JetsonDTOs.CommandResponse> deviceCommand(
            @RequestHeader("userid") String userId
    ) {
        return ResponseEntity.ok(jetsonService.getCommand(userId));
    }

    @PostMapping("/heartbeat")
    public ResponseEntity<JetsonDTOs.CommandResponse> deviceHeartbeat(
            @RequestHeader("userid") String userId,
            @Valid @RequestBody JetsonDTOs.HeartbeatRequest req
    ) {
        return ResponseEntity.ok(jetsonService.reportHeartbeat(userId, req));
    }
}
