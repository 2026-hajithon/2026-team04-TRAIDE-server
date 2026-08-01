package com.gdghajithon.friendrequest;

import com.gdghajithon.friend.FriendshipRepository;
import com.gdghajithon.friendrequest.dto.FriendRequestCreateRequest;
import com.gdghajithon.friendrequest.dto.FriendRequestRespondRequest;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class FriendRequestConcurrencyTest {

    @Autowired private FriendRequestService friendRequestService;
    @Autowired private FriendRequestRepository friendRequestRepository;
    @Autowired private FriendshipRepository friendshipRepository;
    @Autowired private ProfileRepository profileRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private SportRepository sportRepository;
    @Autowired private RegionRepository regionRepository;

    private final ExecutorService executor = Executors.newFixedThreadPool(2);
    private User first;
    private User second;

    @BeforeEach
    void setUp() {
        friendRequestRepository.deleteAll();
        friendshipRepository.deleteAll();
        profileRepository.deleteAll();
        userRepository.deleteAll();
        sportRepository.deleteAll();
        regionRepository.deleteAll();

        Sport sport = BeanUtils.instantiateClass(Sport.class);
        ReflectionTestUtils.setField(sport, "name", "러닝");
        sport = sportRepository.save(sport);
        Region region = BeanUtils.instantiateClass(Region.class);
        ReflectionTestUtils.setField(region, "name", "강남구");
        region = regionRepository.save(region);
        first = userRepository.save(User.create("concurrentFirst", "encoded-password"));
        second = userRepository.save(User.create("concurrentSecond", "encoded-password"));
        profileRepository.save(Profile.create(
                first, "첫 번째", 25, Gender.MALE, sport,
                ExerciseLevel.INTERMEDIATE, region));
        profileRepository.save(Profile.create(
                second, "두 번째", 25, Gender.FEMALE, sport,
                ExerciseLevel.INTERMEDIATE, region));
    }

    @AfterEach
    void tearDown() {
        executor.shutdownNow();
    }

    @Test
    void concurrentOppositeRequestsCreateOnlyOnePendingRequest() throws Exception {
        CountDownLatch start = new CountDownLatch(1);
        Future<CallResult> firstCall = executor.submit(() -> createAfter(start, first, second));
        Future<CallResult> secondCall = executor.submit(() -> createAfter(start, second, first));
        start.countDown();

        List<CallResult> results = List.of(
                firstCall.get(10, TimeUnit.SECONDS),
                secondCall.get(10, TimeUnit.SECONDS));

        assertThat(results).filteredOn(CallResult::success).hasSize(1);
        assertThat(results).filteredOn(result -> !result.success())
                .extracting(CallResult::errorCode)
                .containsExactly(ErrorCode.FRIEND_REQUEST_ALREADY_PENDING);
        assertThat(friendRequestRepository.count()).isEqualTo(1);
    }

    @Test
    void concurrentAcceptanceCreatesOneFriendship() throws Exception {
        Long requestId = friendRequestService.create(
                first.getId(), new FriendRequestCreateRequest(second.getId())).requestId();
        CountDownLatch start = new CountDownLatch(1);
        Future<CallResult> firstCall = executor.submit(() -> acceptAfter(start, requestId));
        Future<CallResult> secondCall = executor.submit(() -> acceptAfter(start, requestId));
        start.countDown();

        List<CallResult> results = List.of(
                firstCall.get(10, TimeUnit.SECONDS),
                secondCall.get(10, TimeUnit.SECONDS));

        assertThat(results).filteredOn(CallResult::success).hasSize(1);
        assertThat(results).filteredOn(result -> !result.success())
                .extracting(CallResult::errorCode)
                .containsExactly(ErrorCode.FRIEND_REQUEST_ALREADY_PROCESSED);
        assertThat(friendshipRepository.count()).isEqualTo(1);
    }

    private CallResult createAfter(CountDownLatch start, User sender, User receiver)
            throws InterruptedException {
        start.await();
        try {
            friendRequestService.create(
                    sender.getId(), new FriendRequestCreateRequest(receiver.getId()));
            return CallResult.succeeded();
        } catch (BusinessException exception) {
            return CallResult.failed(exception.getErrorCode());
        }
    }

    private CallResult acceptAfter(CountDownLatch start, Long requestId)
            throws InterruptedException {
        start.await();
        try {
            friendRequestService.respond(
                    second.getId(), requestId,
                    new FriendRequestRespondRequest(FriendRequestStatus.ACCEPTED));
            return CallResult.succeeded();
        } catch (BusinessException exception) {
            return CallResult.failed(exception.getErrorCode());
        }
    }

    private record CallResult(boolean success, ErrorCode errorCode) {
        private static CallResult succeeded() {
            return new CallResult(true, null);
        }

        private static CallResult failed(ErrorCode errorCode) {
            return new CallResult(false, errorCode);
        }
    }
}
