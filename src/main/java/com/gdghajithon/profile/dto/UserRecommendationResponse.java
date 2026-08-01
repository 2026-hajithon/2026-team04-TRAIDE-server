package com.gdghajithon.profile.dto;

import com.gdghajithon.profile.ExerciseLevel;
import com.gdghajithon.profile.Gender;
import com.gdghajithon.profile.Profile;
import io.swagger.v3.oas.annotations.media.Schema;

public record UserRecommendationResponse(
        Long id,
        String name,
        @Schema(description = "선택한 운동 종목의 기본 프로필 이미지 URL")
        String imageUrl,
        Integer age,
        Gender gender,
        SportSummaryResponse sport,
        ExerciseLevel level,
        RegionSummaryResponse region,
        @Schema(description = "평균 평점. Review 연동 전에는 null", nullable = true, example = "4.8")
        Double averageRating,
        @Schema(description = "후기 수. Review 연동 전에는 0", example = "0")
        long reviewCount
) {
    public static UserRecommendationResponse from(
            Profile profile,
            String imageUrl,
            Double averageRating,
            long reviewCount
    ) {
        return new UserRecommendationResponse(
                profile.getUser().getId(),
                profile.getName(),
                imageUrl,
                profile.getAge(),
                profile.getGender(),
                SportSummaryResponse.from(profile.getSport()),
                profile.getExerciseLevel(),
                RegionSummaryResponse.from(profile.getRegion()),
                averageRating,
                reviewCount
        );
    }
}
