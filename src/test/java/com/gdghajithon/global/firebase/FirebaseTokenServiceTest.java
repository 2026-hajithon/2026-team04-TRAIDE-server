package com.gdghajithon.global.firebase;

import com.google.firebase.auth.FirebaseAuth;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FirebaseTokenServiceTest {

    @Mock
    FirebaseConfig firebaseConfig;

    @Mock
    FirebaseAuth firebaseAuth;

    @Test
    void customTokenCanBeCreated() throws Exception {
        FirebaseTokenService firebaseTokenService = new FirebaseTokenService(firebaseConfig);
        when(firebaseConfig.firebaseAuth()).thenReturn(firebaseAuth);
        when(firebaseAuth.createCustomToken("1")).thenReturn("firebase-token");

        String token = firebaseTokenService.createToken(1L);

        assertThat(token).isEqualTo("firebase-token");
    }

    @Test
    void returnsNullWhenFirebaseIsNotInitialized() {
        FirebaseTokenService firebaseTokenService = new FirebaseTokenService(firebaseConfig);
        when(firebaseConfig.firebaseAuth()).thenReturn(null);

        String token = firebaseTokenService.createToken(1L);

        assertThat(token).isNull();
    }

    @Test
    void returnsNullWhenTokenCreationFails() throws Exception {
        FirebaseTokenService firebaseTokenService = new FirebaseTokenService(firebaseConfig);
        when(firebaseConfig.firebaseAuth()).thenReturn(firebaseAuth);
        when(firebaseAuth.createCustomToken("1"))
                .thenThrow(new IllegalStateException("token creation failed"));

        String token = firebaseTokenService.createToken(1L);

        assertThat(token).isNull();
    }
}
