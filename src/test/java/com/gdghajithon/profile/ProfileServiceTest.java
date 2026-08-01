package com.gdghajithon.profile;

import com.gdghajithon.appointment.Appointment;
import com.gdghajithon.appointment.AppointmentRepository;
import com.gdghajithon.friend.Friendship;
import com.gdghajithon.friend.FriendshipRepository;
import com.gdghajithon.friend.FriendService;
import com.gdghajithon.friendrequest.FriendRequest;
import com.gdghajithon.friendrequest.FriendRequestRepository;
import com.gdghajithon.global.exception.BusinessException;
import com.gdghajithon.global.exception.ErrorCode;
import com.gdghajithon.profile.dto.MyProfileResponse;
import com.gdghajithon.profile.dto.ProfileCreateRequest;
import com.gdghajithon.profile.dto.ProfileUpdateRequest;
import com.gdghajithon.profile.dto.UserDetailResponse;
import com.gdghajithon.profile.dto.UserRecommendationListResponse;
import com.gdghajithon.region.Region;
import com.gdghajithon.region.RegionRepository;
import com.gdghajithon.review.Review;
import com.gdghajithon.review.ReviewQueryService;
import com.gdghajithon.review.ReviewRepository;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
@Import({ProfileService.class, ReviewQueryService.class, FriendService.class})
class ProfileServiceTest {

    @Autowired private ProfileService profileService;
    @Autowired private ProfileRepository profileRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private SportRepository sportRepository;
    @Autowired private RegionRepository regionRepository;
    @Autowired private FriendshipRepository friendshipRepository;
    @Autowired private FriendRequestRepository friendRequestRepository;
    @Autowired private AppointmentRepository appointmentRepository;
    @Autowired private ReviewRepository reviewRepository;
    @Autowired private FriendService friendService;

    private User currentUser;
    private Sport sport;
    private Region region;

    @BeforeEach
    void setUp() {
        currentUser = saveUser("current");
        sport = saveSport("러닝");
        region = saveRegion("강남구");
    }

    @Test
    void createsProfile() {
        MyProfileResponse response = profileService.create(currentUser.getId(), createRequest());

        assertThat(response.id()).isEqualTo(currentUser.getId());
        assertThat(response.loginId()).isEqualTo("current");
        assertThat(profileRepository.existsByUserId(currentUser.getId())).isTrue();
    }

    @Test
    void duplicateProfileFails() {
        profileService.create(currentUser.getId(), createRequest());

        assertError(
                () -> profileService.create(currentUser.getId(), createRequest()),
                ErrorCode.PROFILE_ALREADY_EXISTS
        );
    }

    @Test
    void createFailsWhenUserDoesNotExist() {
        assertError(
                () -> profileService.create(Long.MAX_VALUE, createRequest()),
                ErrorCode.USER_NOT_FOUND
        );
    }

    @Test
    void createFailsWhenSportDoesNotExist() {
        ProfileCreateRequest request = new ProfileCreateRequest(
                "사용자", 25, Gender.MALE, Long.MAX_VALUE,
                ExerciseLevel.INTERMEDIATE, region.getId());

        assertError(
                () -> profileService.create(currentUser.getId(), request),
                ErrorCode.SPORT_NOT_FOUND
        );
    }

    @Test
    void createFailsWhenRegionDoesNotExist() {
        ProfileCreateRequest request = new ProfileCreateRequest(
                "사용자", 25, Gender.MALE, sport.getId(),
                ExerciseLevel.INTERMEDIATE, Long.MAX_VALUE);

        assertError(
                () -> profileService.create(currentUser.getId(), request),
                ErrorCode.REGION_NOT_FOUND
        );
    }

    @Test
    void getsMyProfile() {
        profileService.create(currentUser.getId(), createRequest());

        MyProfileResponse response = profileService.getMyProfile(currentUser.getId());

        assertThat(response.name()).isEqualTo("사용자");
        assertThat(response.sport().name()).isEqualTo("러닝");
        assertThat(response.region().name()).isEqualTo("강남구");
        assertThat(response.averageRating()).isNull();
        assertThat(response.reviewCount()).isZero();
    }

    @Test
    void profileReturnsAverageRatingAndReviewCount() {
        saveProfile(currentUser, "현재");
        User writer = saveUser("writer");
        reviewRepository.save(Review.create(writer, currentUser, 4, null, null));
        reviewRepository.saveAndFlush(Review.create(writer, currentUser, 5, null, null));

        MyProfileResponse response = profileService.getMyProfile(currentUser.getId());

        assertThat(response.averageRating()).isEqualTo(4.5);
        assertThat(response.reviewCount()).isEqualTo(2);
    }

