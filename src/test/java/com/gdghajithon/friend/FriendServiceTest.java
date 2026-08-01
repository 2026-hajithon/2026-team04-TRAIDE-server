package com.gdghajithon.friend;

import com.gdghajithon.global.exception.BusinessException;
import com.gdghajithon.global.exception.ErrorCode;
import com.gdghajithon.user.User;
import com.gdghajithon.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
@Import(FriendService.class)
class FriendServiceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FriendshipRepository friendshipRepository;

    @Autowired
    private FriendService friendService;

    @Test
    void validateFriendSucceedsWhenFriendshipExists() {
        User first = saveUser("first");
        User second = saveUser("second");
        friendshipRepository.save(Friendship.create(first, second));

        assertThatCode(() -> friendService.validateFriend(second.getId(), first.getId()))
                .doesNotThrowAnyException();
    }

    @Test
    void validateFriendFailsWhenFriendshipDoesNotExist() {
        User first = saveUser("first");
        User second = saveUser("second");

        assertThatThrownBy(() -> friendService.validateFriend(first.getId(), second.getId()))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.FRIENDSHIP_REQUIRED);
    }

    @Test
    void validateFriendFailsWhenUserDoesNotExist() {
        User user = saveUser("existing");

        assertThatThrownBy(() -> friendService.validateFriend(user.getId(), Long.MAX_VALUE))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    private User saveUser(String loginId) {
        return userRepository.save(User.create(loginId, "encoded-password"));
    }
}
