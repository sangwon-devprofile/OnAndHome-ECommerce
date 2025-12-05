package com.onandhome.review;

import com.onandhome.review.entity.ReviewReply;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/** 리뷰 답글 Repository */
public interface ReviewReplyRepository extends JpaRepository<ReviewReply, Long> {
    List<ReviewReply> findByReviewId(Long reviewId);
}
