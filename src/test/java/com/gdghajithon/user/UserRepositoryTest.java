package com.gdghajithon.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByLoginId() {
        User saved = userRepository.save(User.create("runner01", "encoded-password"));

        assertThat(userRepository.findByLoginId("runner01"))
                .contains(saved);
    }

    @Test
    void existsByLoginId() {
        userRepository.save(User.create("runner01", "encoded-password"));

        assertThat(userRepository.existsByLoginId("runner01")).isTrue();
        assertThat(userRepository.existsByLoginId("unknown")).isFalse();
    }
}
