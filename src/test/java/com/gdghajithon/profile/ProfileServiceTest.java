package com.gdghajithon.profile;

import com.gdghajithon.friend.Friendship;
import com.gdghajithon.friend.FriendshipRepository;
import com.gdghajithon.global.exception.BusinessException;
import com.gdghajithon.global.exception.ErrorCode;
import com.gdghajithon.profile.dto.MyProfileResponse;
import com.gdghajithon.profile.dto.ProfileCreateRequest;
import com.gdghajithon.profile.dto.ProfileUpdateRequest;
import com.gdghajithon.profile.dto.UserDetailResponse;
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
@Import(ProfileService.class)
class ProfileServiceTest {

    @Autowired private ProfileService profileService;
    @Autowired private ProfileRepository profileRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private SportRepository sportRepository;
    @Autowired private RegionRepository regionRepository;
    @Autowired private FriendshipRepository friendshipRepository;

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

        assertThat(response.userId()).isEqualTo(currentUser.getId());
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
        assertThat(response.sportName()).isEqualTo("러닝");
        assertThat(response.regionName()).isEqualTo("강남구");
    }

    @Test
    void updatesProfile() {
        profileService.create(currentUser.getId(), createRequest());
        Sport newSport = saveSport("축구");
        Region newRegion = saveRegion("마포구");
        ProfileUpdateRequest request = new ProfileUpdateRequest(
                "수정 사용자", 30, Gender.FEMALE, newSport.getId(),
                ExerciseLevel.ADVANCED, newRegion.getId());

        MyProfileResponse response = profileService.update(currentUser.getId(), request);

        assertThat(response.name()).isEqualTo("수정 사용자");
        assertThat(response.gender()).isEqualTo(Gender.FEMALE);
        assertThat(response.sportId()).isEqualTo(newSport.getId());
        assertThat(response.regionId()).isEqualTo(newRegion.getId());
    }

    @Test
    void userDetailReturnsNoneForNonFriend() {
        saveProfile(currentUser, "현재");
        User target = saveUser("target");
        saveProfile(target, "대상");

        UserDetailResponse response =
                profileService.getUserDetail(currentUser.getId(), target.getId());

        assertThat(response.friendStatus()).isEqualTo(FriendStatus.NONE);
        assertThat(response.friendshipId()).isNull();
        assertThat(response.friendSince()).isNull();
    }

    @Test
    void userDetailReturnsFriendshipInformation() {
        saveProfile(currentUser, "현재");
        User target = saveUser("target");
        saveProfile(target, "대상");
        Friendship friendship = friendshipRepository.saveAndFlush(
                Friendship.create(currentUser, target));

        UserDetailResponse response =
                profileService.getUserDetail(currentUser.getId(), target.getId());

        assertThat(response.friendStatus()).isEqualTo(FriendStatus.FRIEND);
        assertThat(response.friendshipId()).isEqualTo(friendship.getId());
        assertThat(response.friendSince()).isEqualTo(friendship.getCreatedAt());
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
    void recommendsEligibleProfiles() {
        saveProfile(currentUser, "현재");
        User candidate = saveUser("candidate");
        saveProfile(candidate, "후보");

        assertThat(profileService.getRecommendations(currentUser.getId(), null, null))
                .extracting("userId")
                .containsExactly(candidate.getId());
    }

    @Test
    void recommendationReturnsEmptyList() {
        saveProfile(currentUser, "현재");

        assertThat(profileService.getRecommendations(currentUser.getId(), null, null)).isEmpty();
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
