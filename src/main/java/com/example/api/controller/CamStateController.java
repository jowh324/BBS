package com.example.api.controller;

import com.example.api.DTO.CamStateDTOs;
import com.example.api.Service.CamStateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cam")
public class CamStateController {

    private final CamStateService camStateService;

    // 현재 관찰 상태 조회
    @GetMapping("/state")
    public ResponseEntity<CamStateDTOs.StateResponse> getState() {
        return ResponseEntity.ok(camStateService.getState());
    }

    // 관찰 상태 갱신
    @PutMapping("/state")
    public ResponseEntity<CamStateDTOs.StateResponse> updateState(
            @Valid @RequestBody CamStateDTOs.UpdateRequest req) {
        return ResponseEntity.ok(camStateService.updateState(req.state()));
    }
}
