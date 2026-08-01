package com.gdghajithon.profile.dto;

import com.gdghajithon.profile.ExerciseLevel;
import com.gdghajithon.profile.Gender;
import com.gdghajithon.profile.Profile;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record MyProfileResponse(
        Long id,
        String loginId,
        String name,
        Integer age,
        Gender gender,
        SportSummaryResponse sport,
        ExerciseLevel level,
        RegionSummaryResponse region,
        long friendCount,
        @Schema(description = "사용자가 참여한 전체 약속 수", example = "8")
        long appointmentCount,
        @Schema(description = "평균 평점. Review 연동 전에는 null", nullable = true, example = "4.8")
        Double averageRating,
        @Schema(description = "후기 수. Review 연동 전에는 0", example = "0")
        long reviewCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static MyProfileResponse from(
            Profile profile,
            long friendCount,
            long appointmentCount,
            Double averageRating,
            long reviewCount
    ) {
        return new MyProfileResponse(
                profile.getUser().getId(),
                profile.getUser().getLoginId(),
                profile.getName(),
                profile.getAge(),
                profile.getGender(),
                SportSummaryResponse.from(profile.getSport()),
                profile.getExerciseLevel(),
                RegionSummaryResponse.from(profile.getRegion()),
                friendCount,
                appointmentCount,
                averageRating,
                reviewCount,
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }
}
