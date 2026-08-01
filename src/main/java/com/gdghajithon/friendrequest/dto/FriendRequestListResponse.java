package com.gdghajithon.friendrequest.dto;

import java.util.List;

public record FriendRequestListResponse(
        List<FriendRequestItemResponse> items
) {
    public FriendRequestListResponse {
        items = List.copyOf(items);
    }

    public static FriendRequestListResponse of(List<FriendRequestItemResponse> items) {
        return new FriendRequestListResponse(items);
    }
}
