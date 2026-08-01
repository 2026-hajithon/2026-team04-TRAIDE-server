package com.gdghajithon.profile.dto;

import com.gdghajithon.sport.Sport;
import io.swagger.v3.oas.annotations.media.Schema;

public record SportSummaryResponse(
        @Schema(example = "6") Long id,
        @Schema(example = "테니스") String name
) {
    public static SportSummaryResponse from(Sport sport) {
        return new SportSummaryResponse(sport.getId(), sport.getName());
    }
}
