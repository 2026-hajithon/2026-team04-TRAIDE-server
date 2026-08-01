package com.gdghajithon.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record AuthResponse(
        @Schema(example = "1")
        Long userId,

        @Schema(example = "eyJhbGciOiJIUzI1NiJ9...")
        String accessToken,

        @Schema(description = "Firebase 커스텀 토큰. 발급 실패 시 null")
        String firebaseToken
) {
}
