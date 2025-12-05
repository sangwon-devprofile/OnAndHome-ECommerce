package com.onandhome.qna;

import com.onandhome.notification.NotificationService;
import com.onandhome.qna.entity.Qna;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class QnaService {

    private final QnaRepository qnaRepository;

    /* DB 저장용 일반 알림 서비스 */
    private final NotificationService notificationService;

    /* 실시간 알림(WebSocket) 전송 도구 */
    private final SimpMessagingTemplate messagingTemplate;

    /* 전체 QnA 조회 */
    public List<Qna> findAll() {
        return qnaRepository.findAll();
    }

    /* ID로 단일 QnA 조회 */
    public Qna findById(Long id) {
        return qnaRepository.findById(id).orElse(null);
    }

    /* 새 QnA 등록
       - DB 기반 일반 알림 생성 (관리자 대상)
       - WebSocket 실시간 알림 전송 (관리자 대시보드)
       두 기능이 함께 수행된다 */
    public Qna save(Qna qna) {

        /* 생성 시간 기록 */
        qna.setCreatedAt(LocalDateTime.now());

        /* QnA 저장 */
        Qna savedQna = qnaRepository.save(qna);

        /* 관리자에게 알림 전송 */
        try {
            /* 상품명 존재 시 포함 */
            String productName = "";
            if (qna.getProduct() != null && qna.getProduct().getName() != null) {
                productName = qna.getProduct().getName();
            }

            /* 알림 내용 생성 */
            String notificationContent = String.format(
                    "%s님이 새로운 문의를 등록했습니다.%s",
                    qna.getWriter(),
                    productName.isEmpty() ? "" : " (상품: " + productName + ")"
            );

            /* 1. DB에 관리자 알림 저장 */
            notificationService.createAdminNotification(
                    "새 Q&A 등록",
                    notificationContent,
                    "ADMIN_QNA",
                    savedQna.getId()
            );

            /* 2. WebSocket으로 관리자 실시간 알림 전송
                 관리자는 '/topic/admin-notifications' 경로를 구독해야 한다 */
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "ADMIN_QNA");
            notification.put("qnaId", savedQna.getId());
            notification.put("title", "새 Q&A 등록");
            notification.put("message", notificationContent);
            notification.put("timestamp", LocalDateTime.now().toString());

            /* 모든 관리자에게 동일한 메시지 전송 */
            messagingTemplate.convertAndSend("/topic/admin-notifications", notification);

            log.info("관리자 QnA 등록 알림 전송 완료 (DB + WebSocket): qnaId={}", savedQna.getId());

        } catch (Exception e) {
            /* 알림 실패해도 QnA 저장은 문제 없이 진행 */
            log.error("관리자 QnA 등록 알림 전송 실패: {}", e.getMessage(), e);
        }

        return savedQna;
    }

    /* QnA 수정 (답변은 별도 로직) */
    public Qna update(Long id, Qna updated) {
        Qna qna = qnaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 질문이 존재하지 않습니다. id=" + id));

        qna.setTitle(updated.getTitle());
        qna.setWriter(updated.getWriter());
        qna.setQuestion(updated.getQuestion());

        return qnaRepository.save(qna);
    }

    /* QnA 삭제 시 관련 알림도 함께 삭제 */
    public void delete(Long id) {

        /* QnA 관련 일반 알림 및 답변 알림 삭제 */
        try {
            notificationService.deleteByTypeAndReferenceId("QNA", id);
            notificationService.deleteByTypeAndReferenceId("QNA_REPLY", id);
            log.info("QnA {} 관련 알림 삭제 완료", id);
        } catch (Exception e) {
            log.error("QnA {} 관련 알림 삭제 실패", id, e);
        }

        /* QnA 삭제 */
        qnaRepository.deleteById(id);
        log.info("QnA {} 삭제 완료", id);
    }

    /* 제목 또는 작성자로 검색 */
    public List<Qna> search(String keyword) {
        return qnaRepository.findAll()
                .stream()
                .filter(qna ->
                        (qna.getTitle() != null && qna.getTitle().contains(keyword)) ||
                                (qna.getWriter() != null && qna.getWriter().contains(keyword))
                )
                .collect(Collectors.toList());
    }

    /* 상품명으로 검색 (사용자용) */
    public List<Qna> searchByProductName(String keyword) {
        return qnaRepository.findAll()
                .stream()
                .filter(qna ->
                        qna.getProduct() != null &&
                                qna.getProduct().getName() != null &&
                                qna.getProduct().getName().contains(keyword)
                )
                .collect(Collectors.toList());
    }
}

/*
요약
1. QnaService는 QnA 등록 시 관리자에게 일반 알림(DB 저장) 과 실시간 알림(WebSocket) 을 함께 전송
2. 관리자 실시간 알림 채널은 /topic/admin-notifications 이며, 모든 관리자가 동일한 메시지를 수신
3. 알림 내용에는 QnA 작성자, 상품명(존재할 경우)이 포함
4. QnA 삭제 시 연결된 모든 알림(QNA, QNA_REPLY)도 함께 삭제
5. 검색 기능은 제목·작성자·상품명 기준으로 필터링하며, 조회 결과는 Entity 리스트 그대로 반환
 */