package com.gdghajithon.global.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class FirebaseConfig {

    private static final String CREDENTIALS_ENV = "GOOGLE_APPLICATION_CREDENTIALS";

    private FirebaseAuth firebaseAuth;

    @PostConstruct
    public void initialize() {
        String credentialsPath = System.getenv(CREDENTIALS_ENV);
        if (credentialsPath == null || credentialsPath.isBlank()) {
            firebaseAuth = null;
            log.info("Firebase 자격 증명 환경변수가 없어 초기화를 건너뜁니다.");
            return;
        }

        try {
            FirebaseApp firebaseApp = FirebaseApp.getApps().isEmpty()
                    ? initializeFirebaseApp()
                    : FirebaseApp.getInstance();
            firebaseAuth = FirebaseAuth.getInstance(firebaseApp);
            log.info("Firebase Admin SDK 초기화 완료");
        } catch (IOException | RuntimeException exception) {
            firebaseAuth = null;
            log.warn("Firebase Admin SDK 초기화 실패: {}", exception.getMessage());
        }
    }

    public FirebaseAuth firebaseAuth() {
        return firebaseAuth;
    }

    private FirebaseApp initializeFirebaseApp() throws IOException {
        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.getApplicationDefault())
                .build();
        return FirebaseApp.initializeApp(options);
    }
}
