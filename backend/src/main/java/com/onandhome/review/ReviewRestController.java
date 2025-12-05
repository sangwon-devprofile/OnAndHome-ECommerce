package com.onandhome.review;

import com.onandhome.review.dto.ReviewDTO;
import com.onandhome.review.dto.ReviewLikeResponseDTO;
import com.onandhome.review.entity.Review;
import com.onandhome.review.entity.ReviewReply;
import com.onandhome.user.UserRepository;
import com.onandhome.user.UserService;
import com.onandhome.user.dto.UserDTO;
import com.onandhome.user.entity.User;
import com.onandhome.util.JWTUtil;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.onandhome.review.entity.ReviewImage;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

/**
 * 리뷰 REST API 컨트롤러
 */
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Slf4j
public class ReviewRestController {

    private final ReviewService reviewService;
    private final UserService userService;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final JWTUtil jwtUtil;
    private static final String SESSION_USER_KEY = "loginUser";

    private final com.onandhome.file.FileStorageService fileStorageService;
    private final ReviewImageRepository reviewImageRepository;
    /**
     * 상품별 리뷰 목록 조회
     * GET /api/reviews/product/{productId}
     */
    /** ✅ 상품별 리뷰 조회 - userId 파라미터 추가 */
    @GetMapping("/product/{productId}")
    public ResponseEntity<?> getProductReviews(
            @PathVariable Long productId,
            @RequestParam(required = false) Long userId) {
        try {
            // 좋아요 여부 포함한 조회
            List<ReviewDTO> reviews = reviewService.findByProductIdWithLikes(productId, userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", reviews);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());

            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 개별 리뷰 조회
     * GET /api/reviews/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getReviewById(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            log.info("리뷰 상세 조회 요청 - id: {}", id);
            ReviewDTO review = reviewService.findById(id);
            response.put("success", true);
            response.put("data", review);
            log.info("리뷰 상세 조회 성공");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("리뷰를 찾을 수 없음", e);
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(404).body(response);
        } catch (Exception e) {
            log.error("리뷰 상세 조회 오류", e);
            response.put("success", false);
            response.put("message", "리뷰를 불러올 수 없습니다.");
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 최근 리뷰 목록 조회 (Footer용)
     * GET /api/reviews/recent?limit=8
     */
    @GetMapping("/recent")
    public ResponseEntity<Map<String, Object>> getRecentReviews(
            @RequestParam(defaultValue = "8") int limit) {
        Map<String, Object> response = new HashMap<>();
        try {
            log.info("최근 리뷰 목록 조회 요청 - limit: {}", limit);
            List<ReviewDTO> reviews = reviewService.findRecent(limit);
            response.put("success", true);
            response.put("data", reviews);
            log.info("최근 리뷰 목록 조회 성공 - 개수: {}", reviews.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("최근 리뷰 목록 조회 오류", e);
            response.put("success", false);
            response.put("message", "리뷰 목록을 불러올 수 없습니다.");
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 내 리뷰 목록 조회 (마이페이지용)
     * GET /api/reviews/my
     */
    @GetMapping("/my")
    public ResponseEntity<Map<String, Object>> getMyReviews(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        try {
            log.info("=== 내 리뷰 목록 조회 요청 ===");
            
            // userId 추출 (JWT 또는 세션)
            String userId = getCurrentUserId(authHeader, session);
            if (userId == null) {
                log.error("인증 정보 없음");
                response.put("success", false);
                response.put("message", "로그인이 필요합니다.");
                return ResponseEntity.status(401).body(response);
            }
            
            log.info("추출된 userId: {}", userId);
            
            // userId로 User 조회
            User user = userRepository.findByUserId(userId).orElse(null);
            if (user == null) {
                log.error("사용자를 찾을 수 없음 - userId: {}", userId);
                response.put("success", false);
                response.put("message", "사용자를 찾을 수 없습니다.");
                return ResponseEntity.status(404).body(response);
            }
            
            Long userIdLong = user.getId();
            log.info("User 정보 - id: {}, username: {}, userId: {}", userIdLong, user.getUsername(), userId);
            
            // user_id로만 리뷰 조회 (Product fetch join 사용)
            List<Review> allReviews = reviewRepository.findByUserIdWithProduct(userIdLong);
            log.info("총 조회된 리뷰 개수: {}", allReviews.size());
            
            // DTO 변환 (ReviewDTO.fromEntity 사용하여 이미지 포함)
            List<ReviewDTO> reviewDTOList = new ArrayList<>();
            for (Review review : allReviews) {
                // fromEntity를 사용하면 이미지도 자동으로 포함됨
                ReviewDTO dto = ReviewDTO.fromEntity(review);
                
                // Product 정보 설정 (fetch join으로 이미 로드됨)
                if (review.getProduct() != null) {
                    dto.setProductId(review.getProduct().getId());
                    dto.setProductName(review.getProduct().getName());
                } else {
                    // Product가 null인 경우 Review에 저장된 productName 사용
                    dto.setProductName(review.getProductName());
                }
                
                reviewDTOList.add(dto);
            }
            
            // 최신순 정렬
            reviewDTOList.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
            
            response.put("success", true);
            response.put("data", reviewDTOList);
            log.info("내 리뷰 조회 성공 - 최종 반환 개수: {}", reviewDTOList.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("내 리뷰 조회 오류", e);
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "리뷰를 불러올 수 없습니다: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 리뷰 작성
     * POST /api/reviews
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createReview(
            @RequestBody Map<String, Object> request,
            HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        try {
            log.info("리뷰 작성 요청: {}", request);
            
            // 세션에서 로그인한 사용자 정보 가져오기
            UserDTO loginUser = (UserDTO) session.getAttribute(SESSION_USER_KEY);
            
            Long userId = null;
            
            // 세션에 사용자 정보가 없으면 요청 본문에서 가져오기 (JWT 토큰 사용 시)
            if (loginUser == null) {
                if (request.containsKey("userId")) {
                    userId = Long.valueOf(request.get("userId").toString());
                    log.info("요청 본문에서 userId 추출: {}", userId);
                } else {
                    log.warn("사용자 정보가 없음 - 로그인 필요");
                    response.put("success", false);
                    response.put("message", "로그인이 필요합니다.");
                    return ResponseEntity.status(401).body(response);
                }
            } else {
                userId = loginUser.getId();
                log.info("세션에서 userId 추출: {}", userId);
            }
            
            Long productId = Long.valueOf(request.get("productId").toString());
            String content = request.get("content").toString();
            int rating = request.containsKey("rating") ? 
                    Integer.parseInt(request.get("rating").toString()) : 5;
            
            log.info("리뷰 작성 - userId: {}, productId: {}, content: {}", userId, productId, content);
            
            ReviewDTO review = reviewService.createReview(productId, userId, content, rating);
            
            response.put("success", true);
            response.put("message", "리뷰가 등록되었습니다.");
            response.put("data", review);
            log.info("리뷰 작성 성공 - reviewId: {}", review.getId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("리뷰 작성 오류", e);
            response.put("success", false);
            response.put("message", "리뷰 작성 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 리뷰 작성 + 이미지 업로드
     * POST /api/reviews/with-images
     * multipart/form-data
     */
    @Transactional
    @PostMapping(
            value = "/with-images",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<Map<String, Object>> createReviewWithImages(
            @RequestParam Long productId,
            @RequestParam(required = false) Long userId,      // 요청에서 userId 받을 수도 있고
            @RequestParam String content,
            @RequestParam(required = false, defaultValue = "5") int rating,
            @RequestParam(required = false) List<MultipartFile> images,
            HttpSession session
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            log.info("리뷰 + 이미지 작성 요청: productId={}, userId={}, content={}, rating={}, imagesCount={}",
                    productId, userId, content, rating,
                    (images == null ? 0 : images.size()));

            // 1) 세션 기반 로그인 사용자 우선 사용
            UserDTO loginUser = (UserDTO) session.getAttribute(SESSION_USER_KEY);
            Long finalUserId = null;

            if (loginUser != null) {
                finalUserId = loginUser.getId();
                log.info("세션에서 userId 사용: {}", finalUserId);
            } else if (userId != null) {
                finalUserId = userId;
                log.info("요청 파라미터에서 userId 사용: {}", finalUserId);
            } else {
                log.warn("사용자 정보가 없음 - 로그인 필요");
                response.put("success", false);
                response.put("message", "로그인이 필요합니다.");
                return ResponseEntity.status(401).body(response);
            }

            // 2) 기존 서비스로 리뷰 먼저 생성
            ReviewDTO reviewDTO = reviewService.createReview(productId, finalUserId, content, rating);
            log.info("리뷰 작성 성공 - reviewId: {}", reviewDTO.getId());

            // 3) 생성된 리뷰 엔티티 다시 조회
            Review review = reviewRepository.findById(reviewDTO.getId())
                    .orElseThrow(() -> new IllegalArgumentException("리뷰 엔티티를 찾을 수 없습니다."));

            // 4) 이미지가 있다면 저장 + ReviewImage 생성
            if (images != null && !images.isEmpty()) {
                for (MultipartFile file : images) {
                    if (file == null || file.isEmpty()) {
                        continue;
                    }

                    // 4-1) 실제 파일 저장 (uploads/ 아래)
                    String imageUrl = fileStorageService.storeFile(file); // 예: /uploads/uuid.jpg
                    log.info("이미지 저장 완료 - url: {}", imageUrl);

                    // 4-2) ReviewImage 엔티티 생성 및 연관관계 설정
                    ReviewImage reviewImage = new ReviewImage();
                    reviewImage.setReview(review);
                    reviewImage.setImageUrl(imageUrl);

                    // createdAt은 @PrePersist에서 자동 세팅
                    reviewImageRepository.save(reviewImage);
                }
            }

            response.put("success", true);
            response.put("message", "리뷰가 등록되었습니다.");
            response.put("data", reviewDTO);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("리뷰 + 이미지 작성 중 오류", e);
            response.put("success", false);
            response.put("message", "리뷰 작성 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 리뷰 수정
     * PUT /api/reviews/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateReview(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        try {
            log.info("리뷰 수정 요청 - id: {}", id);
            
            // 기존 리뷰 조회
            Review review = reviewRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다."));
            
            // 작성자 확인
            String currentUserId = getCurrentUserId(authHeader, session);
            if (!review.getUsername().equals(currentUserId)) {
                response.put("success", false);
                response.put("message", "작성자만 수정할 수 있습니다.");
                return ResponseEntity.status(403).body(response);
            }
            
            // 수정
            String content = request.get("content").toString();
            int rating = request.containsKey("rating") ? 
                    Integer.parseInt(request.get("rating").toString()) : review.getRating();
            
            ReviewDTO updatedReview = reviewService.updateReview(id, content, rating);
            
            response.put("success", true);
            response.put("message", "리뷰가 수정되었습니다.");
            response.put("data", updatedReview);
            log.info("리뷰 수정 성공 - id: {}", id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("리뷰 수정 실패", e);
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(404).body(response);
        } catch (Exception e) {
            log.error("리뷰 수정 오류", e);
            response.put("success", false);
            response.put("message", "리뷰 수정 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 리뷰 수정 + 이미지 추가/삭제
     * PUT /api/reviews/{id}/with-images
     * multipart/form-data
     */
    @Transactional
    @PutMapping(
            value = "/{id}/with-images",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<Map<String, Object>> updateReviewWithImages(
            @PathVariable Long id,
            @RequestParam String content,
            @RequestParam(required = false, defaultValue = "-1") int rating,
            @RequestParam(required = false) List<MultipartFile> images,
            @RequestParam(required = false, name = "deleteImageIds") List<Long> deleteImageIds,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            HttpSession session
    ) {
        Map<String, Object> response = new HashMap<>();
        try {

            log.info("리뷰 수정(+이미지) 요청 - id: {}, content: {}, rating: {}, images: {}, deleteIds: {}",
                    id, content, rating,
                    (images == null ? 0 : images.size()),
                    (deleteImageIds == null ? "[]" : deleteImageIds)
            );

            // 리뷰 조회
            Review review = reviewRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다."));

            // 작성자 확인
            String currentUserId = getCurrentUserId(authHeader, session);
            if (currentUserId == null || !review.getUsername().equals(currentUserId)) {
                response.put("success", false);
                response.put("message", "작성자만 수정할 수 있습니다.");
                return ResponseEntity.status(403).body(response);
            }

            // 내용 + 평점 업데이트
            int finalRating = (rating == -1) ? review.getRating() : rating;
            reviewService.updateReview(id, content, finalRating);
            log.info("리뷰 내용/평점 수정 완료");

            // 삭제할 이미지 처리
            if (deleteImageIds != null && !deleteImageIds.isEmpty()) {
                for (Long imageId : deleteImageIds) {
                    if (imageId == null) continue;

                    reviewImageRepository.findById(imageId).ifPresent(image -> {
                        if (image.getReview().getId().equals(id)) {
                            log.info("리뷰 이미지 삭제 - imageId: {}", imageId);
                            reviewImageRepository.delete(image);
                        }
                    });
                }
            }

            // 새 이미지 저장
            if (images != null && !images.isEmpty()) {
                for (MultipartFile file : images) {
                    if (file.isEmpty()) continue;

                    String imageUrl = fileStorageService.storeFile(file);
                    log.info("리뷰 이미지 추가 저장 - {}", imageUrl);

                    ReviewImage reviewImage = new ReviewImage();
                    reviewImage.setReview(review);
                    reviewImage.setImageUrl(imageUrl);
                    reviewImageRepository.save(reviewImage);
                }
            }

            // 최신 리뷰 다시 조회 → 최신 이미지/내용 DTO 변환
            Review refreshed = reviewRepository.findByIdWithDetails(id)
                    .orElseThrow(() -> new IllegalArgumentException("리뷰를 다시 조회할 수 없습니다."));
            ReviewDTO resultDto = ReviewDTO.fromEntity(refreshed);

            response.put("success", true);
            response.put("message", "리뷰가 수정되었습니다.");
            response.put("data", resultDto);

            log.info("리뷰 수정(+이미지) 최종 성공 - id: {}", id);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("리뷰 수정(+이미지) 중 오류", e);
            response.put("success", false);
            response.put("message", "리뷰 수정 중 오류 발생: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 리뷰 삭제
     * DELETE /api/reviews/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteReview(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        try {
            log.info("리뷰 삭제 요청 - id: {}", id);
            
            // 기존 리뷰 조회
            Review review = reviewRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다."));
            
            // 작성자 확인
            String currentUserId = getCurrentUserId(authHeader, session);
            if (!review.getUsername().equals(currentUserId)) {
                response.put("success", false);
                response.put("message", "작성자만 삭제할 수 있습니다.");
                return ResponseEntity.status(403).body(response);
            }
            
            // 삭제
            reviewService.deleteById(id);
            
            response.put("success", true);
            response.put("message", "리뷰가 삭제되었습니다.");
            log.info("리뷰 삭제 성공 - id: {}", id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("리뷰 삭제 실패", e);
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(404).body(response);
        } catch (Exception e) {
            log.error("리뷰 삭제 오류", e);
            response.put("success", false);
            response.put("message", "리뷰 삭제 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * JWT 토큰 또는 세션에서 사용자 ID 추출
     */
    private String getCurrentUserId(String authHeader, HttpSession session) {
        // 1. JWT 토큰에서 확인
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7);
                Map<String, Object> claims = jwtUtil.validateToken(token);
                String userId = claims.get("userId").toString();
                log.info("JWT에서 userId 추출: {}", userId);
                return userId;
            } catch (Exception e) {
                log.error("JWT 토큰 검증 실패", e);
            }
        }
        
        // 2. 세션에서 확인
        if (session != null) {
            UserDTO loginUser = (UserDTO) session.getAttribute(SESSION_USER_KEY);
            if (loginUser != null) {
                String userId = loginUser.getUserId();
                log.info("세션에서 userId 추출: {}", userId);
                return userId;
            }
        }
        
        log.warn("인증 정보를 찾을 수 없음");
        return null;
    }
    @PostMapping("/{reviewId}/like")
    public ResponseEntity<?> toggleLike(
            @PathVariable Long reviewId,
            @RequestParam Long userId) {
        try {
            ReviewLikeResponseDTO result = reviewService.toggleLike(reviewId, userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", result);
            response.put("message", result.isLiked() ? "좋아요를 눌렀습니다." : "좋아요를 취소했습니다.");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());

            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}

