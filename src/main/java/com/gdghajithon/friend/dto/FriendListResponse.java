package com.gdghajithon.friend.dto;

import java.util.List;

public record FriendListResponse(
        List<FriendItemResponse> items
) {
    public FriendListResponse {
        items = List.copyOf(items);
    }

    public static FriendListResponse of(List<FriendItemResponse> items) {
        return new FriendListResponse(items);
    }
}
