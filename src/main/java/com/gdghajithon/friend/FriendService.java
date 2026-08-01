package com.gdghajithon.friend;

import com.gdghajithon.appointment.AppointmentRepository;
import com.gdghajithon.appointment.FriendAppointmentCount;
import com.gdghajithon.friend.dto.FriendItemResponse;
import com.gdghajithon.friend.dto.FriendListResponse;
import com.gdghajithon.global.exception.BusinessException;
import com.gdghajithon.global.exception.ErrorCode;
import com.gdghajithon.profile.Profile;
import com.gdghajithon.profile.ProfileRepository;
import com.gdghajithon.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FriendService {

    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final ProfileRepository profileRepository;
    private final AppointmentRepository appointmentRepository;

    @Transactional(readOnly = true)
    public void validateFriend(Long userId, Long targetUserId) {
        if (userId.equals(targetUserId)) {
            throw new BusinessException(ErrorCode.CANNOT_FRIEND_SELF);
        }

        validateUserExists(userId);
        validateUserExists(targetUserId);

        Long userAId = Math.min(userId, targetUserId);
        Long userBId = Math.max(userId, targetUserId);
        if (!friendshipRepository.existsByUserAIdAndUserBId(userAId, userBId)) {
            throw new BusinessException(ErrorCode.NOT_FRIEND);
        }
    }

    @Transactional(readOnly = true)
    public FriendListResponse getFriends(Long currentUserId) {
        List<Long> friendIds = friendshipRepository.findFriendIdsByUserId(currentUserId);
        if (friendIds.isEmpty()) {
            return FriendListResponse.of(List.of());
        }

        Map<Long, Profile> profiles = profileRepository.findAllByUserIdIn(friendIds)
                .stream()
                .collect(Collectors.toMap(profile -> profile.getUser().getId(), Function.identity()));
        Map<Long, Long> appointmentCounts = appointmentRepository
                .countByFriendUserIds(currentUserId, friendIds)
                .stream()
                .collect(Collectors.toMap(
                        FriendAppointmentCount::getFriendUserId,
                        FriendAppointmentCount::getAppointmentCount,
                        Long::sum
                ));

        List<FriendItemResponse> items = friendIds.stream()
                .map(friendId -> {
                    Profile profile = profiles.get(friendId);
                    if (profile == null) {
                        throw new BusinessException(ErrorCode.PROFILE_NOT_FOUND);
                    }
                    return FriendItemResponse.from(
                            profile,
                            appointmentCounts.getOrDefault(friendId, 0L),
                            createChatRoomId(currentUserId, friendId)
                    );
                })
                .toList();
        return FriendListResponse.of(items);
    }

    @Transactional
    public void deleteFriend(Long currentUserId, Long friendUserId) {
        if (currentUserId.equals(friendUserId)) {
            throw new BusinessException(ErrorCode.NOT_FRIEND);
        }

        Long userAId = Math.min(currentUserId, friendUserId);
        Long userBId = Math.max(currentUserId, friendUserId);
        userRepository.findAllByIdsForUpdate(List.of(userAId, userBId));

        Friendship friendship = friendshipRepository
                .findByUserAIdAndUserBId(userAId, userBId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FRIEND));
        friendshipRepository.delete(friendship);
    }

    private String createChatRoomId(Long firstUserId, Long secondUserId) {
        Long minId = Math.min(firstUserId, secondUserId);
        Long maxId = Math.max(firstUserId, secondUserId);
        return minId + "_" + maxId;
    }

    private void validateUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
    }
}
