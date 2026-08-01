package com.gdghajithon.global.firebase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FirebaseTokenService {

    private final FirebaseConfig firebaseConfig;

    public String createToken(Long userId) {
        FirebaseAuth firebaseAuth = firebaseConfig.firebaseAuth();
        if (firebaseAuth == null) {
            return null;
        }

        try {
            return firebaseAuth.createCustomToken(String.valueOf(userId));
        } catch (FirebaseAuthException | RuntimeException exception) {
            log.warn(
                    "Firebase 커스텀 토큰 발급 실패: userId={}, reason={}",
                    userId,
                    exception.getMessage()
            );
            return null;
        }
    }
}
