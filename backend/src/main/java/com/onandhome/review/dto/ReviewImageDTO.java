package com.onandhome.review.dto;

import com.onandhome.review.entity.ReviewImage;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewImageDTO {
    private Long id;
    private String url;

    public static ReviewImageDTO fromEntity(ReviewImage image) {
        ReviewImageDTO dto = new ReviewImageDTO();
        dto.setId(image.getId());
        dto.setUrl(image.getImageUrl());
        return dto;
    }
}
