package com.onandhome.review.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * 리뷰 답글 엔티티 (DB 컬럼 완전 매칭)
 */
@Entity
@Getter
@Setter
@Table(name = "review_reply")
public class ReviewReply {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 기본키

    @Column(nullable = false, length = 1000)
    private String content; // 답글 내용

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now(); // 작성 시각

    // ✅ 부모 리뷰 (외래키)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    @Column(name = "user_id")
    private Long userId; // 회원 고유 식별자

    @Column(name = "author")
    private String author; // 작성자 별칭

    @Column(name = "username", nullable = false)
    private String username; // 로그인 계정명

    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // 수정 시각

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
