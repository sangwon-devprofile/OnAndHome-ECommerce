package com.onandhome.notification.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.onandhome.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/* 알림 정보를 저장하는 엔티티 (모든 알림의 기본 구조) */
@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class) // createdAt 자동 기록
public class Notification {

    /* 알림 고유 ID (Primary Key) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* 알림을 받는 사용자 (여러 알림이 한 사용자에게 귀속됨) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /* 알림 제목 */
    @Column(nullable = false)
    private String title;

    /* 알림 내용 (최대 1000자까지 저장) */
    @Column(nullable = false, length = 1000)
    private String content;

    /* 특정 상품과 연결된 알림일 경우 사용 (리뷰, QnA 등에서 활용) */
    @Column(name = "product_id")
    private Long productId;

    /* 알림 종류 구분
       예: ORDER, REVIEW, QNA, QNA_REPLY, NOTICE, ADMIN_ORDER 등 */
    @Column(nullable = false)
    private String type;

    /* 관련 엔티티의 고유 ID (상세 페이지 이동 등에 사용)
       예:
         QNA 알림 → QnA ID
         리뷰 알림 → Review ID
         공지 알림 → Notice ID */
    @Column
    private Long referenceId;

    /* 읽음 여부 (기본 false → 안 읽음) */
    @Column(nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    /* 알림 생성 시각 (자동 기록) */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /* 읽은 시각 (읽지 않은 경우 null) */
    @Column
    private LocalDateTime readAt;
}

/*
요약 (5줄)
1. Notification 엔티티는 모든 알림의 공통 데이터 구조.
2. user, type, referenceId, productId로 알림의 목적과 대상을 정확히 지정.
3. 읽음 상태(isRead)와 readAt을 통해 알림 UI 구현이 쉬움.
4. createdAt은 Auditing으로 자동 기록되므로 따로 처리할 필요 없음.
5. 이 엔티티는 DB 기반 알림과 WebSocket 실시간 알림 둘 다의 핵심 데이터 모델.
*/
