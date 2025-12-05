package com.onandhome.review;

import com.onandhome.admin.adminProduct.ProductRepository;
import com.onandhome.admin.adminProduct.entity.Product;
import com.onandhome.notification.NotificationService;
import com.onandhome.review.dto.ReviewDTO;
import com.onandhome.review.dto.ReviewLikeResponseDTO;
import com.onandhome.review.entity.Review;
import com.onandhome.user.UserRepository;
import com.onandhome.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.onandhome.review.entity.ReviewLike;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.stream.Collectors;

/*
 * 리뷰 서비스
 * - 리뷰 조회, 생성, 수정, 삭제 처리
 * - 리뷰 등록 시 관리자에게 알림 전송 (DB 저장 + WebSocket 실시간 전송)
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    /* 일반 알림(DB 저장)을 처리하는 서비스 */
    private final NotificationService notificationService;
    private final ReviewLikeRepository reviewLikeRepository;

    /* 실시간 알림(WebSocket)을 전송하기 위한 템플릿 */
    private final SimpMessagingTemplate messagingTemplate;

    /* 모든 리뷰 조회 (DTO 변환 포함) */
    @Transactional(readOnly = true)
    public List<ReviewDTO> findAll() {
        List<Review> reviews = reviewRepository.findAll();

        /* Lazy 로딩된 값 강제 초기화 */
        reviews.forEach(r -> {
            r.getAuthor();
            r.getProductName();
            r.getContent();
            r.getCreatedAt();
        });

        return reviews.stream()
                .map(ReviewDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /* ID로 개별 리뷰 조회 */
    @Transactional(readOnly = true)
    public ReviewDTO findById(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다. id=" + id));

        /* Lazy 초기화 */
        review.getAuthor();
        review.getProductName();
        review.getContent();
        review.getCreatedAt();

        return ReviewDTO.fromEntity(review);
    }

    /* 리뷰 삭제 (관련 알림도 함께 삭제) */
    @Transactional
    public void deleteById(Long id) {
        if (!reviewRepository.existsById(id)) {
            throw new IllegalArgumentException("리뷰가 존재하지 않습니다. id=" + id);
        }

        /* 리뷰 관련 알림 삭제 */
        try {
            notificationService.deleteByTypeAndReferenceId("REVIEW", id);
            notificationService.deleteByTypeAndReferenceId("REVIEW_REPLY", id);
            log.info("리뷰 {} 관련 알림 삭제 완료", id);
        } catch (Exception e) {
            log.error("리뷰 {} 관련 알림 삭제 실패", id, e);
        }

        reviewRepository.deleteById(id);
        log.info("리뷰 {} 삭제 완료", id);
    }

    /* 단순 이름만 다르게 한 삭제 */
    @Transactional
    public void deleteReview(Long id) {
        deleteById(id);
    }

    /* 상품별 리뷰 목록 조회 */
    @Transactional(readOnly = true)
    public List<ReviewDTO> findByProductId(Long productId) {
        List<Review> reviews = reviewRepository.findByProductIdOrderByCreatedAtDesc(productId);

        /* Lazy 데이터 초기화 (답글 포함) */
        reviews.forEach(r -> {
            r.getAuthor();
            r.getProductName();
            r.getContent();
            r.getCreatedAt();

            if (r.getReplies() != null) {
                r.getReplies().size(); // Lazy 초기화
            }
        });

        return reviews.stream()
                .map(ReviewDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /* 리뷰 작성 (관리자에게 알림 전송 - DB + WebSocket) */
    @Transactional
    public ReviewDTO createReview(Long productId, Long userId, String content, int rating) {

        /* 상품 조회 */
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

        /* 사용자 조회 */
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        /* 리뷰 생성 */
        Review review = new Review();
        review.setProduct(product);
        review.setUser(user);
        review.setContent(content);
        review.setRating(rating);

        /* 작성자 정보 저장 (username이 있으면 사용, 없으면 userId 사용) */
        review.setAuthor(user.getUsername() != null ? user.getUsername() : user.getUserId());
        review.setUsername(user.getUserId());

        /* 캐시 문제 방지용 상품명 저장 */
        review.setProductName(product.getName());
        review.setCreatedAt(LocalDateTime.now());

        Review savedReview = reviewRepository.save(review);

        /* 리뷰 등록 시 관리자에게 알림 전송 */
        try {
            String notificationContent = String.format(
                    "%s 님이 새로운 리뷰를 작성했습니다. (상품: %s)",
                    user.getUserId(),
                    product.getName()
            );

            /* 1) DB에 관리자 알림 저장 */
            notificationService.createAdminNotification(
                    "새 리뷰 등록",
                    notificationContent,
                    "ADMIN_REVIEW",
                    savedReview.getId()
            );

            /* 2) WebSocket으로 관리자에게 실시간 알림 전송 */
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "ADMIN_REVIEW");
            notification.put("reviewId", savedReview.getId());
            notification.put("title", "새 리뷰 등록");
            notification.put("message", notificationContent);
            notification.put("timestamp", LocalDateTime.now().toString());

            /*
             * "/topic/admin-notifications"를 구독 중인 모든 관리자 클라이언트에게 실시간 전송
             */
            messagingTemplate.convertAndSend("/topic/admin-notifications", notification);

            log.info("관리자 리뷰 등록 알림 전송 성공 (DB + WebSocket): reviewId={}", savedReview.getId());
        } catch (Exception e) {
            log.error("관리자 리뷰 등록 알림 전송 실패: {}", e.getMessage(), e);
            /* 알림 전송 실패해도 리뷰 작성 자체는 정상 처리됨 */
        }

        reviewRepository.flush();
        return ReviewDTO.fromEntity(savedReview);
    }

    /* 리뷰 수정 */
    @Transactional
    public ReviewDTO updateReview(Long id, String content, int rating) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다. id=" + id));
        
        review.setContent(content);
        review.setRating(rating);
        
        Review updatedReview = reviewRepository.save(review);
        return ReviewDTO.fromEntity(updatedReview);
    }

    /* 상품명 또는 작성자로 검색 */
    @Transactional(readOnly = true)
    public List<ReviewDTO> search(String keyword) {
        List<Review> reviews = reviewRepository.findAll();
        
        // ✅ 캐시 초기화
        reviews.forEach(r -> {
            r.getAuthor();
            r.getProductName();
            r.getContent();
            r.getCreatedAt();
        });

        return reviews.stream()
                .filter(review ->
                        (review.getProductName() != null && review.getProductName().contains(keyword)) ||
                                (review.getAuthor() != null && review.getAuthor().contains(keyword))
                )
                .map(ReviewDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /** ✅ 상품명으로만 검색 (사용자용) */
    @Transactional(readOnly = true)
    public List<ReviewDTO> searchByProductName(String keyword) {
        List<Review> reviews = reviewRepository.findAll();
        
        // ✅ 캐시 초기화
        reviews.forEach(r -> {
            r.getAuthor();
            r.getProductName();
            r.getContent();
            r.getCreatedAt();
        });

        return reviews.stream()
                .filter(review ->
                        review.getProductName() != null && review.getProductName().contains(keyword)
                )
                .map(ReviewDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /** ✅ 최근 리뷰 목록 조회 (Footer용) */
    @Transactional(readOnly = true)
    public List<ReviewDTO> findRecent(int limit) {
        List<Review> reviews = reviewRepository.findTop100ByOrderByCreatedAtDesc();
        
        // ✅ 캐시 초기화
        reviews.forEach(r -> {
            r.getAuthor();
            r.getProductName();
            r.getContent();
            r.getCreatedAt();
        });

        return reviews.stream()
                .limit(limit)
                .map(ReviewDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReviewDTO> findByProductIdWithLikes(Long productId, Long userId) {
        List<Review> reviews = reviewRepository.findByProductIdOrderByCreatedAtDesc(productId);

        reviews.forEach(r -> {
            r.getAuthor();
            r.getProductName();
            r.getContent();
            r.getCreatedAt();
            if (r.getReplies() != null) {
                r.getReplies().size();
            }
        });
        // DTO 변환 및 좋아요 정보 추가
        return reviews.stream()
                .map(review -> {
                    ReviewDTO dto = ReviewDTO.fromEntity(review);

                    // 현재 유저가 좋아요 했는지 설정
                    if (userId != null) {
                        boolean isLiked = reviewLikeRepository.existsByReviewIdAndUserId(review.getId(), userId);
                        dto.setIsLiked(isLiked);
                    } else {
                        dto.setIsLiked(false);
                    }

                    return dto;
                })
                .collect(Collectors.toList());
    }
    /**
     * 리뷰 좋아요 토글
     */
    @Transactional
    public ReviewLikeResponseDTO toggleLike(Long reviewId, Long userId) {
        // 리뷰 존재 확인
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다. id=" + reviewId));

        // 유저 존재 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. id=" + userId));

        Integer currentLike = review.getLikeCount();
        if (currentLike == null) currentLike = 0;

        // 이미 좋아요 했는지 확인
        Optional<ReviewLike> existingLike = reviewLikeRepository.findByReviewIdAndUserId(reviewId, userId);

        boolean isLiked;

        if (existingLike.isPresent()) {
            // 좋아요 취소
            reviewLikeRepository.delete(existingLike.get());
            review.setLikeCount(Math.max(0, currentLike - 1));
            isLiked = false;
        } else {
            // 좋아요 추가
            ReviewLike newLike = new ReviewLike();
            newLike.setReview(review);
            newLike.setUser(user);
            reviewLikeRepository.save(newLike);

            review.setLikeCount(currentLike + 1);
            isLiked = true;
        }

        reviewRepository.save(review);

        return new ReviewLikeResponseDTO(isLiked, review.getLikeCount());
    }
}
/*
요약
1. 리뷰 작성 시 관리자에게 DB 알림 생성
2. 리뷰 작성 시 WebSocket으로 관리자 알림 실시간 전송
3. 답글은 ReviewReplyService에서 처리하며, 사용자에게 WebSocket으로 전송됨
4. ReviewService는 “리뷰 자체” 등록 시 관리자 알림 담당
5. 캐시/지연 로딩 문제 방지를 위해 author, productName 등을 즉시 초기화
 */