package com.gdghajithon.profile.dto;

import com.gdghajithon.friend.Friendship;
import com.gdghajithon.profile.ExerciseLevel;
import com.gdghajithon.profile.FriendStatus;
import com.gdghajithon.profile.Gender;
import com.gdghajithon.profile.Profile;

import java.time.LocalDateTime;

public record UserDetailResponse(
        Long userId,
        String name,
        Integer age,
        Gender gender,
        Long sportId,
        String sportName,
        ExerciseLevel exerciseLevel,
        Long regionId,
        String regionName,
        long friendCount,
        FriendStatus friendStatus,
        Long friendshipId,
        LocalDateTime friendSince
) {
    public static UserDetailResponse from(
            Profile profile,
            long friendCount,
            FriendStatus friendStatus,
            Friendship friendship
    ) {
        return new UserDetailResponse(
                profile.getUser().getId(),
                profile.getName(),
                profile.getAge(),
                profile.getGender(),
                profile.getSport().getId(),
                profile.getSport().getName(),
                profile.getExerciseLevel(),
                profile.getRegion().getId(),
                profile.getRegion().getName(),
                friendCount,
                friendStatus,
                friendship == null ? null : friendship.getId(),
                friendship == null ? null : friendship.getCreatedAt()
        );
    }
}
