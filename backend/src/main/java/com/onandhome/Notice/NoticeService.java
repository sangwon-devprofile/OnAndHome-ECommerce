package com.onandhome.Notice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.onandhome.Notice.dto.NoticeDto;
import com.onandhome.Notice.entity.Notice;
import com.onandhome.notification.NotificationService;
import com.onandhome.user.UserRepository;
import com.onandhome.user.entity.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Transactional
@Log4j2
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    /* 실시간 알림(WebSocket) 메시지 전송 도구 */
    private final SimpMessagingTemplate messagingTemplate;

    /** ✅ 전체 조회 (DTO 변환 포함) */
    public List<NoticeDto> findAll() {
        return noticeRepository.findAll()
                .stream()
                .map(NoticeDto::fromEntity)
                .collect(Collectors.toList());
    }

    /** ✅ 단일 조회 */
    public NoticeDto findById(Long id) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("공지사항이 존재하지 않습니다."));
        return NoticeDto.fromEntity(notice);
    }

    /** ✅ 새 공지 등록 (Controller → 여기로 호출됨) */
    public NoticeDto createNotice(NoticeDto dto) {
        if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("제목은 필수 입력 항목입니다.");
        }

        Notice notice = new Notice();
        notice.setTitle(dto.getTitle());
        notice.setWriter(dto.getWriter());
        notice.setContent(dto.getContent());
        notice.setCreatedAt(LocalDateTime.now());
        notice.setUpdatedAt(null);

        Notice savedNotice = noticeRepository.save(notice);

        // 모든 활성 사용자에게 알림 전송 (DB + WebSocket)
        try {
            List<User> activeUsers = userRepository.findAll().stream()
                    .filter(user -> user.getActive() != null && user.getActive())
                    .collect(Collectors.toList());

            log.info("공지사항 생성 - 총 {}명의 사용자에게 알림 전송 (DB + WebSocket)", activeUsers.size());

            /* -------------------------------------------
             * WebSocket으로 보낼 알림 payload 생성
             * (모든 사용자에게 동일한 내용)
             * ------------------------------------------- */
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "NOTICE");
            notification.put("noticeId", savedNotice.getId());
            notification.put("title", "📢 새로운 공지사항");
            notification.put("message", savedNotice.getTitle());
            notification.put("timestamp", LocalDateTime.now().toString());

            for (User user : activeUsers) {
                try {
                    // DB에 알림 저장 (일반 알림)
                    notificationService.createNotification(
                            user.getUserId(),
                            "📢 새로운 공지사항",
                            savedNotice.getTitle(),
                            "NOTICE",
                            savedNotice.getId(),
                            null
                    );

                    /* ----------------------------------------------------------
                     * 📡 WebSocket 실시간 알림 전송
                     * convertAndSendToUser(유저ID, 목적지, payload)
                     *
                     * 프론트는 "/user/{userId}/queue/notifications"
                     * → 이 경로를 구독하면 여기서 보내는 실시간 메시지를 받는다.
                     *
                     * 즉, 공지를 등록하면 모든 활성 사용자에게 즉시 알림이 뜬다.
                     * ---------------------------------------------------------- */
                    messagingTemplate.convertAndSendToUser(
                            user.getUserId(),          // 받는 사람 (User별 개별 전송)
                            "/queue/notifications",    // 구독 경로
                            notification               // 전송할 데이터(Payload)
                    );

                } catch (Exception e) {
                    log.error("사용자 {}에게 알림 전송 실패: {}", user.getUserId(), e.getMessage());
                }
            }

            log.info("공지사항 알림 전송 완료 (DB + WebSocket): noticeId={}", savedNotice.getId());
        } catch (Exception e) {
            log.error("공지사항 알림 전송 중 오류 발생", e);
            // 알림 실패해도 공지 저장은 정상 처리
        }

        return NoticeDto.fromEntity(savedNotice);
    }

    /** ✅ 수정 */
    public NoticeDto update(Long id, NoticeDto dto) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("공지사항이 존재하지 않습니다."));
        notice.setTitle(dto.getTitle());
        notice.setWriter(dto.getWriter());
        notice.setContent(dto.getContent());
        notice.setUpdatedAt(LocalDateTime.now());
        Notice updatedNotice = noticeRepository.save(notice);
        return NoticeDto.fromEntity(updatedNotice);
    }

    /** ✅ 삭제 (관련 알림도 함께 삭제) */
    public void delete(Long id) {
        // 공지사항 삭제 전에 관련 알림 먼저 삭제
        try {
            notificationService.deleteByTypeAndReferenceId("NOTICE", id);
            log.info("공지사항 {} 관련 알림 삭제 완료", id);
        } catch (Exception e) {
            log.error("공지사항 {} 관련 알림 삭제 실패", id, e);
        }

        noticeRepository.deleteById(id);
        log.info("공지사항 {} 삭제 완료", id);
    }

    /** ✅ 검색 (제목 또는 작성자로 검색) */
    public List<NoticeDto> search(String keyword) {
        return noticeRepository.findAll()
                .stream()
                .filter(notice ->
                        (notice.getTitle() != null && notice.getTitle().contains(keyword)) ||
                                (notice.getWriter() != null && notice.getWriter().contains(keyword))
                )
                .map(NoticeDto::fromEntity)
                .collect(Collectors.toList());
    }
}

/*
요약
1. 공지사항이 새로 등록되면 모든 활성 사용자(activeUsers) 목록을 조회
2. 각 사용자에게 DB 알림(createNotification)과 WebSocket 실시간 알림을 함께 전송
3. 실시간 알림은 convertAndSendToUser(userId, "/queue/notifications", payload) 방식으로 보냄
4. 사용자는 프론트에서 /user/{userId}/queue/notifications 경로를 구독해 받아봄
5. WebSocket 전송이 실패해도 공지 저장 기능은 정상적으로 작동
 */