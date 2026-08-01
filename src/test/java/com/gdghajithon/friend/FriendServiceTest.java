package com.gdghajithon.friend;

import com.gdghajithon.appointment.Appointment;
import com.gdghajithon.appointment.AppointmentRepository;
import com.gdghajithon.friend.dto.FriendListResponse;
import com.gdghajithon.friendrequest.FriendRequest;
import com.gdghajithon.friendrequest.FriendRequestRepository;
import com.gdghajithon.friendrequest.FriendRequestService;
import com.gdghajithon.friendrequest.FriendRequestStatus;
import com.gdghajithon.friendrequest.dto.FriendRequestCreateRequest;
import com.gdghajithon.friendrequest.dto.FriendRequestCreateResponse;
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
import jakarta.persistence.EntityManager;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
@Import({FriendService.class, FriendRequestService.class})
class FriendServiceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FriendshipRepository friendshipRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private SportRepository sportRepository;

    @Autowired
    private RegionRepository regionRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private FriendRequestRepository friendRequestRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private FriendService friendService;

    @Autowired
    private FriendRequestService friendRequestService;

    private Sport sport;
    private Region region;

    @BeforeEach
    void setUp() {
        sport = saveSport("테니스");
        region = saveRegion("강남구");
    }

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
                .isEqualTo(ErrorCode.NOT_FRIEND);
    }

    @Test
    void validateFriendFailsWhenUserDoesNotExist() {
        User user = saveUser("existing");

        assertThatThrownBy(() -> friendService.validateFriend(user.getId(), Long.MAX_VALUE))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    void returnsEmptyItemsWhenUserHasNoFriends() {
        User current = saveUser("current");

        assertThat(friendService.getFriends(current.getId()).items()).isEmpty();
    }

    @Test
    void returnsFriendProfileAppointmentCountAndStableChatRoomId() {
        User current = saveUser("current");
        User friend = saveUser("friend");
        saveProfile(friend, "이서연");
        friendshipRepository.save(Friendship.create(friend, current));
        appointmentRepository.save(Appointment.create(
                current, friend, current, LocalDateTime.now().minusDays(1), "과거"));
        appointmentRepository.save(Appointment.create(
                friend, current, friend, LocalDateTime.now().plusDays(1), "미래"));

        FriendListResponse response = friendService.getFriends(current.getId());

        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).id()).isEqualTo(friend.getId());
        assertThat(response.items().get(0).name()).isEqualTo("이서연");
        assertThat(response.items().get(0).sport().name()).isEqualTo("테니스");
        assertThat(response.items().get(0).region().name()).isEqualTo("강남구");
        assertThat(response.items().get(0).imageUrl()).isNull();
        assertThat(response.items().get(0).appointmentCount()).isEqualTo(2);
        assertThat(response.items().get(0).chatRoomId())
                .isEqualTo(Math.min(current.getId(), friend.getId()) + "_"
                        + Math.max(current.getId(), friend.getId()));
    }

    @Test
    void loadsMultipleFriendsWithThreeQueries() {
        User current = saveUser("current");
        User first = saveUser("first");
        User second = saveUser("second");
        saveProfile(first, "첫 번째");
        saveProfile(second, "두 번째");
        friendshipRepository.save(Friendship.create(current, first));
        friendshipRepository.save(Friendship.create(second, current));
        entityManager.flush();
        entityManager.clear();

        Statistics statistics = entityManager.getEntityManagerFactory()
                .unwrap(SessionFactory.class)
                .getStatistics();
        statistics.setStatisticsEnabled(true);
        statistics.clear();
        try {
            assertThat(friendService.getFriends(current.getId()).items()).hasSize(2);
            assertThat(statistics.getPrepareStatementCount()).isEqualTo(3);
        } finally {
            statistics.setStatisticsEnabled(false);
        }
    }

    @Test
    void returnsZeroWhenFriendHasNoAppointments() {
        User current = saveUser("current");
        User friend = saveUser("friend");
        saveProfile(friend, "친구");
        friendshipRepository.save(Friendship.create(current, friend));

        assertThat(friendService.getFriends(current.getId()).items().get(0).appointmentCount())
                .isZero();
    }

    @Test
    void failsWhenFriendProfileDoesNotExist() {
        User current = saveUser("current");
        User friend = saveUser("friend");
        friendshipRepository.save(Friendship.create(current, friend));

        assertThatThrownBy(() -> friendService.getFriends(current.getId()))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PROFILE_NOT_FOUND);
    }

    @Test
    void deletesFriendshipFromEitherNormalizedSide() {
        User first = saveUser("first");
        User current = saveUser("current");
        User last = saveUser("last");
        friendshipRepository.save(Friendship.create(first, current));
        friendshipRepository.save(Friendship.create(current, last));

        friendService.deleteFriend(current.getId(), first.getId());
        friendService.deleteFriend(current.getId(), last.getId());
        friendshipRepository.flush();

        assertThat(friendshipRepository.count()).isZero();
    }

    @Test
    void deleteReturnsNotFriendForSelfMissingUserAndUnrelatedRelationship() {
        User current = saveUser("current");
        User other = saveUser("other");
        User third = saveUser("third");
        friendshipRepository.save(Friendship.create(other, third));

        assertNotFriend(() -> friendService.deleteFriend(current.getId(), current.getId()));
        assertNotFriend(() -> friendService.deleteFriend(current.getId(), Long.MAX_VALUE));
        assertNotFriend(() -> friendService.deleteFriend(current.getId(), other.getId()));
        assertThat(friendshipRepository.existsByUserAIdAndUserBId(other.getId(), third.getId()))
                .isTrue();
    }

    @Test
    void deletingFriendshipKeepsRelatedDataAndAllowsNewRequest() {
        User current = saveUser("current");
        User friend = saveUser("friend");
        Friendship friendship = friendshipRepository.save(Friendship.create(current, friend));
        Appointment appointment = appointmentRepository.save(Appointment.create(
                current, friend, current, LocalDateTime.now().plusDays(1), "유지"));
        FriendRequest accepted = FriendRequest.create(current, friend);
        accepted.accept();
        accepted = friendRequestRepository.saveAndFlush(accepted);
        Long acceptedId = accepted.getId();

        friendService.deleteFriend(current.getId(), friend.getId());
        friendshipRepository.flush();
        FriendRequest pending = friendRequestRepository.saveAndFlush(FriendRequest.create(current, friend));

        assertThat(friendshipRepository.findById(friendship.getId())).isEmpty();
        assertThat(appointmentRepository.findById(appointment.getId())).isPresent();
        assertThat(friendRequestRepository.findById(acceptedId))
                .get().extracting(FriendRequest::getStatus)
                .isEqualTo(FriendRequestStatus.ACCEPTED);
        assertThat(pending.getStatus()).isEqualTo(FriendRequestStatus.PENDING);
    }

    @Test
    void deletedFriendsCanRequestAndBecomeFriendsAgain() {
        User current = saveUser("current");
        User friend = saveUser("friend");
        saveProfile(current, "현재");
        saveProfile(friend, "친구");
        friendshipRepository.saveAndFlush(Friendship.create(current, friend));

        friendService.deleteFriend(current.getId(), friend.getId());
        friendshipRepository.flush();
        FriendRequestCreateResponse request = friendRequestService.create(
                current.getId(), new FriendRequestCreateRequest(friend.getId()));
        friendRequestService.respond(
                friend.getId(),
                request.requestId(),
                new FriendRequestRespondRequest(FriendRequestStatus.ACCEPTED));
        friendshipRepository.flush();

        Friendship recreated = friendshipRepository.findByUserAIdAndUserBId(
                        Math.min(current.getId(), friend.getId()),
                        Math.max(current.getId(), friend.getId()))
                .orElseThrow();
        assertThat(recreated.getCreatedAt().toLocalDate()).isEqualTo(LocalDate.now());
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
                user, name, 25, Gender.FEMALE, sport,
                ExerciseLevel.INTERMEDIATE, region));
    }

    private void assertNotFriend(org.assertj.core.api.ThrowableAssert.ThrowingCallable action) {
        assertThatThrownBy(action)
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.NOT_FRIEND);
    }
}
