package com.gdghajithon.friend;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    boolean existsByUserAIdAndUserBId(Long userAId, Long userBId);

    Optional<Friendship> findByUserAIdAndUserBId(Long userAId, Long userBId);

    @Query("""
            SELECT COUNT(friendship)
            FROM Friendship friendship
            WHERE friendship.userA.id = :userId OR friendship.userB.id = :userId
            """)
    long countByUserId(@Param("userId") Long userId);

    @Query("""
            SELECT CASE
                       WHEN friendship.userA.id = :userId THEN friendship.userB.id
                       ELSE friendship.userA.id
                   END
            FROM Friendship friendship
            WHERE friendship.userA.id = :userId OR friendship.userB.id = :userId
            ORDER BY friendship.id ASC
            """)
    List<Long> findFriendIdsByUserId(@Param("userId") Long userId);
}
