package com.gdghajithon.friendrequest;

import com.gdghajithon.friend.Friendship;
import com.gdghajithon.friend.FriendshipRepository;
import com.gdghajithon.friendrequest.dto.FriendRequestCreateRequest;
import com.gdghajithon.friendrequest.dto.FriendRequestCreateResponse;
import com.gdghajithon.friendrequest.dto.FriendRequestItemResponse;
import com.gdghajithon.friendrequest.dto.FriendRequestListResponse;
import com.gdghajithon.friendrequest.dto.FriendRequestRespondRequest;
import com.gdghajithon.friendrequest.dto.FriendRequestStatusResponse;
import com.gdghajithon.global.exception.BusinessException;
import com.gdghajithon.global.exception.ErrorCode;
import com.gdghajithon.profile.Profile;
import com.gdghajithon.profile.ProfileRepository;
import com.gdghajithon.user.User;
import com.gdghajithon.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FriendRequestService {

    private static final String PENDING_PAIR_CONSTRAINT = "uk_friend_request_pending_pair";

    private final FriendRequestRepository friendRequestRepository;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final FriendshipRepository friendshipRepository;

    @Transactional
    public FriendRequestCreateResponse create(
            Long senderUserId,
            FriendRequestCreateRequest request
    ) {
        Long receiverUserId = request.receiverUserId();
        if (senderUserId.equals(receiverUserId)) {
            throw new BusinessException(ErrorCode.CANNOT_REQUEST_SELF);
        }

        Map<Long, User> users = lockUsers(senderUserId, receiverUserId);
        User sender = getLockedUser(users, senderUserId);
        User receiver = getLockedUser(users, receiverUserId);
        validateProfilesExist(senderUserId, receiverUserId);
        validateNotFriends(senderUserId, receiverUserId);
        validateNoPendingRequest(senderUserId, receiverUserId);

        try {
            FriendRequest saved = friendRequestRepository.saveAndFlush(
                    FriendRequest.create(sender, receiver));
            return FriendRequestCreateResponse.from(saved);
        } catch (DataIntegrityViolationException exception) {
            if (isPendingPairConstraintViolation(exception)) {
                throw new BusinessException(ErrorCode.FRIEND_REQUEST_ALREADY_PENDING);
            }
            throw exception;
        }
    }

    @Transactional(readOnly = true)
    public FriendRequestListResponse getReceived(Long receiverUserId) {
        List<FriendRequest> requests = friendRequestRepository
                .findAllByReceiverIdAndStatusOrderByCreatedAtDesc(
                        receiverUserId, FriendRequestStatus.PENDING);
        return toListResponse(requests, true);
    }

    @Transactional(readOnly = true)
    public FriendRequestListResponse getSent(Long senderUserId) {
        List<FriendRequest> requests = friendRequestRepository
                .findAllBySenderIdAndStatusOrderByCreatedAtDesc(
                        senderUserId, FriendRequestStatus.PENDING);
        return toListResponse(requests, false);
    }

    @Transactional
    public FriendRequestStatusResponse respond(
            Long currentUserId,
            Long requestId,
            FriendRequestRespondRequest response
    ) {
        if (response.status() != FriendRequestStatus.ACCEPTED
                && response.status() != FriendRequestStatus.REJECTED) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        FriendRequest request = getRequestForUpdate(requestId);
        if (!request.getReceiver().getId().equals(currentUserId)) {
            throw new BusinessException(ErrorCode.FRIEND_REQUEST_ACCESS_DENIED);
        }
        validatePending(request);

        if (response.status() == FriendRequestStatus.ACCEPTED) {
            Map<Long, User> users = lockUsers(
                    request.getSender().getId(), request.getReceiver().getId());
            User sender = getLockedUser(users, request.getSender().getId());
            User receiver = getLockedUser(users, request.getReceiver().getId());
            validateNotFriends(sender.getId(), receiver.getId());
            friendshipRepository.save(Friendship.create(sender, receiver));
            request.accept();
        } else {
            request.reject();
        }
        return FriendRequestStatusResponse.from(request);
    }

    @Transactional
    public void cancel(Long currentUserId, Long requestId) {
        FriendRequest request = getRequestForUpdate(requestId);
        if (!request.getSender().getId().equals(currentUserId)) {
            throw new BusinessException(ErrorCode.FRIEND_REQUEST_ACCESS_DENIED);
        }
        validatePending(request);
        friendRequestRepository.delete(request);
    }

    private FriendRequestListResponse toListResponse(
            List<FriendRequest> requests,
            boolean received
    ) {
        if (requests.isEmpty()) {
            return FriendRequestListResponse.of(List.of());
        }
        List<Long> counterpartIds = requests.stream()
                .map(request -> received
                        ? request.getSender().getId()
                        : request.getReceiver().getId())
                .distinct()
                .toList();
        Map<Long, Profile> profiles = profileRepository.findAllByUserIdIn(counterpartIds)
                .stream()
                .collect(Collectors.toMap(profile -> profile.getUser().getId(), Function.identity()));

        List<FriendRequestItemResponse> items = requests.stream()
                .map(request -> {
                    Long counterpartId = received
                            ? request.getSender().getId()
                            : request.getReceiver().getId();
                    Profile profile = profiles.get(counterpartId);
                    if (profile == null) {
                        throw new BusinessException(ErrorCode.PROFILE_NOT_FOUND);
                    }
                    return FriendRequestItemResponse.from(request, profile);
                })
                .toList();
        return FriendRequestListResponse.of(items);
    }

    private Map<Long, User> lockUsers(Long firstUserId, Long secondUserId) {
        List<Long> userIds = List.of(
                Math.min(firstUserId, secondUserId),
                Math.max(firstUserId, secondUserId)
        );
        return userRepository.findAllByIdsForUpdate(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
    }

    private User getLockedUser(Map<Long, User> users, Long userId) {
        User user = users.get(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return user;
    }

    private void validateProfilesExist(Long senderUserId, Long receiverUserId) {
        if (!profileRepository.existsByUserId(senderUserId)
                || !profileRepository.existsByUserId(receiverUserId)) {
            throw new BusinessException(ErrorCode.PROFILE_NOT_FOUND);
        }
    }

    private void validateNotFriends(Long firstUserId, Long secondUserId) {
        Long userAId = Math.min(firstUserId, secondUserId);
        Long userBId = Math.max(firstUserId, secondUserId);
        if (friendshipRepository.existsByUserAIdAndUserBId(userAId, userBId)) {
            throw new BusinessException(ErrorCode.ALREADY_FRIENDS);
        }
    }

    private void validateNoPendingRequest(Long firstUserId, Long secondUserId) {
        if (friendRequestRepository.existsPendingBetweenUsers(firstUserId, secondUserId)) {
            throw new BusinessException(ErrorCode.FRIEND_REQUEST_ALREADY_PENDING);
        }
    }

    private FriendRequest getRequestForUpdate(Long requestId) {
        return friendRequestRepository.findByIdForUpdate(requestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FRIEND_REQUEST_NOT_FOUND));
    }

    private void validatePending(FriendRequest request) {
        if (!request.isPending()) {
            throw new BusinessException(ErrorCode.FRIEND_REQUEST_ALREADY_PROCESSED);
        }
    }

    private boolean isPendingPairConstraintViolation(DataIntegrityViolationException exception) {
        Throwable cause = exception;
        while (cause != null) {
            String message = cause.getMessage();
            if (message != null
                    && message.toLowerCase(Locale.ROOT).contains(PENDING_PAIR_CONSTRAINT)) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }
}
