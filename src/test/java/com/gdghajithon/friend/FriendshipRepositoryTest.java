package com.gdghajithon.friend;

import com.gdghajithon.user.User;
import com.gdghajithon.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class FriendshipRepositoryTest {

    @Autowired private FriendshipRepository friendshipRepository;
    @Autowired private UserRepository userRepository;

    @Test
    void returnsFriendsFromBothNormalizedSidesAndExcludesUnrelatedRelationships() {
        User first = saveUser("first");
        User current = saveUser("current");
        User last = saveUser("last");
        User unrelated = saveUser("unrelated");
        friendshipRepository.save(Friendship.create(first, current));
        friendshipRepository.save(Friendship.create(current, last));
        friendshipRepository.save(Friendship.create(last, unrelated));

        assertThat(friendshipRepository.findFriendIdsByUserId(current.getId()))
                .containsExactly(first.getId(), last.getId())
                .doesNotContain(unrelated.getId());
    }

    private User saveUser(String loginId) {
        return userRepository.saveAndFlush(User.create(loginId, "encoded-password"));
    }
}
