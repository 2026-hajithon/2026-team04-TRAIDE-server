package com.gdghajithon.profile;

import com.gdghajithon.appointment.AppointmentRepository;
import com.gdghajithon.friend.Friendship;
import com.gdghajithon.friend.FriendshipRepository;
import com.gdghajithon.friendrequest.FriendRequestRepository;
import com.gdghajithon.global.exception.BusinessException;
import com.gdghajithon.global.exception.ErrorCode;
import com.gdghajithon.image.ImageUrlResolver;
import com.gdghajithon.profile.dto.MyProfileResponse;
import com.gdghajithon.profile.dto.ProfileCreateRequest;
import com.gdghajithon.profile.dto.ProfileUpdateRequest;
import com.gdghajithon.profile.dto.UserDetailResponse;
import com.gdghajithon.profile.dto.UserRecommendationListResponse;
import com.gdghajithon.profile.dto.UserRecommendationResponse;
import com.gdghajithon.region.Region;
import com.gdghajithon.region.RegionRepository;
import com.gdghajithon.review.ReviewQueryService;
import com.gdghajithon.review.ReviewStats;
import com.gdghajithon.sport.Sport;
import com.gdghajithon.sport.SportRepository;
import com.gdghajithon.user.User;
import com.gdghajithon.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private static final int RECOMMENDATION_LIMIT = 10;
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final SportRepository sportRepository;
    private final RegionRepository regionRepository;
    private final FriendshipRepository friendshipRepository;
    private final FriendRequestRepository friendRequestRepository;
    private final AppointmentRepository appointmentRepository;
    private final ReviewQueryService reviewQueryService;
    private final ImageUrlResolver imageUrlResolver;

    @Transactional
    public MyProfileResponse create(Long userId, ProfileCreateRequest request) {
        User user = getUser(userId);
        if (profileRepository.existsByUserId(userId)) {
            throw new BusinessException(ErrorCode.PROFILE_ALREADY_EXISTS);
        }

        Sport sport = getSport(request.sportId());
        Region region = getRegion(request.regionId());
        Profile profile = Profile.create(
                user,
                request.name(),
                request.age(),
                request.gender(),
                sport,
                request.level(),
                region
        );
        Profile savedProfile = profileRepository.save(profile);
        return toMyProfileResponse(savedProfile);
    }

    @Transactional(readOnly = true)
    public MyProfileResponse getMyProfile(Long userId) {
        Profile profile = getProfile(userId);
        return toMyProfileResponse(profile);
    }

    @Transactional
    public MyProfileResponse update(Long userId, ProfileUpdateRequest request) {
        Profile profile = getProfile(userId);
        Sport sport = request.sportId() == null ? null : getSport(request.sportId());
        Region region = request.regionId() == null ? null : getRegion(request.regionId());
        profile.update(
                request.name(),
                request.age(),
                request.gender(),
                sport,
                request.level(),
                region
        );
        return toMyProfileResponse(profile);
    }

    @Transactional(readOnly = true)
    public UserDetailResponse getUserDetail(Long currentUserId, Long targetUserId) {
        getUser(targetUserId);
        Profile profile = getProfile(targetUserId);
        long friendCount = friendshipRepository.countByUserId(targetUserId);
        long appointmentCount = appointmentRepository.countByUserId(targetUserId);
        ReviewStats reviewStats = reviewQueryService.getStats(targetUserId);

        if (currentUserId.equals(targetUserId)) {
            return UserDetailResponse.from(
                    profile,
                    getProfileImageUrl(profile),
                    friendCount,
                    appointmentCount,
                    reviewStats.averageRating(),
                    reviewStats.reviewCount(),
                    FriendStatus.NONE,
                    null
            );
        }

        Long userAId = Math.min(currentUserId, targetUserId);
        Long userBId = Math.max(currentUserId, targetUserId);
        Friendship friendship = friendshipRepository
                .findByUserAIdAndUserBId(userAId, userBId)
                .orElse(null);
        FriendStatus status;
        Long friendSinceDays;
        if (friendship != null) {
            status = FriendStatus.FRIEND;
            friendSinceDays = calculateFriendSinceDays(friendship);
        } else if (friendRequestRepository.existsPendingBetweenUsers(
                currentUserId, targetUserId)) {
            status = FriendStatus.PENDING;
            friendSinceDays = null;
        } else {
            status = FriendStatus.NONE;
            friendSinceDays = null;
        }
        return UserDetailResponse.from(
                profile,
                getProfileImageUrl(profile),
                friendCount,
                appointmentCount,
                reviewStats.averageRating(),
                reviewStats.reviewCount(),
                status,
                friendSinceDays
        );
    }

    @Transactional(readOnly = true)
    public UserRecommendationListResponse getRecommendations(
            Long currentUserId,
            List<Long> sportIds,
            List<Long> regionIds
    ) {
        Profile currentProfile = getProfile(currentUserId);
        List<Long> normalizedSportIds = normalizeFilterIds(sportIds);
        List<Long> normalizedRegionIds = normalizeFilterIds(regionIds);
        List<Profile> candidates = profileRepository.findRecommendationCandidates(
                currentUserId,
                !normalizedSportIds.isEmpty(),
                normalizedSportIds,
                !normalizedRegionIds.isEmpty(),
                normalizedRegionIds
        );

        Long currentRegionId = currentProfile.getRegion().getId();
        List<Profile> sameRegion = new ArrayList<>();
        List<Profile> otherRegion = new ArrayList<>();
        for (Profile candidate : candidates) {
            if (candidate.getRegion().getId().equals(currentRegionId)) {
                sameRegion.add(candidate);
            } else {
                otherRegion.add(candidate);
            }
        }
        Collections.shuffle(sameRegion);
        Collections.shuffle(otherRegion);

        List<UserRecommendationResponse> recommendations = new ArrayList<>(RECOMMENDATION_LIMIT);
        appendRecommendations(recommendations, sameRegion);
        appendRecommendations(recommendations, otherRegion);
        return UserRecommendationListResponse.of(recommendations);
    }

    private MyProfileResponse toMyProfileResponse(Profile profile) {
        Long userId = profile.getUser().getId();
        ReviewStats reviewStats = reviewQueryService.getStats(userId);
        return MyProfileResponse.from(
                profile,
                getProfileImageUrl(profile),
                friendshipRepository.countByUserId(userId),
                appointmentRepository.countByUserId(userId),
                reviewStats.averageRating(),
                reviewStats.reviewCount()
        );
    }

    private Long calculateFriendSinceDays(Friendship friendship) {
        LocalDate friendSince = friendship.getCreatedAt().toLocalDate();
        return ChronoUnit.DAYS.between(friendSince, LocalDate.now()) + 1;
    }

    private List<Long> normalizeFilterIds(List<Long> ids) {
        if (ids == null) {
            return List.of();
        }
        return ids.stream()
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();
    }

    private void appendRecommendations(
            List<UserRecommendationResponse> recommendations,
            List<Profile> candidates
    ) {
        for (Profile candidate : candidates) {
            if (recommendations.size() == RECOMMENDATION_LIMIT) {
                return;
            }
            ReviewStats reviewStats = reviewQueryService.getStats(candidate.getUser().getId());
            recommendations.add(UserRecommendationResponse.from(
                    candidate,
                    getProfileImageUrl(candidate),
                    reviewStats.averageRating(),
                    reviewStats.reviewCount()
            ));
        }
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private Profile getProfile(Long userId) {
        return profileRepository.findWithAssociationsByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROFILE_NOT_FOUND));
    }

    private Sport getSport(Long sportId) {
        return sportRepository.findById(sportId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SPORT_NOT_FOUND));
    }

    private String getProfileImageUrl(Profile profile) {
        return imageUrlResolver.resolve(profile.getSport().getImageUrl());
    }

    private Region getRegion(Long regionId) {
        return regionRepository.findById(regionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REGION_NOT_FOUND));
    }
}
