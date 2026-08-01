package com.gdghajithon.appointment.dto;

import com.gdghajithon.appointment.Appointment;

import java.time.LocalDateTime;

public record AppointmentListResponse(
        Long id,
        LocalDateTime dateTime,
        String place,
        Long createdBy
) {

    public static AppointmentListResponse from(Appointment appointment) {
        return new AppointmentListResponse(
                appointment.getId(),
                appointment.getDateTime(),
                appointment.getPlace(),
                appointment.getCreator().getId()
        );
    }
}
