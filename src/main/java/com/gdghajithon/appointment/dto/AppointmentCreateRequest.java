package com.gdghajithon.appointment.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record AppointmentCreateRequest(
        @NotNull @Future LocalDateTime dateTime,
        @Size(min = 1, max = 100) String place,
        @NotNull Long coachId
) {
}
