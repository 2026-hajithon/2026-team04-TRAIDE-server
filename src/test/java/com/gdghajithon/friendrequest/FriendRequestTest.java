package com.gdghajithon.friendrequest;

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
class FriendRequestTest {

    @Autowired private UserRepository userRepository;
    @Autowired private FriendRequestRepository friendRequestRepository;

    @Test
    void createsPendingRequestWithNormalizedPairKey() {
        User sender = saveUser("sender");
        User receiver = saveUser("receiver");

        FriendRequest request = friendRequestRepository.saveAndFlush(
                FriendRequest.create(receiver, sender));

        assertThat(request.getStatus()).isEqualTo(FriendRequestStatus.PENDING);
        assertThat(request.getPendingPairKey())
                .isEqualTo(sender.getId() + ":" + receiver.getId());
        assertThat(request.getCreatedAt()).isNotNull();
        assertThat(request.getRespondedAt()).isNull();
    }

    @Test
    void acceptAndRejectCompleteRequestAndClearPendingKey() {
        User sender = saveUser("sender");
        User receiver = saveUser("receiver");
        FriendRequest accepted = FriendRequest.create(sender, receiver);
        accepted.accept();
        FriendRequest rejected = FriendRequest.create(sender, receiver);
        rejected.reject();

        assertThat(accepted.getStatus()).isEqualTo(FriendRequestStatus.ACCEPTED);
        assertThat(accepted.getRespondedAt()).isNotNull();
        assertThat(accepted.getPendingPairKey()).isNull();
        assertThat(rejected.getStatus()).isEqualTo(FriendRequestStatus.REJECTED);
        assertThat(rejected.getRespondedAt()).isNotNull();
        assertThat(rejected.getPendingPairKey()).isNull();
    }

    @Test
    void processedRequestCannotBeProcessedAgain() {
        User sender = saveUser("sender");
        User receiver = saveUser("receiver");
        FriendRequest request = FriendRequest.create(sender, receiver);
        request.accept();

        assertThatThrownBy(request::reject)
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.FRIEND_REQUEST_ALREADY_PROCESSED);
    }

    private User saveUser(String loginId) {
        return userRepository.saveAndFlush(User.create(loginId, "encoded-password"));
    }
}
