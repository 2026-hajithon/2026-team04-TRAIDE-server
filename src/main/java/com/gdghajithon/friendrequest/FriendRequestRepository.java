package com.gdghajithon.friendrequest;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {

    @Query("""
            SELECT CASE WHEN COUNT(request) > 0 THEN true ELSE false END
            FROM FriendRequest request
            WHERE request.status = com.gdghajithon.friendrequest.FriendRequestStatus.PENDING
              AND ((request.sender.id = :firstUserId AND request.receiver.id = :secondUserId)
                OR (request.sender.id = :secondUserId AND request.receiver.id = :firstUserId))
            """)
    boolean existsPendingBetweenUsers(
            @Param("firstUserId") Long firstUserId,
            @Param("secondUserId") Long secondUserId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {"sender", "receiver"})
    @Query("SELECT request FROM FriendRequest request WHERE request.id = :requestId")
    Optional<FriendRequest> findByIdForUpdate(@Param("requestId") Long requestId);

    @EntityGraph(attributePaths = "sender")
    List<FriendRequest> findAllByReceiverIdAndStatusOrderByCreatedAtDesc(
            Long receiverId,
            FriendRequestStatus status
    );

    @EntityGraph(attributePaths = "receiver")
    List<FriendRequest> findAllBySenderIdAndStatusOrderByCreatedAtDesc(
            Long senderId,
            FriendRequestStatus status
    );
}
