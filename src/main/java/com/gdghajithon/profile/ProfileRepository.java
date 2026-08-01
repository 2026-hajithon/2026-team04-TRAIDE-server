package com.gdghajithon.profile;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProfileRepository extends JpaRepository<Profile, Long> {

    Optional<Profile> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    @EntityGraph(attributePaths = {"user", "sport", "region"})
    @Query("SELECT profile FROM Profile profile WHERE profile.user.id = :userId")
    Optional<Profile> findWithAssociationsByUserId(@Param("userId") Long userId);

    @Query("""
            SELECT profile
            FROM Profile profile
            JOIN FETCH profile.user candidate
            JOIN FETCH profile.sport sport
            JOIN FETCH profile.region region
            WHERE candidate.id <> :currentUserId
              AND (:sportId IS NULL OR sport.id = :sportId)
              AND (:regionId IS NULL OR region.id = :regionId)
              AND NOT EXISTS (
                  SELECT friendship.id
                  FROM Friendship friendship
                  WHERE (friendship.userA.id = :currentUserId
                         AND friendship.userB.id = candidate.id)
                     OR (friendship.userA.id = candidate.id
                         AND friendship.userB.id = :currentUserId)
              )
            ORDER BY candidate.id ASC
            """)
    List<Profile> findRecommendations(
            @Param("currentUserId") Long currentUserId,
            @Param("sportId") Long sportId,
            @Param("regionId") Long regionId,
            Pageable pageable
    );
}
