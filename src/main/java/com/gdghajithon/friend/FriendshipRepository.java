package com.gdghajithon.friend;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    boolean existsByUserAIdAndUserBId(Long userAId, Long userBId);

    Optional<Friendship> findByUserAIdAndUserBId(Long userAId, Long userBId);
}
