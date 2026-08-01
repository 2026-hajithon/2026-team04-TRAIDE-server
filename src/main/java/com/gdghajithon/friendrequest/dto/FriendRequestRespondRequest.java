package com.gdghajithon.friendrequest.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gdghajithon.friendrequest.FriendRequestStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

public record FriendRequestRespondRequest(
        @NotNull
        @Schema(allowableValues = {"ACCEPTED", "REJECTED"}, example = "ACCEPTED")
        FriendRequestStatus status
) {
    @JsonIgnore
    @AssertTrue(message = "status는 ACCEPTED 또는 REJECTED여야 합니다.")
    public boolean isRespondableStatus() {
        return status == null
                || status == FriendRequestStatus.ACCEPTED
                || status == FriendRequestStatus.REJECTED;
    }
}
