package com.onandhome.order.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class CreateOrderRequest {

    /* 주문 생성 요청을 보내는 사용자 ID
       OrderService.createOrder() 내부에서 주문자의 정보를 조회하는 데 사용된다. */
    private Long userId;

    /* 주문 항목 목록
       각 항목의 상품 ID와 수량이 포함된다.
       결제 완료 후 OrderItem 리스트로 변환되어 실제 주문에 포함된다. */
    private List<OrderItemRequest> orderItems;

    /* 결제 방법
       CARD → 주문 생성 시 즉시 상태가 ORDERED가 된다.
       BANK_TRANSFER → PAYMENT_PENDING 상태로 저장되고 입금 확인 시 ORDERED로 변경된다.
       결제 흐름에서 중요한 분기점이 되는 필드이다. */
    @Builder.Default
    private String paymentMethod = "CARD";

    /* 배송 정보
       주문 완료 후 Order 엔티티에 그대로 매핑되어 저장된다.
       결제 완료 여부와 관계없이 주문 생성 단계에서 함께 기록된다. */
    private String recipientName;     // 받는 사람
    private String recipientPhone;    // 연락처
    private String shippingAddress;   // 배송지 주소
    private String shippingRequest;   // 배송 요청 메시지

    /**
     * 개별 주문 항목 요청 정보
     * 상품 ID(productId)와 주문 수량(quantity)을 포함한다.
     * 주문 생성 시 OrderItem 엔티티로 변환되어 결제 금액 계산에 사용된다.
     */
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    public static class OrderItemRequest {
        private Long productId;   // 주문할 상품 ID
        private int quantity;     // 수량 (1 이상이어야 하며 유효성 검증됨)
    }
}
