package com.onandhome.review;

import com.onandhome.notification.NotificationService;
import com.onandhome.review.dto.ReviewReplyDTO;
import com.onandhome.review.entity.Review;
import com.onandhome.review.entity.ReviewReply;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/* 리뷰 답글 서비스 */
@Service
@RequiredArgsConstructor
@Transactional
@Log4j2
public class ReviewReplyService {

    private final ReviewReplyRepository reviewReplyRepository;
    private final ReviewRepository reviewRepository;

    /* DB 저장용 일반 알림 서비스 */
    private final NotificationService notificationService;

    /* 실시간 알림(WebSocket) 전송 도구 */
    private final SimpMessagingTemplate messagingTemplate;

    /* 리뷰 ID 기준으로 모든 답글 조회 */
    @Transactional(readOnly = true)
    public List<ReviewReplyDTO> findByReviewId(Long reviewId) {
        return reviewReplyRepository.findByReviewId(reviewId)
                .stream()
                .map(ReviewReplyDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /* 답글 단일 조회 */
    @Transactional(readOnly = true)
    public ReviewReplyDTO findById(Long replyId) {
        ReviewReply reply = reviewReplyRepository.findById(replyId)
                .orElseThrow(() -> new IllegalArgumentException("해당 답글이 존재하지 않습니다. ID=" + replyId));
        return ReviewReplyDTO.fromEntity(reply);
    }

    /* 리뷰 답글 생성 (관리자/사용자 공통)
       - DB 저장 알림 생성 (리뷰 작성자에게)
       - WebSocket 실시간 알림 전송 (리뷰 작성자 실시간 알림)
       두 기능이 동시에 수행된다 */
    public void createReply(Long reviewId, String content, String author, String username, Long userId) {

        /* 답글을 달 리뷰 조회 */
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다. ID=" + reviewId));

        /* 답글 엔티티 생성 */
        ReviewReply reply = new ReviewReply();
        reply.setReview(review);
        reply.setContent(content);
        reply.setAuthor(author);
        reply.setUsername(username);

        /* 관리자 계정이면 userId를 null 처리하여 외래키 문제 방지 */
        if (userId != null && userId > 0) {
            reply.setUserId(userId);
        } else {
            reply.setUserId(null);
        }

        reply.setCreatedAt(LocalDateTime.now());
        reply.setUpdatedAt(LocalDateTime.now());

        /* DB 저장 */
        reviewReplyRepository.save(reply);

        /* 리뷰 작성자 알림 전송 */
        try {
            /* 리뷰 작성자 userId */
            if (review.getUser() != null && review.getUser().getUserId() != null) {
                String reviewAuthorId = review.getUser().getUserId();

                /* 자기 자신이 자기 리뷰에 답글 단 경우 알림 제외 */
                boolean isSelfReply = username != null && username.equals(reviewAuthorId);

                if (!isSelfReply) {
                    /* 1차: DB 기반 일반 알림 저장 */
                    notificationService.createNotification(
                            reviewAuthorId,
                            "리뷰 답글 등록",
                            "작성하신 리뷰에 답글이 등록되었습니다.",
                            "REVIEW_REPLY",
                            reviewId,
                            null
                    );

                    /* 2차: WebSocket 실시간 알림 전송
                           프론트는 '/user/{userId}/queue/notifications'를 구독해야 수신할 수 있음 */
                    Map<String, Object> notification = new HashMap<>();
                    notification.put("type", "REVIEW_REPLY");
                    notification.put("reviewId", reviewId);
                    notification.put("title", "리뷰 답글 등록");
                    notification.put("message", "작성하신 리뷰에 답글이 등록되었습니다.");
                    notification.put("timestamp", LocalDateTime.now().toString());

                    messagingTemplate.convertAndSendToUser(
                            reviewAuthorId,
                            "/queue/notifications",
                            notification
                    );
                }
            }
        } catch (Exception e) {
            /* 알림 실패해도 답글 등록은 성공 처리 */
            log.error("리뷰 답글 알림 전송 실패: {}", e.getMessage(), e);
        }
    }

    /* 답글 수정 */
    public void updateReply(Long replyId, String content) {
        ReviewReply reply = reviewReplyRepository.findById(replyId)
                .orElseThrow(() -> new IllegalArgumentException("해당 답글이 존재하지 않습니다. ID=" + replyId));

        reply.setContent(content);
        reply.setUpdatedAt(LocalDateTime.now());
        reviewReplyRepository.save(reply);
    }

    /* 답글 삭제 */
    public void deleteReply(Long replyId) {
        ReviewReply reply = reviewReplyRepository.findById(replyId)
                .orElseThrow(() -> new IllegalArgumentException("해당 답글이 존재하지 않습니다. ID=" + replyId));

        reviewReplyRepository.delete(reply);
    }
}

/*
요약
1. ReviewReplyService는 리뷰 답글 생성 시 DB 기반 일반 알림과 WebSocket 실시간 알림을 리뷰 작성자에게 전송
2. 알림 목적지는 사용자별 /user/{userId}/queue/notifications 구독 경로
3. 자기 자신이 자기 리뷰에 단 답글은 알림이 전송되지 않음
4. 관리자 계정은 userId를 null로 처리해 외래키 제약을 회피 (리뷰를 작성한 사용자 계정이 없어도 답글 달 수 있도록)
5. 답글 조회·수정·삭제 기능은 순수 CRUD로, 알림 로직과 분리되어 동작
 */