package com.gdghajithon.review;

import com.gdghajithon.appointment.AppointmentQueryService;
import com.gdghajithon.friend.FriendService;
import com.gdghajithon.global.exception.BusinessException;
import com.gdghajithon.global.exception.ErrorCode;
import com.gdghajithon.image.ImageUrlResolver;
import com.gdghajithon.profile.Profile;
import com.gdghajithon.profile.ProfileRepository;
import com.gdghajithon.review.dto.ReviewCreateRequest;
import com.gdghajithon.review.dto.ReviewCreateResponse;
import com.gdghajithon.review.dto.ReviewListResponse;
import com.gdghajithon.user.User;
import com.gdghajithon.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private static final int MAX_TEXT_LENGTH = 500;

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final FriendService friendService;
    private final AppointmentQueryService appointmentQueryService;
    private final ImageUrlResolver imageUrlResolver;

    @Transactional
    public ReviewCreateResponse create(
            Long writerId,
            Long receiverId,
            ReviewCreateRequest request
    ) {
        validateRating(request.rating());
        validateOptionalText(request.content());
        validateOptionalText(request.imageUrl());
        friendService.validateFriend(writerId, receiverId);
        if (!appointmentQueryService.existsBetweenUsers(writerId, receiverId)) {
            throw new BusinessException(ErrorCode.APPOINTMENT_REQUIRED);
        }

        User writer = getUser(writerId);
        User receiver = getUser(receiverId);
        Review review = Review.create(
                writer,
                receiver,
                request.rating(),
                normalize(request.content()),
                normalize(request.imageUrl())
        );
        return ReviewCreateResponse.from(reviewRepository.save(review));
    }

    @Transactional(readOnly = true)
    public List<ReviewListResponse> getReviews(Long receiverId) {
        getUser(receiverId);
        return reviewRepository.findByReceiverIdOrderByCreatedAtDescIdDesc(receiverId)
                .stream()
                .map(review -> {
                    Profile writerProfile = getProfile(review.getWriter().getId());
                    return ReviewListResponse.from(
                            review,
                            writerProfile,
                            imageUrlResolver.resolve(writerProfile.getSport().getImageUrl())
                    );
                })
                .toList();
    }

    private void validateRating(Integer rating) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new BusinessException(ErrorCode.INVALID_RATING);
        }
    }

    private void validateOptionalText(String value) {
        if (value != null && value.length() > MAX_TEXT_LENGTH) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private Profile getProfile(Long userId) {
        return profileRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROFILE_NOT_FOUND));
    }
}
