package com.onandhome.advertisement.dto;

import com.onandhome.advertisement.entity.Advertisement;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdvertisementDTO {
    
    private Long id;
    private String title;
    private String content;
    private String imageUrl;
    private String linkUrl;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime sentAt;
    
    public static AdvertisementDTO fromEntity(Advertisement advertisement) {
        return AdvertisementDTO.builder()
                .id(advertisement.getId())
                .title(advertisement.getTitle())
                .content(advertisement.getContent())
                .imageUrl(advertisement.getImageUrl())
                .linkUrl(advertisement.getLinkUrl())
                .active(advertisement.getActive())
                .createdAt(advertisement.getCreatedAt())
                .updatedAt(advertisement.getUpdatedAt())
                .sentAt(advertisement.getSentAt())
                .build();
    }
    
    public Advertisement toEntity() {
        return Advertisement.builder()
                .id(this.id)
                .title(this.title)
                .content(this.content)
                .imageUrl(this.imageUrl)
                .linkUrl(this.linkUrl)
                .active(this.active)
                .sentAt(this.sentAt)
                .build();
    }
}
