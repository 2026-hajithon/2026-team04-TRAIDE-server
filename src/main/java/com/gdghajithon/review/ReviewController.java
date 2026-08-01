package com.gdghajithon.review;

import com.gdghajithon.global.security.AuthenticatedUser;
import com.gdghajithon.review.dto.ReviewCreateRequest;
import com.gdghajithon.review.dto.ReviewCreateResponse;
import com.gdghajithon.review.dto.ReviewListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Review", description = "후기 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users/{userId}/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "후기 작성")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewCreateResponse create(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable Long userId,
            @RequestBody ReviewCreateRequest request
    ) {
        return reviewService.create(authenticatedUser.userId(), userId, request);
    }

    @Operation(summary = "받은 후기 목록 조회")
    @GetMapping
    public List<ReviewListResponse> getReviews(@PathVariable Long userId) {
        return reviewService.getReviews(userId);
    }
}
