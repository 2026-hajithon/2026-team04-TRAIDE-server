package com.gdghajithon.friendrequest;

import com.gdghajithon.friendrequest.dto.FriendRequestCreateRequest;
import com.gdghajithon.friendrequest.dto.FriendRequestCreateResponse;
import com.gdghajithon.friendrequest.dto.FriendRequestListResponse;
import com.gdghajithon.friendrequest.dto.FriendRequestRespondRequest;
import com.gdghajithon.friendrequest.dto.FriendRequestStatusResponse;
import com.gdghajithon.global.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "FriendRequest", description = "친구 요청 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/friend-requests")
public class FriendRequestController {

    private final FriendRequestService friendRequestService;

    @Operation(summary = "친구 요청 보내기", description = "양방향으로 하나의 PENDING 요청만 허용합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "요청 생성 성공"),
            @ApiResponse(responseCode = "400", description = "자기 자신 요청 또는 입력값 오류"),
            @ApiResponse(responseCode = "404", description = "사용자 또는 프로필 없음"),
            @ApiResponse(responseCode = "409", description = "이미 친구이거나 PENDING 요청 존재")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FriendRequestCreateResponse create(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @Valid @RequestBody FriendRequestCreateRequest request
    ) {
        return friendRequestService.create(authenticatedUser.userId(), request);
    }

    @Operation(summary = "받은 친구 요청 조회", description = "받은 PENDING 요청을 최신순으로 반환합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/received")
    public FriendRequestListResponse getReceived(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return friendRequestService.getReceived(authenticatedUser.userId());
    }

    @Operation(summary = "보낸 친구 요청 조회", description = "보낸 PENDING 요청을 최신순으로 반환합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/sent")
    public FriendRequestListResponse getSent(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return friendRequestService.getSent(authenticatedUser.userId());
    }

    @Operation(summary = "친구 요청 응답", description = "receiver만 PENDING 요청을 수락하거나 거절할 수 있습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "처리 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 상태"),
            @ApiResponse(responseCode = "403", description = "처리 권한 없음"),
            @ApiResponse(responseCode = "404", description = "요청 없음"),
            @ApiResponse(responseCode = "409", description = "이미 처리된 요청 또는 이미 친구")
    })
    @PatchMapping("/{requestId}")
    public FriendRequestStatusResponse respond(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable Long requestId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            schema = @Schema(implementation = FriendRequestRespondRequest.class),
                            examples = {
                                    @ExampleObject(name = "수락", value = "{\"status\":\"ACCEPTED\"}"),
                                    @ExampleObject(name = "거절", value = "{\"status\":\"REJECTED\"}")
                            }
                    )
            )
            @Valid @RequestBody FriendRequestRespondRequest request
    ) {
        return friendRequestService.respond(authenticatedUser.userId(), requestId, request);
    }

    @Operation(summary = "친구 요청 취소", description = "sender만 PENDING 요청을 취소할 수 있습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "취소 성공"),
            @ApiResponse(responseCode = "403", description = "취소 권한 없음"),
            @ApiResponse(responseCode = "404", description = "요청 없음"),
            @ApiResponse(responseCode = "409", description = "이미 처리된 요청")
    })
    @DeleteMapping("/{requestId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable Long requestId
    ) {
        friendRequestService.cancel(authenticatedUser.userId(), requestId);
    }
}
