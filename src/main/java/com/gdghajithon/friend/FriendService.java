package com.gdghajithon.friend;

import com.gdghajithon.global.exception.BusinessException;
import com.gdghajithon.global.exception.ErrorCode;
import com.gdghajithon.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FriendService {

    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;

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
            throw new BusinessException(ErrorCode.FRIENDSHIP_REQUIRED);
        }
    }

    private void validateUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
    }
}
