package com.onandhome.Notice.dto;

import com.onandhome.Notice.entity.Notice;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NoticeDto {

    private Long id;
    private String title;
    private String writer;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** ✅ Entity → DTO 변환 */
    public static NoticeDto fromEntity(Notice notice) {
        return new NoticeDto(
                notice.getId(),
                notice.getTitle(),
                notice.getWriter(),
                notice.getContent(),
                notice.getCreatedAt(),
                notice.getUpdatedAt()
        );
    }

    /** ✅ DTO → Entity 변환 */
    public Notice toEntity() {
        Notice notice = new Notice();
        notice.setId(this.id);
        notice.setTitle(this.title);
        notice.setWriter(this.writer);
        notice.setContent(this.content);
        notice.setCreatedAt(this.createdAt != null ? this.createdAt : LocalDateTime.now());
        notice.setUpdatedAt(this.updatedAt);
        return notice;
    }
}
