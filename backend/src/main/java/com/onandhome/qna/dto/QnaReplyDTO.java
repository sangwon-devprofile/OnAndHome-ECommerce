package com.onandhome.qna.dto;

import com.onandhome.qna.entity.QnaReply;
import lombok.*;

import java.time.LocalDateTime;

/**
 * QnaReply DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QnaReplyDTO {

    private Long id;
    private Long qnaId;        // 어떤 질문에 달린 답변인지
    private String content;    // 답변 내용
    private String responder;  // 답변 작성자
    private String author;     // 작성자 (프론트에드 호환용)
    private LocalDateTime createdAt;

    /** ✅ Entity → DTO 변환 */
    public static QnaReplyDTO fromEntity(QnaReply reply) {
        return QnaReplyDTO.builder()
                .id(reply.getId())
                .qnaId(reply.getQna() != null ? reply.getQna().getId() : null)
                .content(reply.getContent())
                .responder(reply.getResponder())
                .author(reply.getResponder())
                .createdAt(reply.getCreatedAt())
                .build();
    }
}
