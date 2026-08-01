package com.gdghajithon.review.dto;

import com.gdghajithon.review.Review;

import java.time.LocalDateTime;

public record ReviewCreateResponse(
        Long id,
        Integer rating,
        String content,
        String imageUrl,
        LocalDateTime createdAt
) {

    public static ReviewCreateResponse from(Review review) {
        return new ReviewCreateResponse(
                review.getId(),
                review.getRating(),
                review.getContent(),
                review.getImageUrl(),
                review.getCreatedAt()
        );
    }
}
