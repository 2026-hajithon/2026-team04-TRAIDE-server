package com.gdghajithon.friendrequest.dto;

import com.gdghajithon.friendrequest.FriendRequest;
import com.gdghajithon.friendrequest.FriendRequestStatus;

public record FriendRequestStatusResponse(
        Long requestId,
        FriendRequestStatus status
) {
    public static FriendRequestStatusResponse from(FriendRequest request) {
        return new FriendRequestStatusResponse(request.getId(), request.getStatus());
    }
}
