package com.gdghajithon.friendrequest.dto;

import com.gdghajithon.friendrequest.FriendRequest;
import com.gdghajithon.friendrequest.FriendRequestStatus;

import java.time.LocalDateTime;

public record FriendRequestCreateResponse(
        Long requestId,
        Long senderUserId,
        Long receiverUserId,
        FriendRequestStatus status,
        LocalDateTime createdAt
) {
    public static FriendRequestCreateResponse from(FriendRequest request) {
        return new FriendRequestCreateResponse(
                request.getId(),
                request.getSender().getId(),
                request.getReceiver().getId(),
                request.getStatus(),
                request.getCreatedAt()
        );
    }
}
