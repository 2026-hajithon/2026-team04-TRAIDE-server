package com.gdghajithon.appointment;

import com.gdghajithon.user.User;
import com.gdghajithon.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

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
                target, first, LocalDateTime.now().minusDays(1), "과거 약속"));
        appointmentRepository.save(Appointment.create(
                second, target, LocalDateTime.now().plusDays(1), "미래 약속"));
        appointmentRepository.save(Appointment.create(
                first, unrelated, LocalDateTime.now(), "무관한 약속"));
        appointmentRepository.flush();

        assertThat(appointmentRepository.countByUserId(target.getId())).isEqualTo(2);
    }

    private User saveUser(String loginId) {
        return userRepository.saveAndFlush(User.create(loginId, "encoded-password"));
    }
}
