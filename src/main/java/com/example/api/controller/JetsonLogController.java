package com.example.api.controller;

import com.example.api.DTO.JetsonDTOs;
import com.example.api.Service.JetsonLogService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/jetson/logs")
@Tag(name = "jetson-log-controller")
public class JetsonLogController {

    private final JetsonLogService jetsonLogService;

    @PostMapping
    public ResponseEntity<JetsonDTOs.LogResponse> append(@Valid @RequestBody JetsonDTOs.LogRequest req) {
        return ResponseEntity.ok(jetsonLogService.appendLog(req));
    }

    @GetMapping
    public ResponseEntity<List<JetsonDTOs.LogResponse>> list(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant after,
            @RequestParam(required = false) Integer limit
    ) {
        return ResponseEntity.ok(jetsonLogService.listLogs(after, limit));
    }
}
