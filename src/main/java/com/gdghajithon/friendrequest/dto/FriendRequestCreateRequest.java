package com.gdghajithon.friendrequest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record FriendRequestCreateRequest(
        @NotNull @Positive
        @Schema(example = "35")
        Long receiverUserId
) {
}
