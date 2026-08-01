package com.gdghajithon.friendrequest;

import com.gdghajithon.friend.Friendship;
import com.gdghajithon.friend.FriendshipRepository;
import com.gdghajithon.friendrequest.dto.FriendRequestCreateRequest;
import com.gdghajithon.friendrequest.dto.FriendRequestCreateResponse;
import com.gdghajithon.friendrequest.dto.FriendRequestListResponse;
import com.gdghajithon.friendrequest.dto.FriendRequestRespondRequest;
import com.gdghajithon.friendrequest.dto.FriendRequestStatusResponse;
import com.gdghajithon.global.exception.BusinessException;
import com.gdghajithon.global.exception.ErrorCode;
import com.gdghajithon.profile.ExerciseLevel;
import com.gdghajithon.profile.Gender;
import com.gdghajithon.profile.Profile;
import com.gdghajithon.profile.ProfileRepository;
import com.gdghajithon.region.Region;
import com.gdghajithon.region.RegionRepository;
import com.gdghajithon.sport.Sport;
import com.gdghajithon.sport.SportRepository;
import com.gdghajithon.user.User;
import com.gdghajithon.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
@Import(FriendRequestService.class)
class FriendRequestServiceTest {

    @Autowired private FriendRequestService friendRequestService;
    @Autowired private FriendRequestRepository friendRequestRepository;
    @Autowired private FriendshipRepository friendshipRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ProfileRepository profileRepository;
    @Autowired private SportRepository sportRepository;
    @Autowired private RegionRepository regionRepository;

    private User sender;
    private User receiver;
    private Sport sport;
    private Region region;

    @BeforeEach
    void setUp() {
        sport = saveSport("러닝");
        region = saveRegion("강남구");
        sender = saveUser("sender");
        receiver = saveUser("receiver");
        saveProfile(sender, "보낸 사람");
        saveProfile(receiver, "받은 사람");
    }

    @Test
    void createsRequestAndReturnsRequestId() {
        FriendRequestCreateResponse response = create(sender, receiver);

        assertThat(response.requestId()).isNotNull();
        assertThat(response.senderUserId()).isEqualTo(sender.getId());
        assertThat(response.receiverUserId()).isEqualTo(receiver.getId());
        assertThat(response.status()).isEqualTo(FriendRequestStatus.PENDING);
    }

    @Test
    void cannotRequestSelf() {
        assertError(
                () -> create(sender, sender),
                ErrorCode.CANNOT_REQUEST_SELF
        );
    }

    @Test
    void failsWhenReceiverDoesNotExist() {
        assertError(
                () -> friendRequestService.create(
                        sender.getId(), new FriendRequestCreateRequest(Long.MAX_VALUE)),
                ErrorCode.USER_NOT_FOUND
        );
    }

    @Test
    void failsWhenSenderOrReceiverProfileDoesNotExist() {
        User senderWithoutProfile = saveUser("senderWithoutProfile");
        User receiverWithoutProfile = saveUser("receiverWithoutProfile");

        assertError(
                () -> create(senderWithoutProfile, receiver),
                ErrorCode.PROFILE_NOT_FOUND
        );
        assertError(
                () -> create(sender, receiverWithoutProfile),
                ErrorCode.PROFILE_NOT_FOUND
        );
    }

    @Test
    void failsWhenAlreadyFriends() {
        friendshipRepository.saveAndFlush(Friendship.create(sender, receiver));

        assertError(() -> create(sender, receiver), ErrorCode.ALREADY_FRIENDS);
    }

    @Test
    void rejectsSameAndOppositeDirectionPendingRequests() {
        create(sender, receiver);

        assertError(
                () -> create(sender, receiver),
                ErrorCode.FRIEND_REQUEST_ALREADY_PENDING
        );
        assertError(
                () -> create(receiver, sender),
                ErrorCode.FRIEND_REQUEST_ALREADY_PENDING
        );
    }

    @Test
    void receivedAndSentListsUseCounterpartProfile() {
        create(sender, receiver);

        FriendRequestListResponse received = friendRequestService.getReceived(receiver.getId());
        FriendRequestListResponse sent = friendRequestService.getSent(sender.getId());

        assertThat(received.items()).singleElement()
                .satisfies(item -> assertThat(item.user().id()).isEqualTo(sender.getId()));
        assertThat(sent.items()).singleElement()
                .satisfies(item -> assertThat(item.user().id()).isEqualTo(receiver.getId()));
    }

    @Test
    void receiverAcceptsAndCreatesNormalizedFriendship() {
        Long requestId = create(sender, receiver).requestId();

        FriendRequestStatusResponse response = friendRequestService.respond(
                receiver.getId(), requestId,
                new FriendRequestRespondRequest(FriendRequestStatus.ACCEPTED));

        assertThat(response.status()).isEqualTo(FriendRequestStatus.ACCEPTED);
        assertThat(friendshipRepository.existsByUserAIdAndUserBId(
                Math.min(sender.getId(), receiver.getId()),
                Math.max(sender.getId(), receiver.getId()))).isTrue();
        assertThat(friendRequestRepository.findById(requestId).orElseThrow().getPendingPairKey())
                .isNull();
    }

