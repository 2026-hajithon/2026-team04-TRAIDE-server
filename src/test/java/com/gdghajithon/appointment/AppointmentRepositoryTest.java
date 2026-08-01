package com.gdghajithon.appointment;

import com.gdghajithon.user.User;
import com.gdghajithon.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class AppointmentRepositoryTest {

    @Autowired private AppointmentRepository appointmentRepository;
    @Autowired private UserRepository userRepository;

    @Test
    void countsEveryAppointmentWhereUserParticipatesAsCreatorOrFriend() {
        User target = saveUser("target");
        User first = saveUser("first");
        User second = saveUser("second");
        User unrelated = saveUser("unrelated");

        appointmentRepository.save(Appointment.create(
                target, first, target, LocalDateTime.now().minusDays(1), "과거 약속"));
        appointmentRepository.save(Appointment.create(
                second, target, target, LocalDateTime.now().plusDays(1), "미래 약속"));
        appointmentRepository.save(Appointment.create(
                first, unrelated, first, LocalDateTime.now(), "무관한 약속"));
        appointmentRepository.flush();

        assertThat(appointmentRepository.countByUserId(target.getId())).isEqualTo(2);
    }

    @Test
    void countsAppointmentsForAllFriendsInOneAggregation() {
        User current = saveUser("current");
        User firstFriend = saveUser("firstFriend");
        User secondFriend = saveUser("secondFriend");
        User unrelated = saveUser("unrelated");

        appointmentRepository.save(Appointment.create(
                current, firstFriend, current, LocalDateTime.now().minusDays(1), "past"));
        appointmentRepository.save(Appointment.create(
                firstFriend, current, firstFriend, LocalDateTime.now().plusDays(1), "future"));
        appointmentRepository.save(Appointment.create(
                secondFriend, current, current, LocalDateTime.now().plusDays(2), "second"));
        Appointment deleted = appointmentRepository.saveAndFlush(Appointment.create(
                current, secondFriend, current, LocalDateTime.now().plusDays(3), "deleted"));
        appointmentRepository.delete(deleted);
        appointmentRepository.save(Appointment.create(
                current, unrelated, current, LocalDateTime.now(), "excluded"));
        appointmentRepository.flush();

        Map<Long, Long> counts = appointmentRepository.countByFriendUserIds(
                        current.getId(), List.of(firstFriend.getId(), secondFriend.getId()))
                .stream()
                .collect(Collectors.toMap(
                        FriendAppointmentCount::getFriendUserId,
                        FriendAppointmentCount::getAppointmentCount,
                        Long::sum));

        assertThat(counts)
                .containsEntry(firstFriend.getId(), 2L)
                .containsEntry(secondFriend.getId(), 1L)
                .doesNotContainKey(unrelated.getId());
    }

    private User saveUser(String loginId) {
        return userRepository.saveAndFlush(User.create(loginId, "encoded-password"));
    }
}
