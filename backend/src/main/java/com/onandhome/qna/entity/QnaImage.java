package com.onandhome.qna.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * QnA 이미지 엔티티 (QnaImage)
 */
@Entity
@Table(name = "qna_image")
@Getter
@Setter
@NoArgsConstructor
public class QnaImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 이미지 기본키

    // ✅ 여러 이미지가 하나의 Qna에 매핑 (N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "qna_id", nullable = false)
    private Qna qna;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl; // 실제 이미지 URL (/uploads/파일명)

    @Column(name = "created_at")
    private LocalDateTime createdAt; // 생성 시각

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}
