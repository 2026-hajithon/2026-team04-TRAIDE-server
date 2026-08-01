package com.gdghajithon.global.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties(
                "test-jwt-secret-must-be-at-least-32-bytes-long",
                3600
        );
        jwtTokenProvider = new JwtTokenProvider(properties);
    }

    @Test
    void tokenSubjectContainsUserId() {
        String token = jwtTokenProvider.createToken(42L);

        assertThat(jwtTokenProvider.getUserId(token)).isEqualTo(42L);
    }

    @Test
    void invalidTokenFailsValidation() {
        assertThat(jwtTokenProvider.validateToken("invalid.jwt.token")).isFalse();
    }
}
