package com.onandhome.admin.adminReview;

import com.onandhome.review.dto.ReviewDTO;
import com.onandhome.review.dto.ReviewReplyDTO;
import com.onandhome.review.entity.Review;
import com.onandhome.review.ReviewRepository;
import com.onandhome.review.ReviewService;
import com.onandhome.review.ReviewReplyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 관리자 리뷰 관리 REST API 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/reviews")
@RequiredArgsConstructor
// ✅ @CrossOrigin 제거 - SecurityConfig에서 처리
public class AdminReviewRestController {

    private final ReviewRepository reviewRepository;
    private final ReviewService reviewService;
    private final ReviewReplyService reviewReplyService;

    /**
     * 전체 리뷰 목록 조회 (관리자용)
     * GET /api/admin/reviews
     */
    @Transactional(readOnly = true)
    @GetMapping
    public ResponseEntity<List<ReviewDTO>> getAllReviews() {
        log.info("=== 관리자 리뷰 목록 조회 ===");
        
        try {
            List<Review> reviewList = reviewRepository.findAll();
            
            List<ReviewDTO> reviewDTOList = reviewList.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            
            log.info("리뷰 목록 조회 성공 - 총 {}개", reviewDTOList.size());
            return ResponseEntity.ok(reviewDTOList);
        } catch (Exception e) {
            log.error("리뷰 목록 조회 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of());
        }
    }

    /**
     * 리뷰 상세 조회
     * GET /api/admin/reviews/{id}
     */
    @Transactional(readOnly = true)
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getReviewDetail(@PathVariable Long id) {
        log.info("=== 관리자 리뷰 상세 조회 ===");
        log.info("리뷰 ID: {}", id);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Review review = reviewRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다."));
            
            ReviewDTO reviewDTO = convertToDTO(review);
            
            // 답글 목록 조회
            List<ReviewReplyDTO> replies = reviewReplyService.findByReviewId(id);
            
            response.put("success", true);
            response.put("review", reviewDTO);
            response.put("replies", replies);
            
            log.info("리뷰 상세 조회 성공 - ID: {}, 답글 수: {}", id, replies.size());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("리뷰를 찾을 수 없음", e);
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            log.error("리뷰 상세 조회 실패", e);
            response.put("success", false);
            response.put("message", "리뷰 조회 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 리뷰 답글 등록
     * POST /api/admin/reviews/{id}/reply
     */
    @PostMapping("/{id}/reply")
    public ResponseEntity<Map<String, Object>> createReviewReply(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("=== 리뷰 답글 등록 ===");
        log.info("리뷰 ID: {}", id);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String content = request.get("content");
            String responder = request.getOrDefault("responder", "Admin");
            
            if (content == null || content.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "답글 내용을 입력해주세요.");
                return ResponseEntity.badRequest().body(response);
            }
            
            reviewReplyService.createReply(id, content, responder, "admin", null);
            
            response.put("success", true);
            response.put("message", "답글이 등록되었습니다.");
            
            log.info("리뷰 답글 등록 성공 - 리뷰 ID: {}", id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("리뷰를 찾을 수 없음", e);
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            log.error("답글 등록 실패", e);
            response.put("success", false);
            response.put("message", "답글 등록 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 리뷰 답글 수정
     * PUT /api/admin/reviews/reply/{replyId}
     */
    @PutMapping("/reply/{replyId}")
    public ResponseEntity<Map<String, Object>> updateReviewReply(
            @PathVariable Long replyId,
            @RequestBody Map<String, String> request) {
        log.info("=== 리뷰 답글 수정 ===");
        log.info("답글 ID: {}", replyId);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String content = request.get("content");
            
            if (content == null || content.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "답글 내용을 입력해주세요.");
                return ResponseEntity.badRequest().body(response);
            }
            
            reviewReplyService.updateReply(replyId, content);
            
            response.put("success", true);
            response.put("message", "답글이 수정되었습니다.");
            
            log.info("리뷰 답글 수정 성공 - ID: {}", replyId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("답글을 찾을 수 없음", e);
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            log.error("답글 수정 실패", e);
            response.put("success", false);
            response.put("message", "답글 수정 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 리뷰 답글 삭제
     * DELETE /api/admin/reviews/reply/{replyId}
     */
    @DeleteMapping("/reply/{replyId}")
    public ResponseEntity<Map<String, Object>> deleteReviewReply(@PathVariable Long replyId) {
        log.info("=== 리뷰 답글 삭제 ===");
        log.info("답글 ID: {}", replyId);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            reviewReplyService.deleteReply(replyId);
            
            response.put("success", true);
            response.put("message", "답글이 삭제되었습니다.");
            
            log.info("리뷰 답글 삭제 성공 - ID: {}", replyId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("답글을 찾을 수 없음", e);
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            log.error("답글 삭제 실패", e);
            response.put("success", false);
            response.put("message", "답글 삭제 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 리뷰 삭제
     * DELETE /api/admin/reviews/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteReview(@PathVariable Long id) {
        log.info("=== 리뷰 삭제 ===");
        log.info("리뷰 ID: {}", id);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            reviewService.deleteReview(id);
            
            response.put("success", true);
            response.put("message", "리뷰가 삭제되었습니다.");
            
            log.info("리뷰 삭제 성공 - ID: {}", id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("리뷰를 찾을 수 없음", e);
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            log.error("리뷰 삭제 실패", e);
            response.put("success", false);
            response.put("message", "리뷰 삭제 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 여러 리뷰 일괄 삭제
     * POST /api/admin/reviews/delete
     */
    @PostMapping("/delete")
    public ResponseEntity<Map<String, Object>> deleteMultipleReviews(@RequestBody Map<String, List<Long>> request) {
        log.info("=== 리뷰 일괄 삭제 ===");
        
        List<Long> ids = request.get("ids");
        log.info("삭제할 리뷰 ID 목록: {}", ids);
        
        Map<String, Object> response = new HashMap<>();
        
        if (ids == null || ids.isEmpty()) {
            response.put("success", false);
            response.put("message", "삭제할 리뷰를 선택해주세요.");
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            int successCount = 0;
            int failCount = 0;
            
            for (Long id : ids) {
                try {
                    reviewService.deleteReview(id);
                    successCount++;
                } catch (Exception e) {
                    log.error("리뷰 삭제 실패 - ID: {}", id, e);
                    failCount++;
                }
            }
            
            response.put("success", true);
            response.put("message", String.format("%d개의 리뷰가 삭제되었습니다.", successCount));
            response.put("successCount", successCount);
            response.put("failCount", failCount);
            
            log.info("리뷰 일괄 삭제 완료 - 성공: {}, 실패: {}", successCount, failCount);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("리뷰 일괄 삭제 실패", e);
            response.put("success", false);
            response.put("message", "리뷰 삭제 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Review 엔티티를 DTO로 변환
     */
    private ReviewDTO convertToDTO(Review review) {
        ReviewDTO dto = new ReviewDTO();
        dto.setId(review.getId());
        dto.setContent(review.getContent());
        dto.setRating(review.getRating());
        dto.setAuthor(review.getAuthor());
        dto.setUsername(review.getUsername());
        dto.setCreatedAt(review.getCreatedAt());
        dto.setUpdatedAt(review.getUpdatedAt());
        
        if (review.getProduct() != null) {
            dto.setProductId(review.getProduct().getId());
            dto.setProductName(review.getProduct().getName());
        }
        
        if (review.getUser() != null) {
            dto.setUserId(review.getUser().getId());
        }
        
        return dto;
    }
}
