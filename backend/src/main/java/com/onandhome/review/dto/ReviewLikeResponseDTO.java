package com.onandhome.review.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewLikeResponseDTO {

    @JsonProperty("isLiked")
    private boolean isLiked; // 좋아요 상태

    @JsonProperty("likedCount")
    private Integer likedCount; // 좋아요 개수
}
