package com.onandhome.notification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.onandhome.notification.dto.NotificationDTO;
import com.onandhome.notification.entity.Notification;
import com.onandhome.user.UserRepository;
import com.onandhome.user.entity.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional
public class NotificationService {

    /* 알림 데이터 저장소 (CRUD 처리) */
    private final NotificationRepository notificationRepository;

    /* 사용자 조회용 저장소 */
    private final UserRepository userRepository;


    /* 알림 생성 메서드
       특정 사용자에게 한 건의 알림을 생성하여 DB에 저장한다.
       실시간 알림을 사용한다면 이 단계 이후에 웹소켓 알림을 전송함. */
    public void createNotification(String userId, String title, String content, String type, Long referenceId, Long productId) {

        /* userId 기준으로 사용자 조회 */
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        /* 관리자(role = 0)는 특정 타입만 받도록 제한 */
        if (user.getRole() != null && user.getRole() == 0) {
            if (!"ORDER".equals(type) && !"QNA".equals(type) && !"REVIEW".equals(type)) {
                log.info("관리자 알림 차단: userId={}, type={}", userId, type);
                return;
            }
        }

        /* 광고 알림은 마케팅 동의 여부에 따라 전달 여부 결정 */
        if ("MARKETING".equals(type) || "ADVERTISEMENT".equals(type)) {
            if (!Boolean.TRUE.equals(user.getMarketingConsent())) {
                log.info("광고 알림 차단: userId={} (마케팅 동의 안함)", userId);
                return;
            }
        }

        /* 알림 엔티티 생성 */
        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .content(content)
                .type(type)
                .referenceId(referenceId)
                .productId(productId)
                .isRead(false)
                .build();

        /* 저장 */
        notificationRepository.save(notification);

        log.info("알림 생성 완료: userId={}, type={}", userId, type);
    }


    /* 특정 사용자의 모든 알림 목록 조회 (최신순) */
    @Transactional(readOnly = true)
    public List<NotificationDTO> getUserNotifications(String userId) {

        /* 사용자 조회 */
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        /* 해당 사용자 알림 목록 조회 후 DTO로 변환 */
        return notificationRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(NotificationDTO::fromEntity)
                .collect(Collectors.toList());
    }


    /* 특정 사용자의 읽지 않은 알림 개수 조회 */
    @Transactional(readOnly = true)
    public long getUnreadCount(String userId) {

        /* 사용자 조회 */
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        /* 읽지 않은 알림 개수 조회 */
        return notificationRepository.countByUserAndIsReadFalse(user);
    }


    /* 단일 알림을 읽음 처리하는 메서드 */
    public void markAsRead(Long notificationId) {

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("알림을 찾을 수 없습니다."));

        /* 읽음 처리 */
        notification.setIsRead(true);
        notification.setReadAt(LocalDateTime.now());

        notificationRepository.save(notification);
    }


    /* 사용자의 모든 알림을 읽음 처리한다 */
    public void markAllAsRead(String userId) {

        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        /* 읽지 않은 알림만 조회 */
        List<Notification> unreadNotifications = notificationRepository
                .findByUserAndIsReadFalseOrderByCreatedAtDesc(user);

        /* 읽음 처리 시간 통일 */
        LocalDateTime now = LocalDateTime.now();

        unreadNotifications.forEach(notification -> {
            notification.setIsRead(true);
            notification.setReadAt(now);
        });

        /* 일괄 저장 */
        notificationRepository.saveAll(unreadNotifications);
    }


    /* 특정 알림 삭제 */
    public void deleteNotification(Long notificationId) {
        notificationRepository.deleteById(notificationId);
    }


    /* 관리자 또는 시스템에서 대량 알림을 생성하는 메서드
       type에 따라 알림을 받을 사용자 그룹이 달라진다. */
    public int createBulkNotification(String title, String content, String type, Long referenceId) {

        List<User> users;

        /* 광고 알림 → 마케팅 동의 사용자만 */
        if ("MARKETING".equals(type) || "ADVERTISEMENT".equals(type)) {
            users = userRepository.findByMarketingConsentTrueAndRoleNot(0);
            log.info("광고 알림 대량 전송: 대상 {}명", users.size());

            /* 주문, QnA, 리뷰 알림 → 관리자 포함 전체 사용자 */
        } else if ("ORDER".equals(type) || "QNA".equals(type) || "REVIEW".equals(type)) {
            users = userRepository.findByActiveTrue();
            log.info("일반 알림 대량 전송 (관리자 포함): 대상 {}명", users.size());

            /* 기타 알림 → 일반 사용자만 (관리자 제외) */
        } else {
            users = userRepository.findByActiveTrueAndRoleNot(0);
            log.info("일반 알림 대량 전송 (관리자 제외): 대상 {}명", users.size());
        }

        /* 각 사용자에게 알림 엔티티 생성 */
        List<Notification> notifications = users.stream()
                .map(user -> Notification.builder()
                        .user(user)
                        .title(title)
                        .content(content)
                        .type(type)
                        .referenceId(referenceId)
                        .isRead(false)
                        .build())
                .collect(Collectors.toList());

        /* 일괄 저장 */
        notificationRepository.saveAll(notifications);

        log.info("대량 알림 생성 완료: type={}, count={}", type, notifications.size());

        return notifications.size();
    }


    /* 관리자에게만 알림을 보내는 메서드
       QnA 답변, 리뷰 신고, 주문 처리 등 관리자 대상 알림에 사용됨 */
    public void createAdminNotification(String title, String content, String type, Long referenceId) {

        /* 관리자(role = 0) 전체 조회 */
        List<User> admins = userRepository.findByRole(0);

        if (admins.isEmpty()) {
            log.warn("관리자가 없어 알림을 전송할 수 없습니다.");
            return;
        }

        /* 관리자 각각에 대해 알림 생성 */
        List<Notification> notifications = admins.stream()
                .map(admin -> Notification.builder()
                        .user(admin)
                        .title(title)
                        .content(content)
                        .type(type)
                        .referenceId(referenceId)
                        .isRead(false)
                        .build())
                .collect(Collectors.toList());

        notificationRepository.saveAll(notifications);

        log.info("관리자 알림 생성 완료: type={}, 관리자 수={}", type, admins.size());
    }


    /* 특정 조건(type + referenceId)에 해당하는 알림 삭제
       예: QnA가 삭제되면 해당 QnA의 알림도 함께 삭제 */
    public void deleteByTypeAndReferenceId(String type, Long referenceId) {
        try {
            notificationRepository.deleteByTypeAndReferenceId(type, referenceId);
            log.info("알림 삭제 완료: type={}, referenceId={}", type, referenceId);
        } catch (Exception e) {
            log.error("알림 삭제 실패: type={}, referenceId={}", type, referenceId, e);
        }
    }
}

/*
요약
1. NotificationService는 알림 생성, 조회, 읽음 처리, 삭제를 모두 담당하는 핵심 비즈니스 로직
2. 마케팅 동의 여부, 관리자 여부에 따라 알림 필터링을 수행
3. createNotification은 단일 알림 생성, createBulkNotification은 대량 생성 기능을 담당
4. markAsRead / markAllAsRead로 읽음 상태를 변경하여 UI와 연동됨
5. deleteByTypeAndReferenceId는 특정 엔티티 삭제 시 해당 알림을 함께 제거하는 데 사용
 */