package com.onandhome.notification.dto;

import java.time.LocalDateTime; // 알림 엔티티

import com.onandhome.notification.entity.Notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDTO {

    // 알림 고유 ID
    private Long id;

    // 알림 제목 (예: "새 QnA가 등록되었습니다", "리뷰가 달렸습니다")
    private String title;

    // 알림 내용 (요약 설명)
    private String content;

    // 알림 타입 (예: QNA, REVIEW, NOTICE, ORDER 등)
    // 어떤 종류의 알림인지 구분하는데 사용
    private String type;

    // 어떤 객체와 연결된 알림인지 식별하는 값
    // 예: QnA ID, 리뷰 ID, 주문 ID 등
    private Long referenceId;

    // 읽음 여부 (true: 읽음 / false: 안 읽음)
    private Boolean isRead;

    // 알림이 생성된 시간
    private LocalDateTime createdAt;

    // 알림을 읽은 시간 (읽지 않았다면 null)
    private LocalDateTime readAt;

    // 상품 알림일 경우 상품 ID를 담기 위해 존재하는 필드
    // 예: 상품 QnA, 상품 리뷰 등
    private Long productId;


    /**
     *  Entity → DTO 변환
     * Notification 엔티티를 클라이언트에게 전달할 수 있는 DTO로 변환하는 메서드.
     * 실시간 알림(웹소켓)에서도 NotificationDTO 형태로 전달되고,
     * 일반 알림 목록 API에서도 NotificationDTO로 내려주기 때문에
     * 알림 시스템 전체에서 가장 많이 사용되는 핵심 변환 코드임.
     */
    public static NotificationDTO fromEntity(Notification notification) {
        return NotificationDTO.builder()
                .id(notification.getId())                // 알림 ID
                .title(notification.getTitle())          // 알림 제목
                .content(notification.getContent())      // 알림 내용
                .type(notification.getType())            // 알림 타입 (QNA 등)
                .referenceId(notification.getReferenceId()) // 관련된 엔티티 ID
                .productId(notification.getProductId())  // 관련 상품 ID (상품기반 알림일 경우)
                .isRead(notification.getIsRead())        // 읽음 여부
                .createdAt(notification.getCreatedAt())  // 생성 시간
                .readAt(notification.getReadAt())        // 읽은 시간
                .build();
    }
}

/*
요약
1. NotificationDTO는 알림을 화면으로 전달하기 위한 핵심 데이터 구조
2. 알림 내용(title, content), 타입(type), 연결된 ID(referenceId) 등이 들어 있음
3. 읽음 여부(isRead)와 읽은 시간(readAt)까지 포함돼 읽음 처리 UI에 사용됨
4. 실시간 알림(WebSocket)·일반 알림 REST API 둘 다 이 DTO를 사용
5. fromEntity()는 알림 Entity를 클라이언트로 보낼 수 있는 DTO로 변환하는 메서드
 */