    @Test
    void receiverRejectsRequest() {
        Long requestId = create(sender, receiver).requestId();

        FriendRequestStatusResponse response = friendRequestService.respond(
                receiver.getId(), requestId,
                new FriendRequestRespondRequest(FriendRequestStatus.REJECTED));

        assertThat(response.status()).isEqualTo(FriendRequestStatus.REJECTED);
        assertThat(friendshipRepository.count()).isZero();
    }

    @Test
    void rejectedRequestCanBeCreatedAgain() {
        Long requestId = create(sender, receiver).requestId();
        friendRequestService.respond(
                receiver.getId(), requestId,
                new FriendRequestRespondRequest(FriendRequestStatus.REJECTED));

        FriendRequestCreateResponse response = create(sender, receiver);

        assertThat(response.status()).isEqualTo(FriendRequestStatus.PENDING);
        assertThat(friendRequestRepository.count()).isEqualTo(2);
    }

    @Test
    void pendingCannotBeUsedAsResponseStatusAtServiceBoundary() {
        Long requestId = create(sender, receiver).requestId();

        assertError(
                () -> friendRequestService.respond(
                        receiver.getId(), requestId,
                        new FriendRequestRespondRequest(FriendRequestStatus.PENDING)),
                ErrorCode.VALIDATION_ERROR
        );
    }

    @Test
    void emptyReceivedAndSentListsReturnEmptyItems() {
        assertThat(friendRequestService.getReceived(receiver.getId()).items()).isEmpty();
        assertThat(friendRequestService.getSent(sender.getId()).items()).isEmpty();
    }

    @Test
    void senderAndThirdPartyCannotRespond() {
        Long requestId = create(sender, receiver).requestId();
        User thirdParty = saveUser("thirdParty");

        assertError(
                () -> friendRequestService.respond(
                        sender.getId(), requestId,
                        new FriendRequestRespondRequest(FriendRequestStatus.ACCEPTED)),
                ErrorCode.FRIEND_REQUEST_ACCESS_DENIED
        );
        assertError(
                () -> friendRequestService.respond(
                        thirdParty.getId(), requestId,
                        new FriendRequestRespondRequest(FriendRequestStatus.REJECTED)),
                ErrorCode.FRIEND_REQUEST_ACCESS_DENIED
        );
    }

    @Test
    void senderCancelsPendingRequestButReceiverCannot() {
        Long requestId = create(sender, receiver).requestId();

        assertError(
                () -> friendRequestService.cancel(receiver.getId(), requestId),
                ErrorCode.FRIEND_REQUEST_ACCESS_DENIED
        );
        friendRequestService.cancel(sender.getId(), requestId);

        assertThat(friendRequestRepository.findById(requestId)).isEmpty();
    }

    @Test
    void processedRequestCannotBeRespondedToOrCancelled() {
        Long requestId = create(sender, receiver).requestId();
        friendRequestService.respond(
                receiver.getId(), requestId,
                new FriendRequestRespondRequest(FriendRequestStatus.REJECTED));

        assertError(
                () -> friendRequestService.respond(
                        receiver.getId(), requestId,
                        new FriendRequestRespondRequest(FriendRequestStatus.ACCEPTED)),
                ErrorCode.FRIEND_REQUEST_ALREADY_PROCESSED
        );
        assertError(
                () -> friendRequestService.cancel(sender.getId(), requestId),
                ErrorCode.FRIEND_REQUEST_ALREADY_PROCESSED
        );
    }

    private FriendRequestCreateResponse create(User requestSender, User requestReceiver) {
        return friendRequestService.create(
                requestSender.getId(),
                new FriendRequestCreateRequest(requestReceiver.getId())
        );
    }

    private User saveUser(String loginId) {
        return userRepository.saveAndFlush(User.create(loginId, "encoded-password"));
    }

    private Sport saveSport(String name) {
        Sport entity = BeanUtils.instantiateClass(Sport.class);
        ReflectionTestUtils.setField(entity, "name", name);
        return sportRepository.saveAndFlush(entity);
    }

    private Region saveRegion(String name) {
        Region entity = BeanUtils.instantiateClass(Region.class);
        ReflectionTestUtils.setField(entity, "name", name);
        return regionRepository.saveAndFlush(entity);
    }

    private Profile saveProfile(User user, String name) {
        return profileRepository.saveAndFlush(Profile.create(
                user, name, 25, Gender.MALE, sport,
                ExerciseLevel.INTERMEDIATE, region));
    }

    private void assertError(
            org.assertj.core.api.ThrowableAssert.ThrowingCallable action,
            ErrorCode expected
    ) {
        assertThatThrownBy(action)
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(expected);
    }
}