    @Test
    void updatesOnlyName() {
        profileService.create(currentUser.getId(), createRequest());
        ProfileUpdateRequest request = new ProfileUpdateRequest(
                "수정 사용자", null, null, null, null, null);

        MyProfileResponse response = profileService.update(currentUser.getId(), request);

        assertThat(response.name()).isEqualTo("수정 사용자");
        assertThat(response.age()).isEqualTo(25);
        assertThat(response.gender()).isEqualTo(Gender.MALE);
        assertThat(response.sport().id()).isEqualTo(sport.getId());
        assertThat(response.level()).isEqualTo(ExerciseLevel.INTERMEDIATE);
        assertThat(response.region().id()).isEqualTo(region.getId());
    }

    @Test
    void updatesOnlySport() {
        profileService.create(currentUser.getId(), createRequest());
        Sport newSport = saveSport("축구");
        ProfileUpdateRequest request = new ProfileUpdateRequest(
                null, null, null, newSport.getId(), null, null);

        MyProfileResponse response = profileService.update(currentUser.getId(), request);

        assertThat(response.sport().id()).isEqualTo(newSport.getId());
        assertThat(response.level()).isEqualTo(ExerciseLevel.INTERMEDIATE);
        assertThat(response.name()).isEqualTo("사용자");
        assertThat(response.region().id()).isEqualTo(region.getId());
    }

    @Test
    void updatesOnlyLevel() {
        profileService.create(currentUser.getId(), createRequest());
        ProfileUpdateRequest request = new ProfileUpdateRequest(
                null, null, null, null, ExerciseLevel.ADVANCED, null);

        MyProfileResponse response = profileService.update(currentUser.getId(), request);

        assertThat(response.level()).isEqualTo(ExerciseLevel.ADVANCED);
        assertThat(response.sport().id()).isEqualTo(sport.getId());
        assertThat(response.region().id()).isEqualTo(region.getId());
    }

    @Test
    void emptyUpdateKeepsEveryValue() {
        MyProfileResponse before = profileService.create(currentUser.getId(), createRequest());

        MyProfileResponse after = profileService.update(
                currentUser.getId(), new ProfileUpdateRequest(null, null, null, null, null, null));

        assertThat(after.name()).isEqualTo(before.name());
        assertThat(after.age()).isEqualTo(before.age());
        assertThat(after.gender()).isEqualTo(before.gender());
        assertThat(after.sport()).isEqualTo(before.sport());
        assertThat(after.level()).isEqualTo(before.level());
        assertThat(after.region()).isEqualTo(before.region());
    }

    @Test
    void userDetailReturnsNoneForNonFriend() {
        saveProfile(currentUser, "현재");
        User target = saveUser("target");
        saveProfile(target, "대상");

        UserDetailResponse response =
                profileService.getUserDetail(currentUser.getId(), target.getId());

        assertThat(response.friendStatus()).isEqualTo(FriendStatus.NONE);
        assertThat(response.friendSinceDays()).isNull();
    }

    @Test
    void userDetailReturnsPendingForSentAndReceivedRequests() {
        saveProfile(currentUser, "현재");
        User sentTarget = saveUser("sentTarget");
        User receivedTarget = saveUser("receivedTarget");
        saveProfile(sentTarget, "보낸 대상");
        saveProfile(receivedTarget, "받은 대상");
        friendRequestRepository.save(FriendRequest.create(currentUser, sentTarget));
        friendRequestRepository.saveAndFlush(FriendRequest.create(receivedTarget, currentUser));

        UserDetailResponse sent =
                profileService.getUserDetail(currentUser.getId(), sentTarget.getId());
        UserDetailResponse received =
                profileService.getUserDetail(currentUser.getId(), receivedTarget.getId());

        assertThat(sent.friendStatus()).isEqualTo(FriendStatus.PENDING);
        assertThat(sent.friendSinceDays()).isNull();
        assertThat(received.friendStatus()).isEqualTo(FriendStatus.PENDING);
        assertThat(received.friendSinceDays()).isNull();
    }

    @Test
    void friendshipTakesPriorityOverPendingRequest() {
        saveProfile(currentUser, "현재");
        User target = saveUser("targetPriority");
        saveProfile(target, "대상");
        friendRequestRepository.saveAndFlush(FriendRequest.create(currentUser, target));
        friendshipRepository.saveAndFlush(Friendship.create(currentUser, target));

        UserDetailResponse response =
                profileService.getUserDetail(currentUser.getId(), target.getId());

        assertThat(response.friendStatus()).isEqualTo(FriendStatus.FRIEND);
        assertThat(response.friendSinceDays()).isEqualTo(1);
    }

    @Test
    void userDetailReturnsOneFriendSinceDayWhenFriendshipWasCreatedToday() {
        saveProfile(currentUser, "현재");
        User target = saveUser("target");
        saveProfile(target, "대상");
        Friendship friendship = friendshipRepository.saveAndFlush(
                Friendship.create(currentUser, target));

        UserDetailResponse response =
                profileService.getUserDetail(currentUser.getId(), target.getId());

        assertThat(response.friendStatus()).isEqualTo(FriendStatus.FRIEND);
        assertThat(response.friendSinceDays()).isEqualTo(1);
    }

