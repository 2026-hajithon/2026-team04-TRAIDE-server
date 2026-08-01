package com.gdghajithon.appointment;

import com.gdghajithon.appointment.dto.AppointmentCreateRequest;
import com.gdghajithon.appointment.dto.AppointmentUpdateRequest;
import com.gdghajithon.friend.FriendService;
import com.gdghajithon.friend.Friendship;
import com.gdghajithon.friend.FriendshipRepository;
import com.gdghajithon.global.exception.BusinessException;
import com.gdghajithon.global.exception.ErrorCode;
import com.gdghajithon.image.ImageUrlResolver;
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
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
@Import({
        AppointmentService.class,
        AppointmentQueryService.class,
        FriendService.class,
        ImageUrlResolver.class
})
class AppointmentServiceTest {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private AppointmentQueryService appointmentQueryService;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private FriendshipRepository friendshipRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private SportRepository sportRepository;

    @Autowired
    private RegionRepository regionRepository;

    @Test
    void friendCanCreateAppointmentAndBothUsersCanViewIt() {
        User creator = saveUser("creator");
        User friend = saveUser("friend");
        saveFriendship(creator, friend);

        appointmentService.create(
                creator.getId(),
                friend.getId(),
                new AppointmentCreateRequest(futureDateTime(), "목동운동장", creator.getId())
        );

        assertThat(appointmentService.getAppointments(creator.getId(), friend.getId()))
                .singleElement()
                .satisfies(response -> assertThat(response.createdBy()).isEqualTo(creator.getId()));
        assertThat(appointmentService.getAppointments(friend.getId(), creator.getId()))
                .singleElement()
                .satisfies(response -> assertThat(response.createdBy()).isEqualTo(creator.getId()));
    }

