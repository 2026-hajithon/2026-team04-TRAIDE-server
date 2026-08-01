package com.gdghajithon.friend;

import com.gdghajithon.global.exception.BusinessException;
import com.gdghajithon.global.exception.ErrorCode;
import com.gdghajithon.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
        name = "friendship",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_friendship_user_a_user_b",
                columnNames = {"user_a_id", "user_b_id"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Friendship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_a_id", nullable = false)
    private User userA;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_b_id", nullable = false)
    private User userB;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private Friendship(User userA, User userB) {
        this.userA = userA;
        this.userB = userB;
    }

    public static Friendship create(User firstUser, User secondUser) {
        Long firstId = requirePersistedId(firstUser);
        Long secondId = requirePersistedId(secondUser);

        if (firstId.equals(secondId)) {
            throw new BusinessException(ErrorCode.CANNOT_FRIEND_SELF);
        }

        return firstId < secondId
                ? new Friendship(firstUser, secondUser)
                : new Friendship(secondUser, firstUser);
    }

    private static Long requirePersistedId(User user) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("Friendship은 저장된 User로만 생성할 수 있습니다.");
        }
        return user.getId();
    }

    @PrePersist
    private void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
