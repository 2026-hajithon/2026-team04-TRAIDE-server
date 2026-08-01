package com.gdghajithon.friendrequest;

import com.gdghajithon.user.User;
import com.gdghajithon.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
class FriendRequestRepositoryTest {

    @Autowired private FriendRequestRepository friendRequestRepository;
    @Autowired private UserRepository userRepository;

    @Test
    void findsPendingRequestInBothDirections() {
        User first = saveUser("first");
        User second = saveUser("second");
        friendRequestRepository.saveAndFlush(FriendRequest.create(first, second));

        assertThat(friendRequestRepository.existsPendingBetweenUsers(first.getId(), second.getId()))
                .isTrue();
        assertThat(friendRequestRepository.existsPendingBetweenUsers(second.getId(), first.getId()))
                .isTrue();
    }

    @Test
    void receivedAndSentListsContainOnlyPendingRequestsInNewestFirstOrder() {
        User owner = saveUser("owner");
        User first = saveUser("first");
        User second = saveUser("second");
        User third = saveUser("third");
        FriendRequest older = friendRequestRepository.saveAndFlush(
                FriendRequest.create(first, owner));
        FriendRequest newer = friendRequestRepository.saveAndFlush(
                FriendRequest.create(second, owner));
        FriendRequest processed = friendRequestRepository.saveAndFlush(
                FriendRequest.create(owner, third));
        processed.reject();
        friendRequestRepository.flush();

        List<FriendRequest> received = friendRequestRepository
                .findAllByReceiverIdAndStatusOrderByCreatedAtDesc(
                        owner.getId(), FriendRequestStatus.PENDING);
        List<FriendRequest> sent = friendRequestRepository
                .findAllBySenderIdAndStatusOrderByCreatedAtDesc(
                        owner.getId(), FriendRequestStatus.PENDING);

        assertThat(received).extracting(FriendRequest::getId)
                .containsExactly(newer.getId(), older.getId());
        assertThat(sent).isEmpty();
    }

    @Test
    void uniquePendingPairPreventsOppositeDirectionRequest() {
        User first = saveUser("first");
        User second = saveUser("second");
        friendRequestRepository.saveAndFlush(FriendRequest.create(first, second));

        assertThatThrownBy(() -> friendRequestRepository.saveAndFlush(
                FriendRequest.create(second, first)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private User saveUser(String loginId) {
        return userRepository.saveAndFlush(User.create(loginId, "encoded-password"));
    }
}
