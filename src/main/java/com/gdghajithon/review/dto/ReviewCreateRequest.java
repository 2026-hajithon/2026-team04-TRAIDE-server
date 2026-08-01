package com.gdghajithon.review.dto;

public record ReviewCreateRequest(
        Integer rating,
        String content,
        String imageUrl
) {
}
