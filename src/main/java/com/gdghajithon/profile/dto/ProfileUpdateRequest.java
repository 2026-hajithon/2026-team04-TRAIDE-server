package com.gdghajithon.profile.dto;

import com.gdghajithon.profile.ExerciseLevel;
import com.gdghajithon.profile.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ProfileUpdateRequest(
        @Size(min = 1, max = 30)
        @Pattern(regexp = ".*\\S.*")
        @Schema(example = "김민수")
        String name,

        @Min(14) @Max(100)
        @Schema(example = "26")
        Integer age,

        @Schema(example = "MALE")
        Gender gender,

        @Positive
        @Schema(example = "3")
        Long sportId,

        @Schema(example = "ADVANCED")
        ExerciseLevel level,

        @Positive
        @Schema(example = "2")
        Long regionId
) {
}
