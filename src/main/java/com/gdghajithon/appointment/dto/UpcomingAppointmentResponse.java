package com.gdghajithon.appointment.dto;

import com.gdghajithon.appointment.Appointment;
import com.gdghajithon.user.User;

import java.time.LocalDateTime;

public record UpcomingAppointmentResponse(
        Long id,
        LocalDateTime dateTime,
        String place,
        Long coachId,
        FriendResponse friend
) {

    public static UpcomingAppointmentResponse from(Appointment appointment, Long userId) {
        User friend = appointment.getOtherParticipant(userId);
        return new UpcomingAppointmentResponse(
                appointment.getId(),
                appointment.getDateTime(),
                appointment.getPlace(),
                appointment.getCoach().getId(),
                new FriendResponse(friend.getId(), null, null)
        );
    }

    public record FriendResponse(
            Long id,
            String name,
            String imageUrl
    ) {
    }
}
