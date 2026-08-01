package com.gdghajithon.appointment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record AppointmentCreateRequest(
        @NotNull LocalDateTime dateTime,
        @NotBlank @Size(max = 100) String place
) {
}
