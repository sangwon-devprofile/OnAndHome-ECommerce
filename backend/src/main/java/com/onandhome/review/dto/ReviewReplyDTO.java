package com.onandhome.review.dto;

import com.onandhome.review.entity.ReviewReply;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * ✅ 리뷰 답글 DTO
 */
@Getter
@Setter
public class ReviewReplyDTO {

    private Long id;
    private String content;
    private String author;
    private String username;
    private Long userId;
    private Long reviewId; // ✅ AdminReviewController 에서 getReviewId()로 접근
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String productName; // ✅ 추가: 상품명

    /** ✅ 엔티티 → DTO 변환 */
    public static ReviewReplyDTO fromEntity(ReviewReply reply) {
        ReviewReplyDTO dto = new ReviewReplyDTO();
        dto.setId(reply.getId());
        dto.setContent(reply.getContent());
        dto.setAuthor(reply.getAuthor());
        dto.setUsername(reply.getUsername());
        dto.setUserId(reply.getUserId());
        dto.setReviewId(reply.getReview() != null ? reply.getReview().getId() : null);
        dto.setCreatedAt(reply.getCreatedAt());
        dto.setUpdatedAt(reply.getUpdatedAt());

        // ✅ Review 가 존재하면 상품명 세팅
        if (reply.getReview() != null) {
            dto.setProductName(reply.getReview().getProductName());
        }

        return dto;
    }
}
