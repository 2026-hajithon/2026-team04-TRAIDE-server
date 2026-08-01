package com.gdghajithon.review.dto;

import com.gdghajithon.profile.Profile;
import com.gdghajithon.review.Review;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record ReviewListResponse(
        Long id,
        Integer rating,
        String content,
        String imageUrl,
        LocalDateTime createdAt,
        WriterResponse writer
) {

    public static ReviewListResponse from(
            Review review,
            Profile writerProfile,
            String writerImageUrl
    ) {
        return new ReviewListResponse(
                review.getId(),
                review.getRating(),
                review.getContent(),
                review.getImageUrl(),
                review.getCreatedAt(),
                new WriterResponse(
                        review.getWriter().getId(),
                        writerProfile.getName(),
                        writerImageUrl
                )
        );
    }

    public record WriterResponse(
            Long id,
            String name,
            @Schema(description = "작성자가 선택한 운동 종목의 기본 프로필 이미지 URL")
            String imageUrl
    ) {
    }
}
