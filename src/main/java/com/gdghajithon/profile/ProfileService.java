package com.gdghajithon.profile;

import com.gdghajithon.friend.Friendship;
import com.gdghajithon.friend.FriendshipRepository;
import com.gdghajithon.global.exception.BusinessException;
import com.gdghajithon.global.exception.ErrorCode;
import com.gdghajithon.profile.dto.MyProfileResponse;
import com.gdghajithon.profile.dto.ProfileCreateRequest;
import com.gdghajithon.profile.dto.ProfileUpdateRequest;
import com.gdghajithon.profile.dto.UserDetailResponse;
import com.gdghajithon.profile.dto.UserRecommendationResponse;
import com.gdghajithon.region.Region;
import com.gdghajithon.region.RegionRepository;
import com.gdghajithon.sport.Sport;
import com.gdghajithon.sport.SportRepository;
import com.gdghajithon.user.User;
import com.gdghajithon.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                request.exerciseLevel(),
                region
        );
        Profile savedProfile = profileRepository.save(profile);
        return MyProfileResponse.from(savedProfile, friendshipRepository.countByUserId(userId));
    }

    @Transactional(readOnly = true)
    public MyProfileResponse getMyProfile(Long userId) {
        Profile profile = getProfile(userId);
        return MyProfileResponse.from(profile, friendshipRepository.countByUserId(userId));
    }

    @Transactional
    public MyProfileResponse update(Long userId, ProfileUpdateRequest request) {
        Profile profile = getProfile(userId);
        Sport sport = getSport(request.sportId());
        Region region = getRegion(request.regionId());
        profile.update(
                request.name(),
                request.age(),
                request.gender(),
                sport,
                request.exerciseLevel(),
                region
        );
        return MyProfileResponse.from(profile, friendshipRepository.countByUserId(userId));
    }

    @Transactional(readOnly = true)
    public UserDetailResponse getUserDetail(Long currentUserId, Long targetUserId) {
        getUser(targetUserId);
        Profile profile = getProfile(targetUserId);
        long friendCount = friendshipRepository.countByUserId(targetUserId);

        if (currentUserId.equals(targetUserId)) {
            return UserDetailResponse.from(profile, friendCount, FriendStatus.NONE, null);
        }

        Long userAId = Math.min(currentUserId, targetUserId);
        Long userBId = Math.max(currentUserId, targetUserId);
        Friendship friendship = friendshipRepository
                .findByUserAIdAndUserBId(userAId, userBId)
                .orElse(null);
        FriendStatus status = friendship == null ? FriendStatus.NONE : FriendStatus.FRIEND;
        return UserDetailResponse.from(profile, friendCount, status, friendship);
    }

    @Transactional(readOnly = true)
    public List<UserRecommendationResponse> getRecommendations(
            Long currentUserId,
            Long sportId,
            Long regionId
    ) {
        return profileRepository.findRecommendations(
                        currentUserId,
                        sportId,
                        regionId,
                        PageRequest.of(0, RECOMMENDATION_LIMIT)
                ).stream()
                .map(UserRecommendationResponse::from)
                .toList();
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

    private Region getRegion(Long regionId) {
        return regionRepository.findById(regionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REGION_NOT_FOUND));
    }
}
