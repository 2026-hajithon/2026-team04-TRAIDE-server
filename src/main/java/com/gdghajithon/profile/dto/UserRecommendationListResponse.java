package com.gdghajithon.profile.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record UserRecommendationListResponse(
        @Schema(description = "추천 사용자 목록")
        List<UserRecommendationResponse> items
) {
    public UserRecommendationListResponse {
        items = List.copyOf(items);
    }

    public static UserRecommendationListResponse of(List<UserRecommendationResponse> items) {
        return new UserRecommendationListResponse(items);
    }
}