    @Test
    void userWhoIsNotFriendCannotCreateAppointment() {
        User creator = saveUser("creator");
        User other = saveUser("other");

        assertThatThrownBy(() -> appointmentService.create(
                creator.getId(),
                other.getId(),
                new AppointmentCreateRequest(futureDateTime(), "목동운동장", creator.getId())
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.NOT_FRIEND);
    }

    @Test
    void bothParticipantsCanUpdateAndDeleteAppointment() {
        User creator = saveUser("creator");
        User friend = saveUser("friend");
        saveFriendship(creator, friend);
        Long appointmentId = appointmentService.create(
                creator.getId(),
                friend.getId(),
                new AppointmentCreateRequest(futureDateTime(), "목동운동장", creator.getId())
        ).id();

        appointmentService.update(
                friend.getId(),
                appointmentId,
                new AppointmentUpdateRequest(futureDateTime().plusHours(1), "한강공원", friend.getId())
        );

        appointmentService.delete(friend.getId(), appointmentId);
        assertThat(appointmentRepository.findById(appointmentId)).isEmpty();
    }

    @Test
    void canUpdateOnlyDateTime() {
        User creator = saveUser("creator");
        User friend = saveUser("friend");
        saveFriendship(creator, friend);
        Long appointmentId = appointmentService.create(
                creator.getId(),
                friend.getId(),
                new AppointmentCreateRequest(futureDateTime(), "목동운동장", creator.getId())
        ).id();

        LocalDateTime newDateTime = futureDateTime().plusHours(1);
        var response = appointmentService.update(
                creator.getId(),
                appointmentId,
                new AppointmentUpdateRequest(newDateTime, null, null)
        );

        assertThat(response.dateTime()).isEqualTo(newDateTime);
        assertThat(response.place()).isEqualTo("목동운동장");
    }

    @Test
    void canUpdateOnlyPlace() {
        User creator = saveUser("creator");
        User friend = saveUser("friend");
        saveFriendship(creator, friend);
        LocalDateTime dateTime = futureDateTime();
        Long appointmentId = appointmentService.create(
                creator.getId(),
                friend.getId(),
                new AppointmentCreateRequest(dateTime, "목동운동장", creator.getId())
        ).id();

        var response = appointmentService.update(
                creator.getId(),
                appointmentId,
                new AppointmentUpdateRequest(null, "한강공원", null)
        );

        assertThat(response.dateTime()).isEqualTo(dateTime);
        assertThat(response.place()).isEqualTo("한강공원");
    }

    @Test
    void placeCanBeClearedWithExplicitNull() {
        User creator = saveUser("creator");
        User friend = saveUser("friend");
        saveFriendship(creator, friend);
        Long appointmentId = appointmentService.create(
                creator.getId(),
                friend.getId(),
                new AppointmentCreateRequest(futureDateTime(), "목동운동장", creator.getId())
        ).id();

        var response = appointmentService.update(
                creator.getId(),
                appointmentId,
                new AppointmentUpdateRequest(null, null, null, true)
        );

        assertThat(response.place()).isNull();
    }

    @Test
    void userWhoIsNotParticipantCannotUpdateAppointment() {
        User creator = saveUser("creator");
        User friend = saveUser("friend");
        User other = saveUser("other");
        saveFriendship(creator, friend);
        Long appointmentId = appointmentService.create(
                creator.getId(),
                friend.getId(),
                new AppointmentCreateRequest(futureDateTime(), "목동운동장", creator.getId())
        ).id();

        assertThatThrownBy(() -> appointmentService.update(
                other.getId(),
                appointmentId,
                new AppointmentUpdateRequest(futureDateTime().plusHours(1), "한강공원", null)
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.FORBIDDEN);
    }

    @Test
    void appointmentCanBeCreatedWithoutPlace() {
        User creator = saveUser("creator");
        User friend = saveUser("friend");
        saveFriendship(creator, friend);

        var response = appointmentService.create(
                creator.getId(),
                friend.getId(),
                new AppointmentCreateRequest(futureDateTime(), null, friend.getId())
        );

        assertThat(response.place()).isNull();
        assertThat(response.coachId()).isEqualTo(friend.getId());
    }

    @Test
    void coachCanBeChangedToOtherParticipant() {
        User creator = saveUser("creator");
        User friend = saveUser("friend");
        saveFriendship(creator, friend);
        Long appointmentId = appointmentService.create(
                creator.getId(),
                friend.getId(),
                new AppointmentCreateRequest(futureDateTime(), null, creator.getId())
        ).id();

        var response = appointmentService.update(
                friend.getId(),
                appointmentId,
                new AppointmentUpdateRequest(null, null, friend.getId())
        );

        assertThat(response.coachId()).isEqualTo(friend.getId());
    }

    @Test
    void userWhoIsNotParticipantCannotBeCoach() {
        User creator = saveUser("creator");
        User friend = saveUser("friend");
        User other = saveUser("other");
        saveFriendship(creator, friend);

        assertThatThrownBy(() -> appointmentService.create(
                creator.getId(),
                friend.getId(),
                new AppointmentCreateRequest(futureDateTime(), null, other.getId())
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.VALIDATION_ERROR);
    }

    @Test
    void pastAppointmentCannotBeCreated() {
        User creator = saveUser("creator");
        User friend = saveUser("friend");
        saveFriendship(creator, friend);

        assertThatThrownBy(() -> appointmentService.create(
                creator.getId(),
                friend.getId(),
                new AppointmentCreateRequest(LocalDateTime.now().minusMinutes(1), null, creator.getId())
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.VALIDATION_ERROR);
    }

    @Test
    void upcomingAppointmentsReturnOnlyFutureAppointmentsInDateOrder() {
        User creator = saveUser("creator");
        User friend = saveUser("friend");
        saveProfile(friend, "운동 친구");
        saveFriendship(creator, friend);
        appointmentRepository.save(Appointment.create(
                creator,
                friend,
                creator,
                LocalDateTime.now().minusDays(1),
                null
        ));
        Long laterId = appointmentService.create(
                creator.getId(),
                friend.getId(),
                new AppointmentCreateRequest(futureDateTime().plusHours(1), null, creator.getId())
        ).id();
        Long earlierId = appointmentService.create(
                creator.getId(),
                friend.getId(),
                new AppointmentCreateRequest(futureDateTime(), null, friend.getId())
        ).id();

        var responses = appointmentService.getUpcomingAppointments(creator.getId());
        assertThat(responses)
                .extracting("id")
                .containsExactly(earlierId, laterId);
        assertThat(responses.get(0).friend().name()).isEqualTo("운동 친구");
        assertThat(responses.get(0).friend().imageUrl())
                .isEqualTo("/images/sports/tennis.png");
    }

    @Test
    void appointmentCountsCanBeQueried() {
        User creator = saveUser("creator");
        User friend = saveUser("friend");
        saveFriendship(creator, friend);
        appointmentService.create(
                creator.getId(),
                friend.getId(),
                new AppointmentCreateRequest(futureDateTime(), null, creator.getId())
        );

        assertThat(appointmentQueryService.countByUserId(creator.getId())).isEqualTo(1);
        assertThat(appointmentQueryService.countBetweenUsers(creator.getId(), friend.getId())).isEqualTo(1);
        assertThat(appointmentQueryService.existsBetweenUsers(creator.getId(), friend.getId())).isTrue();
    }

    private User saveUser(String loginId) {
        return userRepository.save(User.create(loginId, "encoded-password"));
    }

    private void saveFriendship(User first, User second) {
        friendshipRepository.save(Friendship.create(first, second));
    }

    private void saveProfile(User user, String name) {
        Sport sport = BeanUtils.instantiateClass(Sport.class);
        ReflectionTestUtils.setField(sport, "name", "테니스");
        ReflectionTestUtils.setField(sport, "imageUrl", "/images/sports/tennis.png");
        sport = sportRepository.save(sport);

        Region region = BeanUtils.instantiateClass(Region.class);
        ReflectionTestUtils.setField(region, "name", "강남구");
        region = regionRepository.save(region);

        profileRepository.save(Profile.create(
                user,
                name,
                25,
                Gender.FEMALE,
                sport,
                ExerciseLevel.INTERMEDIATE,
                region
        ));
    }

    private LocalDateTime futureDateTime() {
        return LocalDateTime.now().plusDays(1).withNano(0);
    }
}
