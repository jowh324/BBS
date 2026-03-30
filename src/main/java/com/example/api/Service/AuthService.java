package com.example.api.Service;

import com.example.api.DTO.*;
import com.example.api.Entity.RefreshToken;
import com.example.api.Entity.User;
import com.example.api.JWT.JwtProvider;
import com.example.api.Repository.RefreshTokenRepository;
import com.example.api.Repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepo;
    private final RefreshTokenRepository rtRepo;
    private final PasswordEncoder encoder;
    private final JwtProvider jwt;

    @Transactional
    public CreateResponse signUp(SignUpRequest req) {
        if (!req.password().equals(req.passwordConfirm())) {
            throw new IllegalArgumentException("비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        }

        String email = req.email().trim().toLowerCase();

        userRepo.findByEmail(email).ifPresent(u -> {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        });

        User u = new User();
        u.setId(UUID.randomUUID().toString());
        u.setEmail(email);
        u.setPasswordHash(encoder.encode(req.password()));
        u.setName(req.name().trim());
        userRepo.save(u);

        return new CreateResponse(u.getId());
    }

    @Transactional
    public AuthResponse login(LoginRequest req) {
        String email = req.email().trim().toLowerCase();

        User u = userRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다."));

        if (!encoder.matches(req.password(), u.getPasswordHash())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        u.setLastLoginAt(Instant.now());

        String access = jwt.issueAccess(u.getId());
        String refresh = jwt.issueRefresh(u.getId());

        RefreshToken rt = new RefreshToken();
        rt.setId(UUID.randomUUID().toString());
        rt.setUserId(u.getId());
        rt.setToken(refresh);
        rt.setExpiresAt(Instant.now().plus(Duration.ofDays(30)));
        rtRepo.save(rt);

        return new AuthResponse(u.getId(), access, refresh);
    }

    @Transactional
    public AuthResponse refresh(String refreshToken) {
        if (refreshToken == null) {
            throw new IllegalArgumentException("refreshToken 값이 필요합니다.");
        }

        String token = refreshToken.trim();
        if (token.isEmpty()) {
            throw new IllegalArgumentException("refreshToken 값이 비어 있습니다.");
        }

        RefreshToken rt = rtRepo.findByTokenAndRevokedFalse(token)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다."));

        if (rt.getExpiresAt() == null || !rt.getExpiresAt().isAfter(Instant.now())) {
            throw new IllegalArgumentException("만료된 리프레시 토큰입니다.");
        }

        String newAccess = jwt.issueAccess(rt.getUserId());
        String newRefresh = jwt.issueRefresh(rt.getUserId());

        rt.setRevoked(true);

        RefreshToken next = new RefreshToken();
        next.setId(UUID.randomUUID().toString());
        next.setUserId(rt.getUserId());
        next.setToken(newRefresh);
        next.setExpiresAt(Instant.now().plus(Duration.ofDays(30)));
        rtRepo.save(next);

        return new AuthResponse(rt.getUserId(), newAccess, newRefresh);
    }

    public UserInfoResponse getMe(String userId) {
        User u = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        return new UserInfoResponse(u.getId(), u.getName(), u.getEmail());
    }

    @Transactional
    public void changePassword(String userId, ChangePasswordRequest req) {
        if (!req.newPassword().equals(req.newPasswordConfirm())) {
            throw new IllegalArgumentException("새 비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        }

        User u = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (!encoder.matches(req.currentPassword(), u.getPasswordHash())) {
            throw new IllegalArgumentException("현재 비밀번호가 올바르지 않습니다.");
        }

        u.setPasswordHash(encoder.encode(req.newPassword()));
    }

    @Transactional
    public void logoutByAccessToken(String userId){
        rtRepo.findAllByUserIdAndRevokedFalse(userId)
                .forEach(rt -> rt.setRevoked(true));
    }
}