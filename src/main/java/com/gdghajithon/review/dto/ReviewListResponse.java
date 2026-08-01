package com.gdghajithon.review.dto;

import com.gdghajithon.profile.Profile;
import com.gdghajithon.review.Review;

import java.time.LocalDateTime;

public record ReviewListResponse(
        Long id,
        Integer rating,
        String content,
        String imageUrl,
        LocalDateTime createdAt,
        WriterResponse writer
) {

    public static ReviewListResponse from(Review review, Profile writerProfile) {
        return new ReviewListResponse(
                review.getId(),
                review.getRating(),
                review.getContent(),
                review.getImageUrl(),
                review.getCreatedAt(),
                new WriterResponse(
                        review.getWriter().getId(),
                        writerProfile.getName(),
                        null
                )
        );
    }

    public record WriterResponse(
            Long id,
            String name,
            String imageUrl
    ) {
    }
}