    @Test
    void deletingFriendChangesDetailAndRecommendationWithoutChangingProfileService() {
        saveProfile(currentUser, "현재");
        User target = saveUser("target");
        saveProfile(target, "대상");
        friendshipRepository.saveAndFlush(Friendship.create(currentUser, target));

        assertThat(profileService.getUserDetail(currentUser.getId(), target.getId()).friendStatus())
                .isEqualTo(FriendStatus.FRIEND);

        friendService.deleteFriend(currentUser.getId(), target.getId());
        friendshipRepository.flush();

        UserDetailResponse detail = profileService.getUserDetail(currentUser.getId(), target.getId());
        assertThat(detail.friendStatus()).isEqualTo(FriendStatus.NONE);
        assertThat(detail.friendSinceDays()).isNull();
        assertThat(profileService.getRecommendations(currentUser.getId(), null, null).items())
                .extracting("id")
                .contains(target.getId());

        friendRequestRepository.saveAndFlush(FriendRequest.create(currentUser, target));
        assertThat(profileService.getRecommendations(currentUser.getId(), null, null).items())
                .extracting("id")
                .doesNotContain(target.getId());
    }

    @Test
    void userDetailReturnsTwoFriendSinceDaysWhenFriendshipWasCreatedYesterday() {
        saveProfile(currentUser, "현재");
        User target = saveUser("target");
        saveProfile(target, "대상");
        Friendship friendship = friendshipRepository.saveAndFlush(
                Friendship.create(currentUser, target));
        ReflectionTestUtils.setField(
                friendship, "createdAt", LocalDateTime.now().minusDays(1));

        UserDetailResponse response =
                profileService.getUserDetail(currentUser.getId(), target.getId());

        assertThat(response.friendSinceDays()).isEqualTo(2);
    }

    @Test
    void calculatesFriendCount() {
        saveProfile(currentUser, "현재");
        User friendOne = saveUser("friendOne");
        User friendTwo = saveUser("friendTwo");
        friendshipRepository.save(Friendship.create(currentUser, friendOne));
        friendshipRepository.saveAndFlush(Friendship.create(friendTwo, currentUser));

        assertThat(profileService.getMyProfile(currentUser.getId()).friendCount()).isEqualTo(2);
        assertThat(friendshipRepository.findFriendIdsByUserId(currentUser.getId()))
                .containsExactly(friendOne.getId(), friendTwo.getId());
    }

    @Test
    void userDetailCountsAllTargetAppointments() {
        saveProfile(currentUser, "현재");
        User target = saveUser("target");
        User other = saveUser("other");
        saveProfile(target, "대상");
        appointmentRepository.save(Appointment.create(
                target, currentUser, target, LocalDateTime.now().minusDays(1), "과거"));
        appointmentRepository.save(Appointment.create(
                other, target, target, LocalDateTime.now().plusDays(1), "미래"));

        UserDetailResponse response =
                profileService.getUserDetail(currentUser.getId(), target.getId());

        assertThat(response.appointmentCount()).isEqualTo(2);
    }

    @Test
    void recommendsEligibleProfiles() {
        saveProfile(currentUser, "현재");
        User candidate = saveUser("candidate");
        saveProfile(candidate, "후보");

        UserRecommendationListResponse response =
                profileService.getRecommendations(currentUser.getId(), null, null);

        assertThat(response.items())
                .extracting("id")
                .containsExactly(candidate.getId());
        assertThat(response.items().get(0).averageRating()).isNull();
        assertThat(response.items().get(0).reviewCount()).isZero();
    }

    @Test
    void recommendationReturnsEmptyList() {
        saveProfile(currentUser, "현재");

        assertThat(profileService.getRecommendations(currentUser.getId(), null, null).items())
                .isEmpty();
    }

    @Test
    void recommendationPrioritizesSameRegionAndReturnsAtMostTen() {
        saveProfile(currentUser, "현재");
        Region otherRegion = saveRegion("마포구");
        for (int index = 0; index < 3; index++) {
            saveProfile(saveUser("same" + index), "같은 지역" + index);
        }
        for (int index = 0; index < 10; index++) {
            User candidate = saveUser("other" + index);
            profileRepository.saveAndFlush(Profile.create(
                    candidate, "다른 지역" + index, 25, Gender.MALE, sport,
                    ExerciseLevel.INTERMEDIATE, otherRegion));
        }

        List<com.gdghajithon.profile.dto.UserRecommendationResponse> items =
                profileService.getRecommendations(currentUser.getId(), List.of(), List.of()).items();

        assertThat(items).hasSize(10);
        assertThat(items.subList(0, 3))
                .allMatch(item -> item.region().id().equals(region.getId()));
        assertThat(items.subList(3, 10))
                .allMatch(item -> item.region().id().equals(otherRegion.getId()));
    }

    private ProfileCreateRequest createRequest() {
        return new ProfileCreateRequest(
                "사용자", 25, Gender.MALE, sport.getId(),
                ExerciseLevel.INTERMEDIATE, region.getId());
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
            ErrorCode errorCode
    ) {
        assertThatThrownBy(action)
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(errorCode);
    }
}
