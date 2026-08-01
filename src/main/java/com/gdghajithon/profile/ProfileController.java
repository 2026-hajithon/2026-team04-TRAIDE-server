package com.gdghajithon.profile;

import com.gdghajithon.global.security.AuthenticatedUser;
import com.gdghajithon.profile.dto.MyProfileResponse;
import com.gdghajithon.profile.dto.ProfileCreateRequest;
import com.gdghajithon.profile.dto.ProfileUpdateRequest;
import com.gdghajithon.profile.dto.UserDetailResponse;
import com.gdghajithon.profile.dto.UserRecommendationListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Profile", description = "사용자 프로필 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class ProfileController {

    private final ProfileService profileService;

    @Operation(summary = "프로필 생성", description = "회원가입 후 온보딩 프로필을 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "프로필 생성 성공"),
            @ApiResponse(responseCode = "400", description = "입력값 검증 실패"),
            @ApiResponse(responseCode = "404", description = "사용자, 운동 또는 지역 없음"),
            @ApiResponse(responseCode = "409", description = "프로필 중복")
    })
    @PostMapping("/me")
    @ResponseStatus(HttpStatus.CREATED)
    public MyProfileResponse create(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @Valid @RequestBody ProfileCreateRequest request
    ) {
        return profileService.create(authenticatedUser.userId(), request);
    }

    @Operation(
            summary = "내 프로필 조회",
            description = "친구 수, 전체 약속 수와 Review 임시 계약 값을 포함한 내 프로필을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "프로필 없음")
    })
    @GetMapping("/me")
    public MyProfileResponse getMyProfile(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return profileService.getMyProfile(authenticatedUser.userId());
    }

    @Operation(
            summary = "내 프로필 수정",
            description = "전달한 필드만 수정합니다. 빈 JSON 객체를 전달하면 기존 프로필을 그대로 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "입력값 검증 실패"),
            @ApiResponse(responseCode = "404", description = "프로필, 운동 또는 지역 없음")
    })
    @PatchMapping("/me")
    public MyProfileResponse update(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "수정할 필드만 전달합니다.",
                    content = @Content(
                            schema = @Schema(implementation = ProfileUpdateRequest.class),
                            examples = {
                                    @ExampleObject(name = "이름만 수정", value = "{\"name\":\"이정훈\"}"),
                                    @ExampleObject(
                                            name = "운동과 레벨 수정",
                                            value = "{\"sportId\":3,\"level\":\"ADVANCED\"}"
                                    )
                            }
                    )
            )
            @Valid @RequestBody ProfileUpdateRequest request
    ) {
        return profileService.update(authenticatedUser.userId(), request);
    }

    @Operation(
            summary = "추천 사용자 조회",
            description = "sportIds와 regionIds 내부는 OR, 두 조건 사이는 AND로 적용합니다. "
                    + "같은 지역 사용자를 우선하여 최대 10명을 items에 반환합니다."
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/recommendations")
    public UserRecommendationListResponse getRecommendations(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @Parameter(description = "운동 ID 목록", example = "2,3")
            @RequestParam(required = false) List<Long> sportIds,
            @Parameter(description = "지역 ID 목록", example = "1,19")
            @RequestParam(required = false) List<Long> regionIds
    ) {
        return profileService.getRecommendations(authenticatedUser.userId(), sportIds, regionIds);
    }

    @Operation(
            summary = "사용자 상세 프로필 조회",
            description = "friendStatus와 친구가 된 일수(friendSinceDays), 친구 수, 전체 약속 수를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "사용자 또는 프로필 없음")
    })
    @GetMapping("/{userId}")
    public UserDetailResponse getUserDetail(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable Long userId
    ) {
        return profileService.getUserDetail(authenticatedUser.userId(), userId);
    }
}
