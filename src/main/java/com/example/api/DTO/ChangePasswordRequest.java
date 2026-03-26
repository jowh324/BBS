package com.example.api.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "비밀번호 변경 요청")
public record ChangePasswordRequest(
        @Schema(description = "현재 비밀번호", example = "password1234")
        @NotBlank
        String currentPassword,

        @Schema(description = "새 비밀번호", example = "newpassword1234")
        @NotBlank @Size(min = 8, max = 64)
        String newPassword,

        @Schema(description = "새 비밀번호 확인", example = "newpassword1234")
        @NotBlank
        String newPasswordConfirm
) {}
