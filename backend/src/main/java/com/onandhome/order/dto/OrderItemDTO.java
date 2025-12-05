package com.onandhome.order.dto;

import com.onandhome.order.entity.OrderItem;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class OrderItemDTO {

    private Long orderItemId;
    private Long id; // 관리자용

    private Long productId;

    private String productName;

    private int productPrice; // 상품 가격

    private int orderPrice; // 주문 가격

    private int price; // orderPrice 별칭 (프론트엔드 호환성)

    private int quantity;

    // 상품 이미지 정보 추가
    private String thumbnailImage;  // 썸네일 이미지 (Product 엔티티에서 가져옴)
    private String productImage;    // thumbnailImage 별칭 (프론트엔드 호환성)
    private String image;           // thumbnailImage 별칭 (프론트엔드 호환성)
    private String mainImg;         // thumbnailImage 별칭 (프론트엔드 호환성)
    private String detailImage;     // 상세 이미지 (Product 엔티티에서 가져옴)

    /**
     * OrderItem Entity를 DTO로 변환
     */
    public static OrderItemDTO fromEntity(OrderItem orderItem) {
        // Product 엔티티에서 썸네일 이미지 가져오기
        String thumbnail = orderItem.getProduct().getThumbnailImage();
        String detail = orderItem.getProduct().getDetailImage();

        return OrderItemDTO.builder()
                .id(orderItem.getId())
                .orderItemId(orderItem.getId())
                .productId(orderItem.getProduct().getId())
                .productName(orderItem.getProduct().getName())
                .productPrice(orderItem.getProduct().getPrice())
                .orderPrice(orderItem.getOrderPrice())
                .price(orderItem.getOrderPrice()) // 별칭
                .quantity(orderItem.getCount())
                // 이미지 정보 추가 (프론트엔드에서 다양한 필드명으로 접근 가능하도록 별칭 제공)
                .thumbnailImage(thumbnail)
                .productImage(thumbnail)   // 별칭
                .image(thumbnail)          // 별칭
                .mainImg(thumbnail)        // 별칭
                .detailImage(detail)
                .build();
    }

    /**
     * 주문 상품의 총 가격 계산
     */
    public int getTotalPrice() {
        return orderPrice * quantity;
    }
}