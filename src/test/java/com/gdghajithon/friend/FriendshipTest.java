package com.gdghajithon.friend;

import com.gdghajithon.global.exception.BusinessException;
import com.gdghajithon.global.exception.ErrorCode;
import com.gdghajithon.user.User;
import com.gdghajithon.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
class FriendshipTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void smallerUserIdBecomesUserA() {
        User smaller = saveUser("smaller");
        User larger = saveUser("larger");

        Friendship friendship = Friendship.create(larger, smaller);

        assertThat(friendship.getUserA().getId()).isEqualTo(smaller.getId());
    }

    @Test
    void largerUserIdBecomesUserB() {
        User smaller = saveUser("smaller");
        User larger = saveUser("larger");

        Friendship friendship = Friendship.create(larger, smaller);

        assertThat(friendship.getUserB().getId()).isEqualTo(larger.getId());
    }

    @Test
    void cannotCreateFriendshipWithSelf() {
        User user = saveUser("self");

        assertThatThrownBy(() -> Friendship.create(user, user))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.CANNOT_FRIEND_SELF);
    }

    private User saveUser(String loginId) {
        return userRepository.save(User.create(loginId, "encoded-password"));
    }
}
