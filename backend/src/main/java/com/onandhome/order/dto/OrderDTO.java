package com.onandhome.order.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.onandhome.order.entity.Order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderDTO {

    private Long id;                   // 주문 고유 ID
    private Long orderId;              // id와 동일한 별칭 (프론트 호환성 유지 목적)
    private String orderNumber;        // 화면 주문 목록·상세 조회 시 표시되는 주문번호

    private Long userIdLong;           // User 엔티티의 기본키 (DB 기준 ID)
    private String userId;             // 사용자의 로그인 아이디
    private String userName;           // 사용자의 이름
    private String username;           // 동일 기능 별칭 (프론트에서 중복된 호칭으로 쓰여 호환 유지)
    private String userEmail;          // 사용자 이메일
    private String userPhone;          // 사용자 연락처
    private String userAddress;        // 사용자 주소 (회원 정보 기준)

    private String status;             // 주문 상태 (ORDERED, PAYMENT_PENDING, DELIVERING 등)
    // 프론트에서 주문 상태 표시용으로 사용됨

    private int totalPrice;            // 주문 전체 금액
    // 주문 목록 정렬 및 결제 버튼 표시 조건 등에 사용됨

    private List<OrderItemDTO> orderItems;  // 주문 항목 목록
    // 상세 주문 화면에서 상품 정보 렌더링 시 사용됨

    private LocalDateTime createdAt;   // 주문일시
    // 마이페이지 주문 내역 정렬 기준

    private LocalDateTime paidAt;      // 결제 완료 시점 (미결제 주문은 null)
    // 결제 여부 체크용 핵심 필드

    // 배송지 정보 (주문 생성 시 저장됨)
    private String recipientName;
    private String recipientPhone;
    private String shippingAddress;
    private String shippingRequest;

    // 프론트 호환성 유지용 별칭 (기존 컴포넌트들이 이 필드명을 사용함)
    private String address;            // shippingAddress의 별칭
    private String deliveryMessage;    // shippingRequest의 별칭
    private String phone;              // userPhone의 별칭
    private String email;              // userEmail의 별칭

    // 엔티티 -> DTO 변환 메서드
    // OrderService의 조회 기능에서 공통적으로 사용되며,
    // 주문 상세 조회, 사용자 주문 목록, 관리자 주문 목록 등 대부분의 API에서 반환된다.
    public static OrderDTO fromEntity(Order order) {
        List<OrderItemDTO> orderItemDTOs = order.getOrderItems().stream()
                .map(OrderItemDTO::fromEntity)
                .collect(Collectors.toList());

        return OrderDTO.builder()
                .id(order.getId())
                .orderId(order.getId())                                    // 동일 값으로 세팅
                .orderNumber(order.getOrderNumber())
                .userIdLong(order.getUser().getId())                       // DB PK
                .userId(order.getUser().getUserId())                       // 로그인 ID
                .userName(order.getUser().getUsername())
                .username(order.getUser().getUsername())                  // 별칭
                .userEmail(order.getUser().getEmail())
                .userPhone(order.getUser().getPhone())
                .userAddress(order.getUser().getAddress())
                .status(order.getStatus().toString())                      // Enum → 문자열
                .totalPrice(order.getTotalPrice())
                .orderItems(orderItemDTOs)
                .createdAt(order.getCreatedAt())
                .paidAt(order.getPaidAt())
                .recipientName(order.getRecipientName())
                .recipientPhone(order.getRecipientPhone())
                .shippingAddress(order.getShippingAddress())
                .shippingRequest(order.getShippingRequest())
                // 별칭 필드 세팅 (프론트에서 legacy 코드 때문에 필요)
                .address(order.getShippingAddress())
                .deliveryMessage(order.getShippingRequest())
                .phone(order.getUser().getPhone())
                .email(order.getUser().getEmail())
                .build();
    }
}
