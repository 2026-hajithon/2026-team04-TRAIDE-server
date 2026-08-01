package com.gdghajithon.friendrequest;

import com.gdghajithon.global.exception.BusinessException;
import com.gdghajithon.global.exception.ErrorCode;
import com.gdghajithon.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "friend_request",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_friend_request_pending_pair",
                columnNames = "pending_pair_key"
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FriendRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FriendRequestStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime respondedAt;

    @Column(name = "pending_pair_key", length = 50)
    private String pendingPairKey;

    private FriendRequest(User sender, User receiver) {
        Long senderId = requirePersistedId(sender);
        Long receiverId = requirePersistedId(receiver);
        if (senderId.equals(receiverId)) {
            throw new BusinessException(ErrorCode.CANNOT_REQUEST_SELF);
        }
        this.sender = sender;
        this.receiver = receiver;
        this.status = FriendRequestStatus.PENDING;
        this.pendingPairKey = createPairKey(senderId, receiverId);
    }

    public static FriendRequest create(User sender, User receiver) {
        return new FriendRequest(sender, receiver);
    }

    public void accept() {
        validatePending();
        this.status = FriendRequestStatus.ACCEPTED;
        complete();
    }

    public void reject() {
        validatePending();
        this.status = FriendRequestStatus.REJECTED;
        complete();
    }

    public boolean isPending() {
        return status == FriendRequestStatus.PENDING;
    }

    private void validatePending() {
        if (!isPending()) {
            throw new BusinessException(ErrorCode.FRIEND_REQUEST_ALREADY_PROCESSED);
        }
    }

    private void complete() {
        this.respondedAt = LocalDateTime.now();
        this.pendingPairKey = null;
    }

    private static String createPairKey(Long firstId, Long secondId) {
        return Math.min(firstId, secondId) + ":" + Math.max(firstId, secondId);
    }

    private static Long requirePersistedId(User user) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("FriendRequest는 저장된 User로만 생성할 수 있습니다.");
        }
        return user.getId();
    }

    @PrePersist
    private void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
