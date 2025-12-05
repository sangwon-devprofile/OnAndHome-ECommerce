package com.onandhome.review.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.onandhome.review.entity.Review;
import com.onandhome.review.entity.ReviewImage;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ✅ 리뷰 DTO
 * Review → ReviewDTO 변환 시, ReviewReply까지 포함
 */
@Getter
@Setter
public class ReviewDTO {

    private Long id;                // 리뷰 ID
    private String content;         // 리뷰 내용
    private int rating;             // 평점
    private String productName;     // 상품명
    private String author;          // 작성자명
    private String username;        // 사용자 계정명
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 상품 ID와 사용자 ID (관리자용)
    private Long productId;         // 상품 ID
    private Long userId;            // 사용자 ID

    // 별점 기능
    private Double averageRating;
    private Integer reviewCount;

    // 리뷰 좋아요 기능
    @JsonProperty("likedCount")
    private Integer likedCount;
    @JsonProperty("isLiked")
    private Boolean isLiked;

    // ✅ 리뷰에 연결된 답글 목록
    private List<ReviewReplyDTO> replies;

    // ✅ 리뷰에 연결된 이미지 URL 목록
    //   - review_image.image_url 값들이 들어감
    private List<String> imageUrls;

    private List<ReviewImageDTO> images;

    /** ✅ 엔티티 → DTO 변환 */
    public static ReviewDTO fromEntity(Review review) {
        ReviewDTO dto = new ReviewDTO();
        dto.setId(review.getId());
        dto.setContent(review.getContent());
        dto.setRating(review.getRating());
        dto.setProductName(review.getProductName());
        dto.setAuthor(review.getAuthor());
        dto.setUsername(review.getUsername());
        dto.setCreatedAt(review.getCreatedAt());
        dto.setUpdatedAt(review.getUpdatedAt());

        // 리뷰 좋아요 기능
        dto.setLikedCount(review.getLikeCount() != null ? review.getLikeCount() : 0);

        // Review ↔ Reply
        if (review.getReplies() != null) {
            dto.setReplies(
                    review.getReplies().stream()
                            .map(ReviewReplyDTO::fromEntity)
                            .collect(Collectors.toList())
            );
        }

        // ✅ 이미지 URL 목록 매핑 (ID 순 정렬)
        if (review.getImages() != null) {
            dto.setImageUrls(
                    review.getImages().stream()
                            .sorted(Comparator.comparing(ReviewImage::getId))
                            .map(img -> img.getImageUrl())
                            .collect(Collectors.toList())
            );
        }

        if (review.getImages() != null) {
            dto.setImages(
                    review.getImages().stream()
                            .sorted(Comparator.comparing(ReviewImage::getId))
                            .map(ReviewImageDTO::fromEntity)
                            .collect(Collectors.toList())
            );
        }

        return dto;
    }

    public void setReply(String content) {
    }
}
