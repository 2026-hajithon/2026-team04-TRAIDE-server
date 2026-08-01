package com.gdghajithon.review;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewQueryService {

    private final ReviewRepository reviewRepository;

    public ReviewStats getStats(Long userId) {
        return new ReviewStats(
                reviewRepository.findAverageRatingByReceiverId(userId),
                reviewRepository.countByReceiverId(userId)
        );
    }
}
