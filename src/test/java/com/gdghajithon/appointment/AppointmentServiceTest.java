package com.gdghajithon.appointment;

import com.gdghajithon.appointment.dto.AppointmentCreateRequest;
import com.gdghajithon.appointment.dto.AppointmentUpdateRequest;
import com.gdghajithon.friend.FriendService;
import com.gdghajithon.friend.Friendship;
import com.gdghajithon.friend.FriendshipRepository;
import com.gdghajithon.global.exception.BusinessException;
import com.gdghajithon.global.exception.ErrorCode;
import com.gdghajithon.user.User;
import com.gdghajithon.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
@Import({AppointmentService.class, FriendService.class})
class AppointmentServiceTest {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private FriendshipRepository friendshipRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void friendCanCreateAppointmentAndBothUsersCanViewIt() {
        User creator = saveUser("creator");
        User friend = saveUser("friend");
        saveFriendship(creator, friend);

        appointmentService.create(
                creator.getId(),
                friend.getId(),
                new AppointmentCreateRequest(LocalDateTime.of(2026, 8, 2, 14, 0), "목동운동장")
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
                new AppointmentCreateRequest(LocalDateTime.of(2026, 8, 2, 14, 0), "목동운동장")
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
                new AppointmentCreateRequest(LocalDateTime.of(2026, 8, 2, 14, 0), "목동운동장")
        ).id();

        appointmentService.update(
                friend.getId(),
                appointmentId,
                new AppointmentUpdateRequest(LocalDateTime.of(2026, 8, 2, 15, 0), "한강공원")
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
                new AppointmentCreateRequest(LocalDateTime.of(2026, 8, 2, 14, 0), "목동운동장")
        ).id();

        LocalDateTime newDateTime = LocalDateTime.of(2026, 8, 2, 15, 0);
        var response = appointmentService.update(
                creator.getId(),
                appointmentId,
                new AppointmentUpdateRequest(newDateTime, null)
        );

        assertThat(response.dateTime()).isEqualTo(newDateTime);
        assertThat(response.place()).isEqualTo("목동운동장");
    }

    @Test
    void canUpdateOnlyPlace() {
        User creator = saveUser("creator");
        User friend = saveUser("friend");
        saveFriendship(creator, friend);
        LocalDateTime dateTime = LocalDateTime.of(2026, 8, 2, 14, 0);
        Long appointmentId = appointmentService.create(
                creator.getId(),
                friend.getId(),
                new AppointmentCreateRequest(dateTime, "목동운동장")
        ).id();

        var response = appointmentService.update(
                creator.getId(),
                appointmentId,
                new AppointmentUpdateRequest(null, "한강공원")
        );

        assertThat(response.dateTime()).isEqualTo(dateTime);
        assertThat(response.place()).isEqualTo("한강공원");
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
                new AppointmentCreateRequest(LocalDateTime.of(2026, 8, 2, 14, 0), "목동운동장")
        ).id();

        assertThatThrownBy(() -> appointmentService.update(
                other.getId(),
                appointmentId,
                new AppointmentUpdateRequest(LocalDateTime.of(2026, 8, 2, 15, 0), "한강공원")
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.FORBIDDEN);
    }

    private User saveUser(String loginId) {
        return userRepository.save(User.create(loginId, "encoded-password"));
    }

    private void saveFriendship(User first, User second) {
        friendshipRepository.save(Friendship.create(first, second));
    }
}
