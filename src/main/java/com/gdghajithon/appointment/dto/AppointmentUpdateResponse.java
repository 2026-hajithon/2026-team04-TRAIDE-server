package com.gdghajithon.appointment.dto;

import com.gdghajithon.appointment.Appointment;

import java.time.LocalDateTime;

public record AppointmentUpdateResponse(
        Long id,
        LocalDateTime dateTime,
        String place
) {

    public static AppointmentUpdateResponse from(Appointment appointment) {
        return new AppointmentUpdateResponse(
                appointment.getId(),
                appointment.getDateTime(),
                appointment.getPlace()
        );
    }
}
