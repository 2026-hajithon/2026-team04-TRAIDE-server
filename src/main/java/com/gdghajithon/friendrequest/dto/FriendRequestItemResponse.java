package com.gdghajithon.friendrequest.dto;

import com.gdghajithon.friendrequest.FriendRequest;
import com.gdghajithon.profile.Profile;

import java.time.LocalDateTime;

public record FriendRequestItemResponse(
        Long requestId,
        LocalDateTime createdAt,
        FriendRequestUserResponse user
) {
    public static FriendRequestItemResponse from(FriendRequest request, Profile profile) {
        return new FriendRequestItemResponse(
                request.getId(),
                request.getCreatedAt(),
                FriendRequestUserResponse.from(profile)
        );
    }
}
