package com.gdghajithon.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record AuthResponse(
        @Schema(example = "1")
        Long userId,

        @Schema(example = "eyJhbGciOiJIUzI1NiJ9...")
        String accessToken,

        @Schema(example = "Bearer")
        String tokenType,

        @Schema(example = "3600")
        long expiresInSeconds
) {
}
