package com.gdghajithon.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignupRequest(
        @Schema(description = "로그인 아이디", example = "user01")
        @NotBlank(message = "아이디는 필수입니다.")
        String loginId,

        @Schema(description = "비밀번호(8자 이상)", example = "password123")
        @NotBlank(message = "비밀번호는 필수입니다.")
        @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.")
        String password
) {
}
