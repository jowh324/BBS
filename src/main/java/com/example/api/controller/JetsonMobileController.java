package com.example.api.controller;

import com.example.api.DTO.JetsonDTOs;
import com.example.api.Service.JetsonService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/jetson")
@Tag(name = "jetson-controller-for-mobile")
public class JetsonMobileController {

    private final JetsonService jetsonService;

    @GetMapping("/status")
    public ResponseEntity<JetsonDTOs.MobileStatusResponse> status() {
        return ResponseEntity.ok(jetsonService.getMobileStatus(currentUserId()));
    }

    @PostMapping("/power")
    public ResponseEntity<JetsonDTOs.MobileStatusResponse> power(@Valid @RequestBody JetsonDTOs.PowerRequest req) {
        return ResponseEntity.ok(jetsonService.requestPower(currentUserId(), req.power()));
    }

    private String currentUserId() {
        return (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
