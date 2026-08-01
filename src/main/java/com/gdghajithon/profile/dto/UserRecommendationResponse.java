package com.gdghajithon.profile.dto;

import com.gdghajithon.profile.ExerciseLevel;
import com.gdghajithon.profile.Gender;
import com.gdghajithon.profile.Profile;

public record UserRecommendationResponse(
        Long userId,
        String name,
        Integer age,
        Gender gender,
        Long sportId,
        String sportName,
        ExerciseLevel exerciseLevel,
        Long regionId,
        String regionName
) {
    public static UserRecommendationResponse from(Profile profile) {
        return new UserRecommendationResponse(
                profile.getUser().getId(),
                profile.getName(),
                profile.getAge(),
                profile.getGender(),
                profile.getSport().getId(),
                profile.getSport().getName(),
                profile.getExerciseLevel(),
                profile.getRegion().getId(),
                profile.getRegion().getName()
        );
    }
}
