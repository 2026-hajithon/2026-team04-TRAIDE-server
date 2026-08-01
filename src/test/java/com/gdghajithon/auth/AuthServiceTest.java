package com.gdghajithon.auth;

import com.gdghajithon.auth.dto.AuthResponse;
import com.gdghajithon.auth.dto.LoginRequest;
import com.gdghajithon.auth.dto.SignupRequest;
import com.gdghajithon.global.exception.BusinessException;
import com.gdghajithon.global.exception.ErrorCode;
import com.gdghajithon.global.firebase.FirebaseTokenService;
import com.gdghajithon.global.security.JwtTokenProvider;
import com.gdghajithon.user.User;
import com.gdghajithon.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private FirebaseTokenService firebaseTokenService;

    private PasswordEncoder passwordEncoder;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        authService = new AuthService(
                userRepository,
                passwordEncoder,
                jwtTokenProvider,
                firebaseTokenService
        );
    }

    @Test
    void signupSucceeds() {
        prepareSignupSave();
        when(jwtTokenProvider.createToken(1L)).thenReturn("access-token");
        when(firebaseTokenService.createToken(1L)).thenReturn("firebase-token");

        AuthResponse response = authService.signup(new SignupRequest("user01", "password123"));

        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.firebaseToken()).isEqualTo("firebase-token");
    }

    @Test
    void duplicateLoginIdSignupFails() {
        when(userRepository.existsByLoginId("user01")).thenReturn(true);

        assertThatThrownBy(() -> authService.signup(new SignupRequest("user01", "password123")))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.LOGIN_ID_ALREADY_EXISTS);
        verify(userRepository, never()).save(any());
    }

    @Test
    void savedPasswordIsNotPlainText() {
        prepareSignupSave();

        authService.signup(new SignupRequest("user01", "password123"));

        User savedUser = captureSavedUser();
        assertThat(savedUser.getPassword()).isNotEqualTo("password123");
    }

    @Test
    void savedPasswordMatchesWithPasswordEncoder() {
        prepareSignupSave();

        authService.signup(new SignupRequest("user01", "password123"));

        User savedUser = captureSavedUser();
        assertThat(passwordEncoder.matches("password123", savedUser.getPassword())).isTrue();
    }

    @Test
    void loginSucceedsAndIssuesJwt() {
        User user = persistedUser("user01", passwordEncoder.encode("password123"), 1L);
        when(userRepository.findByLoginId("user01")).thenReturn(Optional.of(user));
        when(jwtTokenProvider.createToken(1L)).thenReturn("access-token");
        when(firebaseTokenService.createToken(1L)).thenReturn("firebase-token");

        AuthResponse response = authService.login(new LoginRequest("user01", "password123"));

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.firebaseToken()).isEqualTo("firebase-token");
        verify(jwtTokenProvider).createToken(1L);
    }

    @Test
    void loginFailsWhenLoginIdDoesNotExist() {
        when(userRepository.findByLoginId("unknown")).thenReturn(Optional.empty());

        assertInvalidCredentials(() ->
                authService.login(new LoginRequest("unknown", "password123")));
    }

    @Test
    void loginFailsWhenPasswordIsIncorrect() {
        User user = persistedUser("user01", passwordEncoder.encode("password123"), 1L);
        when(userRepository.findByLoginId("user01")).thenReturn(Optional.of(user));

        assertInvalidCredentials(() ->
                authService.login(new LoginRequest("user01", "wrong-password")));
    }

    private void prepareSignupSave() {
        when(userRepository.existsByLoginId("user01")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            ReflectionTestUtils.setField(user, "id", 1L);
            return user;
        });
    }

    private User captureSavedUser() {
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        return captor.getValue();
    }

    private User persistedUser(String loginId, String encodedPassword, Long id) {
        User user = User.create(loginId, encodedPassword);
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private void assertInvalidCredentials(org.assertj.core.api.ThrowableAssert.ThrowingCallable action) {
        assertThatThrownBy(action)
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_CREDENTIALS);
    }
}
