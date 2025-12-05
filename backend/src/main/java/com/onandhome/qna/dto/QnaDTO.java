package com.onandhome.qna.dto;

import com.onandhome.qna.entity.Qna;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * QnA 데이터 전송용 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QnaDTO {

    private Long id;
    private String title;
    private String writer;
    private String question;
    private LocalDateTime createdAt;
    private List<QnaReplyDTO> replies;
    private Long productId;
    private String productName;
    private Long userId;

    // ✅ 비밀글 여부 추가
    private Boolean isPrivate;
    // ✅ 이미지 URL 리스트
    private List<String> imageUrls;
    private List<QnaImageDTO> images;

    /** ✅ Entity → DTO 변환 */
    public static QnaDTO fromEntity(Qna qna) {
        return QnaDTO.builder()
                .id(qna.getId())
                .title(qna.getTitle())
                .writer(qna.getWriter())
                .question(qna.getQuestion())
                .createdAt(qna.getCreatedAt())
                .isPrivate(qna.getIsPrivate())
                .productId(qna.getProduct() != null ? qna.getProduct().getId() : null)
                .productName(qna.getProduct() != null ? qna.getProduct().getName() : null)
                .replies(qna.getReplies() != null
                        ? qna.getReplies().stream()
                        .map(QnaReplyDTO::fromEntity)
                        .collect(Collectors.toList())
                        : null)
                .build();
    }
}
