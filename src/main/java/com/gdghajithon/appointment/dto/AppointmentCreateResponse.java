package com.gdghajithon.appointment.dto;

import com.gdghajithon.appointment.Appointment;

import java.time.LocalDateTime;

public record AppointmentCreateResponse(
        Long id,
        Long friendId,
        LocalDateTime dateTime,
        String place,
        Long coachId
) {

    public static AppointmentCreateResponse from(Appointment appointment) {
        return new AppointmentCreateResponse(
                appointment.getId(),
                appointment.getFriend().getId(),
                appointment.getDateTime(),
                appointment.getPlace(),
                appointment.getCoach().getId()
        );
    }
}
