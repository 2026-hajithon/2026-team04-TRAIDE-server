package com.gdghajithon.profile.dto;

import com.gdghajithon.profile.ExerciseLevel;
import com.gdghajithon.profile.FriendStatus;
import com.gdghajithon.profile.Gender;
import com.gdghajithon.profile.Profile;
import io.swagger.v3.oas.annotations.media.Schema;

public record UserDetailResponse(
        Long id,
        String name,
        Integer age,
        Gender gender,
        SportSummaryResponse sport,
        ExerciseLevel level,
        RegionSummaryResponse region,
        long friendCount,
        @Schema(description = "대상 사용자가 참여한 전체 약속 수", example = "8")
        long appointmentCount,
        @Schema(description = "평균 평점. Review 연동 전에는 null", nullable = true, example = "4.8")
        Double averageRating,
        @Schema(description = "후기 수. Review 연동 전에는 0", example = "0")
        long reviewCount,
        @Schema(description = "현재 로그인 사용자와 대상 사용자의 친구 상태", example = "FRIEND")
        FriendStatus friendStatus,
        @Schema(description = "친구가 된 당일을 1로 계산한 일수. 친구가 아니면 null", nullable = true, example = "16")
        Long friendSinceDays
) {
    public static UserDetailResponse from(
            Profile profile,
            long friendCount,
            long appointmentCount,
            Double averageRating,
            long reviewCount,
            FriendStatus friendStatus,
            Long friendSinceDays
    ) {
        return new UserDetailResponse(
                profile.getUser().getId(),
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
                friendStatus,
                friendSinceDays
        );
    }
}
