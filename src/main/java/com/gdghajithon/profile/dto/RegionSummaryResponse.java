package com.gdghajithon.profile.dto;

import com.gdghajithon.region.Region;
import io.swagger.v3.oas.annotations.media.Schema;

public record RegionSummaryResponse(
        @Schema(example = "1") Long id,
        @Schema(example = "강남구") String name
) {
    public static RegionSummaryResponse from(Region region) {
        return new RegionSummaryResponse(region.getId(), region.getName());
    }
}
