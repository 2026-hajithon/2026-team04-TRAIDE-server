package com.gdghajithon.friend.dto;

import com.gdghajithon.profile.Profile;
import com.gdghajithon.profile.dto.RegionSummaryResponse;
import com.gdghajithon.profile.dto.SportSummaryResponse;

public record FriendItemResponse(
        Long id,
        String name,
        SportSummaryResponse sport,
        RegionSummaryResponse region,
        String imageUrl,
        long appointmentCount,
        String chatRoomId
) {
    public static FriendItemResponse from(
            Profile profile,
            String imageUrl,
            long appointmentCount,
            String chatRoomId
    ) {
        return new FriendItemResponse(
                profile.getUser().getId(),
                profile.getName(),
                SportSummaryResponse.from(profile.getSport()),
                RegionSummaryResponse.from(profile.getRegion()),
                imageUrl,
                appointmentCount,
                chatRoomId
        );
    }
}
