package com.example.api.DTO;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "사용자 정보 응답")
public record UserInfoResponse(
        @Schema(description = "유저 ID", example = "550e8400-e29b-41d4-a716-446655440000")
        String userId,

        @Schema(description = "이름", example = "홍길동")
        String name,

        @Schema(description = "이메일", example = "test@example.com")
        String email
) {}
