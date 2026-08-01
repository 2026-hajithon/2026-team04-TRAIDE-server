package com.gdghajithon.friendrequest.dto;

import com.gdghajithon.profile.ExerciseLevel;
import com.gdghajithon.profile.Gender;
import com.gdghajithon.profile.Profile;
import com.gdghajithon.profile.dto.RegionSummaryResponse;
import com.gdghajithon.profile.dto.SportSummaryResponse;

public record FriendRequestUserResponse(
        Long id,
        String name,
        Integer age,
        Gender gender,
        SportSummaryResponse sport,
        ExerciseLevel level,
        RegionSummaryResponse region
) {
    public static FriendRequestUserResponse from(Profile profile) {
        return new FriendRequestUserResponse(
                profile.getUser().getId(),
                profile.getName(),
                profile.getAge(),
                profile.getGender(),
                SportSummaryResponse.from(profile.getSport()),
                profile.getExerciseLevel(),
                RegionSummaryResponse.from(profile.getRegion())
        );
    }
}
