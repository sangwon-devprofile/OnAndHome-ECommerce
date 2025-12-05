package com.onandhome.qna;

import com.onandhome.notification.NotificationService;
import com.onandhome.qna.entity.Qna;
import com.onandhome.qna.entity.QnaReply;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/* QnA 답변 관련 서비스 */
@Service
@RequiredArgsConstructor
@Slf4j
public class QnaReplyService {

    private final QnaRepository qnaRepository;
    private final QnaReplyRepository qnaReplyRepository;

    /* 일반 알림(DB 저장용) 서비스 */
    private final NotificationService notificationService;

    /* 실시간 알림(WebSocket) 메시지 전송 도구 */
    private final SimpMessagingTemplate messagingTemplate;

    /* 전체 리플라이 조회 */
    public List<QnaReply> findAll() {
        return qnaReplyRepository.findAll();
    }

    /* ID로 리플라이 조회 */
    public QnaReply findById(Long id) {
        return qnaReplyRepository.findById(id).orElse(null);
    }

    /* 새 리플라이 등록
       - DB 저장 알림(NotificationService)
       - WebSocket 실시간 알림(SimpMessagingTemplate)
       이 두 가지 기능이 함께 실행된다 */
    @Transactional
    public QnaReply save(QnaReply reply) {

        /* 생성 시간 기록 */
        reply.setCreatedAt(LocalDateTime.now());

        /* DB 저장 */
        QnaReply savedReply = qnaReplyRepository.save(reply);

        /* QnA 정보 확인 */
        Qna qna = reply.getQna();
        if (qna == null) {
            log.warn("QnA 정보가 없어 알림 전송 불가");
            return savedReply;
        }

        Long qnaId = qna.getId();

        /* 연결된 상품이 있다면 productId 추출 */
        Long productId = null;
        if (qna.getProduct() != null) {
            productId = qna.getProduct().getId();
        }

        /* QnA 작성자 userId */
        String writer = qna.getWriter();

        /* 답변 작성자 userId */
        String responder = reply.getResponder();

        log.info("QnA 답변 알림 처리 시작");

        try {
            /* 작성자가 존재할 경우에만 알림 처리 */
            if (writer != null && !writer.trim().isEmpty()) {

                /* 자기 자신의 글에 답변하는 경우 알림 제외 */
                boolean isSelfReply = responder != null && responder.equals(writer);

                if (!isSelfReply) {

                    /* 1. 일반 알림(DB 저장) */
                    notificationService.createNotification(
                            writer,
                            "Q&A 답변 등록",
                            "문의하신 내용에 답변이 등록되었습니다.",
                            "QNA_REPLY",
                            qnaId,
                            productId
                    );

                    /* 2. 실시간 WebSocket 알림 전송 */
                    /* 프론트는 "/user/{userId}/queue/notifications" 경로를 구독해야 한다 */
                    Map<String, Object> notification = new HashMap<>();
                    notification.put("type", "QNA_REPLY");
                    notification.put("qnaId", qna.getId());
                    notification.put("productId", productId);
                    notification.put("title", "Q&A 답변 알림");
                    notification.put("message", "작성하신 문의에 답변이 등록되었습니다.");
                    notification.put("timestamp", LocalDateTime.now().toString());

                    /* convertAndSendToUser(대상 userId, 목적지, 전송할 데이터) */
                    messagingTemplate.convertAndSendToUser(
                            writer,
                            "/queue/notifications",
                            notification
                    );

                    log.info("QnA 답변 알림 전송 완료 (DB + WebSocket): userId={}, qnaId={}", writer, qnaId);

                } else {
                    log.info("자기 자신의 QnA에 대한 답변이므로 알림 제외");
                }
            }
        } catch (Exception e) {
            log.error("알림 전송 중 오류 발생", e);
        }

        return savedReply;
    }

    /* 관리자 컨트롤러에서 사용하는 리플라이 생성 */
    @Transactional
    public QnaReply createReply(Long qnaId, String content, String responder) {

        /* QnA와 Product를 함께 조회 */
        Qna qna = qnaRepository.findByIdWithProduct(qnaId)
                .orElseThrow(() -> new IllegalArgumentException("QnA를 찾을 수 없습니다."));

        QnaReply reply = new QnaReply();
        reply.setQna(qna);
        reply.setContent(content);
        reply.setResponder(responder);
        reply.setCreatedAt(LocalDateTime.now());

        /* save 메서드 내부에서 알림 처리도 함께 수행된다 */
        return save(reply);
    }

    /* 리플라이 수정 */
    @Transactional
    public QnaReply update(Long id, QnaReply updated) {
        QnaReply reply = qnaReplyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 리플라이가 존재하지 않습니다."));
        reply.setContent(updated.getContent());
        reply.setResponder(updated.getResponder());
        return qnaReplyRepository.save(reply);
    }

    /* 관리자 리플라이 수정 */
    @Transactional
    public QnaReply updateReply(Long replyId, String content) {
        QnaReply reply = qnaReplyRepository.findById(replyId)
                .orElseThrow(() -> new IllegalArgumentException("답변을 찾을 수 없습니다."));
        reply.setContent(content);
        return qnaReplyRepository.save(reply);
    }

    /* 리플라이 삭제 */
    @Transactional
    public void delete(Long id) {
        qnaReplyRepository.deleteById(id);
    }

    /* 관리자 리플라이 삭제 */
    @Transactional
    public void deleteReply(Long replyId) {
        qnaReplyRepository.deleteById(replyId);
    }

    /* 특정 QnA의 모든 리플라이 조회 */
    public List<QnaReply> findByQnaId(Long qnaId) {
        Qna qna = qnaRepository.findById(qnaId)
                .orElseThrow(() -> new IllegalArgumentException("QnA를 찾을 수 없습니다."));
        return qnaReplyRepository.findByQnaOrderByCreatedAtAsc(qna);
    }
}

/*
요약
1. 이 서비스는 QnA 답변 생성 시 일반 알림 저장 + WebSocket 실시간 알림을 동시에 처리
2. 실시간 알림은 SimpMessagingTemplate.convertAndSendToUser()로 전송
3. 프론트는 /user/{userId}/queue/notifications 를 구독해야 실시간 알림 수신 가능
4. 자기 자신이 작성한 QnA에 답변하는 경우 알림은 제외
 */