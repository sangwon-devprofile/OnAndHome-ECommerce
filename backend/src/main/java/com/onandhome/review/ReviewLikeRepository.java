package com.onandhome.review;

import com.onandhome.review.entity.ReviewLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewLikeRepository extends JpaRepository<ReviewLike, Long> {
    // 특정 리뷰에 특정 유저가 좋아요 했는지 찾기
    Optional<ReviewLike> findByReviewIdAndUserId(Long reviewId, Long userId);
    // 좋아요 입력 여부
    boolean existsByReviewIdAndUserId(Long reviewId, Long userId);
}

