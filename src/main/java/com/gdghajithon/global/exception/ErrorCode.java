package com.gdghajithon.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    CANNOT_FRIEND_SELF(HttpStatus.BAD_REQUEST, "자기 자신과 친구 관계를 맺을 수 없습니다."),
    FRIENDSHIP_REQUIRED(HttpStatus.FORBIDDEN, "친구 관계가 필요한 기능입니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다.");

    private final HttpStatus status;
    private final String message;
}
