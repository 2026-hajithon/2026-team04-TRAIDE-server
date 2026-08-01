package com.gdghajithon.friend;

import com.gdghajithon.friend.dto.FriendListResponse;
import com.gdghajithon.global.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Friend", description = "친구 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/friends")
public class FriendController {

    private final FriendService friendService;

    @Operation(summary = "친구 목록 조회", description = "현재 사용자의 친구 목록과 친구별 전체 약속 수를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "친구 프로필 없음")
    })
    @GetMapping
    public FriendListResponse getFriends(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return friendService.getFriends(authenticatedUser.userId());
    }

    @Operation(summary = "친구 삭제", description = "현재 사용자와 대상 사용자의 Friendship만 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "친구 관계 없음")
    })
    @DeleteMapping("/{friendUserId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFriend(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable Long friendUserId
    ) {
        friendService.deleteFriend(authenticatedUser.userId(), friendUserId);
    }
}
