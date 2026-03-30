package com.example.api.controller;

import com.example.api.DTO.*;
import com.example.api.Service.AuthService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@AllArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<CreateResponse> signUp(@RequestBody @Valid SignUpRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.signUp(req));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }


    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody @Valid RefreshRequest req){

        return ResponseEntity.ok(authService.refresh(req.refreshToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(Authentication authentication){
        if (authentication == null || authentication.getPrincipal() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(java.util.Map.of("message", "인증이 필요합니다."));
        }

        String userId = (String) authentication.getPrincipal();
        authService.logoutByAccessToken(userId);

        return ResponseEntity.ok(java.util.Map.of("message", "로그아웃 완료"));
    }

    @GetMapping("/checklogin")
    public ResponseEntity<?> checkLogin() {
        return ResponseEntity.ok(java.util.Map.of(
                "authenticated", true,
                "userId", currentUserId()
        ));
    }

    @GetMapping("/me")
    public ResponseEntity<UserInfoResponse> me() {
        return ResponseEntity.ok(authService.getMe(currentUserId()));
    }

    @PutMapping("/me/password")
    public ResponseEntity<Void> changePassword(@RequestBody @Valid ChangePasswordRequest req) {
        authService.changePassword(currentUserId(), req);
        return ResponseEntity.ok().build();
    }

    private String currentUserId() {
        return (String) org.springframework.security.core.context.SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }
}