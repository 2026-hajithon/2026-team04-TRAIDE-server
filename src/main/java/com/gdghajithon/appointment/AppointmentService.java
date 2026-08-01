package com.gdghajithon.appointment;

import com.gdghajithon.appointment.dto.AppointmentCreateRequest;
import com.gdghajithon.appointment.dto.AppointmentCreateResponse;
import com.gdghajithon.appointment.dto.AppointmentListResponse;
import com.gdghajithon.appointment.dto.AppointmentUpdateRequest;
import com.gdghajithon.appointment.dto.AppointmentUpdateResponse;
import com.gdghajithon.friend.FriendService;
import com.gdghajithon.global.exception.BusinessException;
import com.gdghajithon.global.exception.ErrorCode;
import com.gdghajithon.user.User;
import com.gdghajithon.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final FriendService friendService;

    @Transactional
    public AppointmentCreateResponse create(
            Long userId,
            Long friendId,
            AppointmentCreateRequest request
    ) {
        friendService.validateFriend(userId, friendId);

        User creator = getUser(userId);
        User friend = getUser(friendId);
        Appointment appointment = Appointment.create(
                creator,
                friend,
                request.dateTime(),
                request.place()
        );

        return AppointmentCreateResponse.from(appointmentRepository.save(appointment));
    }

    @Transactional(readOnly = true)
    public List<AppointmentListResponse> getAppointments(Long userId, Long friendId) {
        friendService.validateFriend(userId, friendId);
        return appointmentRepository.findAllBetweenUsers(userId, friendId)
                .stream()
                .map(AppointmentListResponse::from)
                .toList();
    }

    @Transactional
    public AppointmentUpdateResponse update(
            Long userId,
            Long appointmentId,
            AppointmentUpdateRequest request
    ) {
        Appointment appointment = getAppointment(appointmentId);
        validateParticipant(appointment, userId);
        appointment.update(request.dateTime(), request.place());
        return AppointmentUpdateResponse.from(appointment);
    }

    @Transactional
    public void delete(Long userId, Long appointmentId) {
        Appointment appointment = getAppointment(appointmentId);
        validateParticipant(appointment, userId);
        appointmentRepository.delete(appointment);
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private Appointment getAppointment(Long appointmentId) {
        return appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.APPOINTMENT_NOT_FOUND));
    }

    private void validateParticipant(Appointment appointment, Long userId) {
        if (!appointment.hasParticipant(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }
}
