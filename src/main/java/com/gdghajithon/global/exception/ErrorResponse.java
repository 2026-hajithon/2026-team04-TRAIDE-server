package com.gdghajithon.global.exception;

import java.time.LocalDateTime;

public record ErrorResponse(
        String code,
        String message,
        LocalDateTime timestamp,
        String path
) {
    public static ErrorResponse of(ErrorCode errorCode, String path) {
        return new ErrorResponse(
                errorCode.name(),
                errorCode.getMessage(),
                LocalDateTime.now(),
                path
        );
    }
}
