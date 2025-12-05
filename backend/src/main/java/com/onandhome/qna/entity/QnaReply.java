package com.onandhome.qna.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * QnaReply (답변) 엔티티
 */
@Entity
@Table(name = "qna_reply")
@Getter
@Setter
@NoArgsConstructor
public class QnaReply {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ✅ Qna와 다대일 관계 (하나의 질문에 여러 답변)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "qna_id", nullable = false)
    private Qna qna;

    private String content;    // 답변 내용
    private String responder;  // 작성자
    private LocalDateTime createdAt = LocalDateTime.now();
}
