package com.onandhome.review;

import com.onandhome.review.entity.ReviewImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewImageRepository extends JpaRepository<ReviewImage, Long> {

    // 특정 리뷰에 속한 이미지 목록 조회
    List<ReviewImage> findByReviewId(Long reviewId);

    // 필요하다면: 리뷰 기준으로 전체 삭제 (ON DELETE CASCADE 있으니 자주 쓸 일은 적음)
    void deleteByReviewId(Long reviewId);
}
