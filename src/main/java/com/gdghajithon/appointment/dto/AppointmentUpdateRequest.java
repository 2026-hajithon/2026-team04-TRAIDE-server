package com.gdghajithon.appointment.dto;

import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record AppointmentUpdateRequest(
        LocalDateTime dateTime,
        @Size(min = 1, max = 100) String place
) {
}
