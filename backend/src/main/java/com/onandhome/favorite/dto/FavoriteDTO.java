package com.onandhome.favorite.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FavoriteDTO {
    private Long id;
    private Long userId;
    private Long productId;
    private String productName;
    private String productCode;
    private Integer price;
    private Integer salePrice;
    private String thumbnailImage;
    private String category;
    private Integer stock;
    private LocalDateTime createdAt;
}

//역할: 찜 목록 조회 시 클라이언트(User)에게 전달되는 데이터 전송 객체
//사용 이유: Entity를 직접 노출하지 않고, 필요한 정보만 조합하여 전송
//찜 기본 정보: id, userId, productId, createdAt
//상품 상세 정보: productName, productCode, price, salePrice, thumbnailImage, category, stock

