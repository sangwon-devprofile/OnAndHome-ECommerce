package com.onandhome.review.entity;

import com.onandhome.admin.adminProduct.entity.Product;
import com.onandhome.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * 리뷰 엔티티 (Review)
 */
@Entity
@Table(name = "review")
@Getter
@Setter
@NoArgsConstructor
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 기본키

    @Column(nullable = false, length = 1000)
    private String content; // 리뷰 내용

    @Column(nullable = false)
    private int rating; // 평점

    @Column(name = "product_name", length = 255)
    private String productName; // 상품명

    @Column(length = 255)
    private String author; // 작성자 이름 (표시용)

    @Column(length = 255)
    private String username; // 로그인 아이디 (옵션)

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now(); // 작성 시각

    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // 수정 시각

    @Column(name = "like_count")
    private Integer likeCount = 0; // 작성된 리뷰에 좋아요

    // ✅ 상품 연관관계 (FK)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    // ✅ 사용자 연관관계 (FK)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ReviewReply> replies = new HashSet<>();

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ReviewImage> images = new HashSet<>();

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ReviewLike> likes = new HashSet<>();

    /** ✅ 수정일 자동 업데이트 */
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /** ✅ 편의 메서드: Review에 Reply 추가 */
    public void addReply(ReviewReply reply) {
        replies.add(reply);
        reply.setReview(this);
    }

    /** ✅ 편의 메서드: Review에 Image 추가 */
    public void addImage(ReviewImage image) {
        images.add(image);
        image.setReview(this);
    }

    /** ✅ 편의 메서드: Review에서 Image 제거 */
    public void removeImage(ReviewImage image) {
        images.remove(image);
        image.setReview(null);
    }
}
