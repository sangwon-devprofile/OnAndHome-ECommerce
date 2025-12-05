package com.onandhome.qna.entity;

import com.onandhome.admin.adminProduct.entity.Product;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 질문(QnA) 엔티티
 */
@Entity
@Table(name = "qna")
@Getter
@Setter
@NoArgsConstructor
public class Qna {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;     // 질문 제목
    private String writer;    // 작성자
    private String question;  // 질문 내용
    private LocalDateTime createdAt = LocalDateTime.now();

    // ✅ 비밀글 여부 추가
    @Column(name = "is_private", nullable = false)
    private Boolean isPrivate = false;

    // ✅ 제품 정보 연결 (기존 유지)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    // ✅ 여러 개의 리플라이(답변) 연결 (1:N 관계)
    @OneToMany(mappedBy = "qna", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QnaReply> replies = new ArrayList<>();

    /** ✅ 편의 메서드: Qna에 리플라이 추가 시 자동 연결 */
    public void addReply(QnaReply reply) {
        replies.add(reply);
        reply.setQna(this);
    }

    // QnA ↔ 이미지 (1:N)
    @OneToMany(mappedBy = "qna", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QnaImage> images = new ArrayList<>();

    public void addImage(QnaImage image) {
        images.add(image);
        image.setQna(this);
    }

    public void removeImage(QnaImage image) {
        images.remove(image);
        image.setQna(null);
    }
}