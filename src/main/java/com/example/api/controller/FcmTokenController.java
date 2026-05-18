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
@RequestMapping("/api/fcm")
@Tag(name = "fcm-token-controller")
public class FcmTokenController {

    private final JetsonService jetsonService;

    @PostMapping("/token")
    public ResponseEntity<JetsonDTOs.FcmTokenResponse> updateToken(
            @Valid @RequestBody JetsonDTOs.FcmTokenRequest req
    ) {
        return ResponseEntity.ok(jetsonService.updateFcmToken(req));
    }
}
