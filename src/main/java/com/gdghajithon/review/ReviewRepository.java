package com.gdghajithon.review;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    long countByReceiverId(Long receiverId);

    @Query("SELECT AVG(review.rating) FROM Review review WHERE review.receiver.id = :receiverId")
    Double findAverageRatingByReceiverId(@Param("receiverId") Long receiverId);

    @EntityGraph(attributePaths = "writer")
    List<Review> findByReceiverIdOrderByCreatedAtDescIdDesc(Long receiverId);
}
