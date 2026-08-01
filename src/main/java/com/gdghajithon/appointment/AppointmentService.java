package com.gdghajithon.appointment;

import com.gdghajithon.appointment.dto.AppointmentCreateRequest;
import com.gdghajithon.appointment.dto.AppointmentCreateResponse;
import com.gdghajithon.appointment.dto.AppointmentListResponse;
import com.gdghajithon.appointment.dto.AppointmentUpdateRequest;
import com.gdghajithon.appointment.dto.AppointmentUpdateResponse;
import com.gdghajithon.appointment.dto.UpcomingAppointmentResponse;
import com.gdghajithon.friend.FriendService;
import com.gdghajithon.global.exception.BusinessException;
import com.gdghajithon.global.exception.ErrorCode;
import com.gdghajithon.user.User;
import com.gdghajithon.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
        User coach = getCoach(request.coachId(), creator, friend);
        validateDateTime(request.dateTime());
        validatePlace(request.place());
        Appointment appointment = Appointment.create(
                creator,
                friend,
                coach,
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

    @Transactional(readOnly = true)
    public List<UpcomingAppointmentResponse> getUpcomingAppointments(Long userId) {
        getUser(userId);
        return appointmentRepository.findUpcomingByUserId(userId, LocalDateTime.now())
                .stream()
                .map(appointment -> UpcomingAppointmentResponse.from(appointment, userId))
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
        validateDateTime(request.dateTime());
        if (request.placeIncluded()) {
            validatePlace(request.place());
        }
        User coach = request.coachId() == null
                ? null
                : getCoach(request.coachId(), appointment.getCreator(), appointment.getFriend());
        appointment.update(
                request.dateTime(),
                request.place(),
                request.placeIncluded(),
                coach
        );
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

    private User getCoach(Long coachId, User creator, User friend) {
        if (creator.getId().equals(coachId)) {
            return creator;
        }
        if (friend.getId().equals(coachId)) {
            return friend;
        }
        throw new BusinessException(ErrorCode.VALIDATION_ERROR);
    }

    private void validateDateTime(LocalDateTime dateTime) {
        if (dateTime != null && !dateTime.isAfter(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
    }

    private void validatePlace(String place) {
        if (place != null && (place.isBlank() || place.length() > 100)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
    }
}
