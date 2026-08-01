package com.gdghajithon.profile.dto;

import com.gdghajithon.profile.ExerciseLevel;
import com.gdghajithon.profile.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ProfileUpdateRequest(
        @NotBlank @Size(max = 30)
        @Schema(example = "김민수")
        String name,

        @NotNull @Min(14) @Max(100)
        @Schema(example = "26")
        Integer age,

        @NotNull
        @Schema(example = "MALE")
        Gender gender,

        @NotNull @Positive
        @Schema(example = "3")
        Long sportId,

        @NotNull
        @Schema(example = "ADVANCED")
        ExerciseLevel exerciseLevel,

        @NotNull @Positive
        @Schema(example = "2")
        Long regionId
) {
}
