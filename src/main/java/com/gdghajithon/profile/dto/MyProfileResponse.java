package com.gdghajithon.profile.dto;

import com.gdghajithon.profile.ExerciseLevel;
import com.gdghajithon.profile.Gender;
import com.gdghajithon.profile.Profile;

import java.time.LocalDateTime;

public record MyProfileResponse(
        Long userId,
        String loginId,
        String name,
        Integer age,
        Gender gender,
        Long sportId,
        String sportName,
        ExerciseLevel exerciseLevel,
        Long regionId,
        String regionName,
        long friendCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static MyProfileResponse from(Profile profile, long friendCount) {
        return new MyProfileResponse(
                profile.getUser().getId(),
                profile.getUser().getLoginId(),
                profile.getName(),
                profile.getAge(),
                profile.getGender(),
                profile.getSport().getId(),
                profile.getSport().getName(),
                profile.getExerciseLevel(),
                profile.getRegion().getId(),
                profile.getRegion().getName(),
                friendCount,
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }
}
