package com.onandhome.favorite;

import com.onandhome.favorite.dto.FavoriteDTO;
import com.onandhome.user.UserRepository;
import com.onandhome.user.entity.User;
import com.onandhome.util.JWTUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 📌 클래스 개요
 * - Base URL: /api/favorites
 * - 역할: 사용자의 상품 찜하기(좋아요) 기능을 위한 RESTful API 제공
 * - 인증: JWT 토큰 기반 (Authorization 헤더)
 *
 * ========================================
 * 📌 제공 API 엔드포인트
 * ========================================
 * | HTTP   | URI                           | 설명                    | 인증 필요 |
 * |--------|-------------------------------|------------------------|----------|
 * | GET    | /api/favorites                | 사용자의 찜 목록 조회      | O        |
 * | POST   | /api/favorites/toggle         | 찜하기 토글 (추가/삭제)    | O        |
 * | GET    | /api/favorites/check/{id}     | 특정 상품 찜 여부 확인     | △ (선택) |
 * | GET    | /api/favorites/count/product/{id} | 상품별 찜 개수 조회    | X        |
 * | GET    | /api/favorites/count          | 사용자의 찜 개수 조회      | △ (선택) |
 * | DELETE | /api/favorites/product/{id}   | 특정 상품 찜 삭제         | O        |
 *
 * ========================================
 * 📌 응답 형식 (공통)
 * ========================================
 * {
 *   "success": boolean,      // 요청 성공 여부
 *   "message": string,       // 결과 메시지 (선택)
 *   "data": object/array,    // 응답 데이터 (선택)
 *   "isFavorite": boolean,   // 찜 여부 (선택)
 *   "count": number          // 개수 (선택)
 * }
 *
 * ========================================
 * 📌 의존성
 * ========================================
 * - FavoriteService: 찜하기 비즈니스 로직
 * - JWTUtil: JWT 토큰 검증 및 클레임 추출
 * - UserRepository: 사용자 정보 조회
 *
 * ========================================
 * 📌 프론트엔드 연동
 * ========================================
 * - 연동 파일: src/api/favoriteApi.js
 * - 사용 컴포넌트: MyFavorites.js, ProductDetail.js, ProductCard.js
 */
@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor //클래스 내에서 반드시 초기화가 필요한 필드(final)만을 파라미터로 받는 생성자를 자동으로 생성
@Slf4j //로깅 객체를 코드를 직접 작성하지 않고도 자동으로 사용할 수 있게 해줍니다.
public class FavoriteRestController {

    /** 찜하기 비즈니스 로직 처리 서비스 */
    private final FavoriteService favoriteService;

    /** JWT 토큰 검증 및 파싱 유틸리티 */
    private final JWTUtil jwtUtil;

    /** 사용자 정보 조회 리포지토리 */
    private final UserRepository userRepository;

    /**
     * JWT 토큰에서 사용자 정보 추출
     * ========================================
     * 📌 처리 흐름
     * ========================================
     * 1. Authorization 헤더 유효성 검사 ("Bearer " 접두사 확인)
     * 2. "Bearer " 제거 후 순수 토큰 추출
     * 3. JWTUtil.validateToken()으로 토큰 검증 및 클레임 추출
     * 4. 클레임에서 userId 추출
     * 5. UserRepository로 User 엔티티 조회
     *
     * ========================================
     * 📌 예외 발생 케이스
     * ========================================
     * - authHeader가 null이거나 "Bearer "로 시작하지 않는 경우
     * - JWT 토큰이 만료되었거나 유효하지 않은 경우
     * - userId에 해당하는 사용자가 DB에 없는 경우
     *
     * @param authHeader Authorization 헤더 (예: "Bearer eyJhbGciOiJIUzI1...")
     * @return User 객체
     * @throws IllegalArgumentException 토큰이 유효하지 않거나 사용자를 찾을 수 없는 경우
     */
    private User getUserFromToken(String authHeader) {
        // 1. Authorization 헤더 검증
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }

        // 2. "Bearer " (7글자) 제거하여 순수 토큰만 추출
        String token = authHeader.substring(7);

        // 3. JWT 토큰 검증 및 클레임(payload) 추출
        // claims 예시: {userId: "user123", exp: 1234567890, iat: 1234567800}
        Map<String, Object> claims = jwtUtil.validateToken(token);

        // 4. 클레임에서 userId 추출
        String userId = (String) claims.get("userId");

        // 5. DB에서 User 엔티티 조회 (없으면 예외 발생)
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    /**
     * 사용자의 찜 목록 조회
     *
     * ========================================
     * 📌 API 정보
     * ========================================
     * - HTTP Method: GET
     * - URL: /api/favorites
     * - 인증: 필수 (JWT 토큰)
     *
     * ========================================
     * 📌 처리 흐름
     * ========================================
     * 1. JWT 토큰에서 사용자 정보 추출
     * 2. FavoriteService.getFavoritesByUserId() 호출
     * 3. FavoriteDTO 리스트 반환
     *
     * ========================================
     * 📌 응답 예시 (성공)
     * ========================================
     * {
     *   "success": true,
     *   "data": [
     *     {
     *       "id": 1,
     *       "userId": 10,
     *       "productId": 100,
     *       "productName": "상품명",
     *       "price": 10000,
     *       "salePrice": 8000,
     *       "thumbnailImage": "image.jpg",
     *       "createdAt": "2024-01-01T12:00:00"
     *     }
     *   ],
     *   "count": 1
     * }
     *
     * ========================================
     * 📌 프론트엔드 호출
     * ========================================
     * favoriteAPI.getList() → MyFavorites.js에서 사용
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getFavorites(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Map<String, Object> response = new HashMap<>();
        try {
            // JWT 토큰에서 사용자 정보 추출
            User user = getUserFromToken(authHeader);
            log.info("찜 목록 조회 요청 - 사용자 ID: {}", user.getId());

            // 사용자의 찜 목록 조회 (최신순 정렬)
            List<FavoriteDTO> favorites = favoriteService.getFavoritesByUserId(user.getId());

            // 성공 응답 구성
            response.put("success", true);
            response.put("data", favorites);
            response.put("count", favorites.size());
            log.info("찜 목록 조회 성공 - 아이템 개수: {}", favorites.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("찜 목록 조회 중 오류: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "찜 목록을 조회하는 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 찜하기 토글 (있으면 삭제, 없으면 추가)
     *
     * ========================================
     * 📌 API 정보
     * ========================================
     * - HTTP Method: POST
     * - URL: /api/favorites/toggle
     * - 인증: 필수 (JWT 토큰)
     * - Content-Type: application/json
     *
     * ========================================
     * 📌 요청 Body
     * ========================================
     * {
     *   "productId": 1
     * }
     *
     * ========================================
     * 📌 처리 흐름
     * ========================================
     * 1. JWT 토큰에서 사용자 정보 추출
     * 2. productId 유효성 검증 (null 또는 0 이하 체크)
     * 3. FavoriteService.toggleFavorite() 호출
     *    - 이미 찜한 상품 → 삭제 후 null 반환
     *    - 찜하지 않은 상품 → 추가 후 FavoriteDTO 반환
     * 4. 결과에 따라 응답 구성
     *
     * ========================================
     * 📌 응답 예시 (찜 추가)
     * ========================================
     * {
     *   "success": true,
     *   "message": "찜하기에 추가되었습니다.",
     *   "isFavorite": true,
     *   "data": { ... FavoriteDTO ... }
     * }
     *
     * ========================================
     * 📌 응답 예시 (찜 취소)
     * ========================================
     * {
     *   "success": true,
     *   "message": "찜하기가 취소되었습니다.",
     *   "isFavorite": false
     * }
     *
     * ========================================
     * 📌 프론트엔드 호출
     * ========================================
     * favoriteAPI.toggle(productId) → ProductCard.js, ProductDetail.js에서 사용
     */
    @PostMapping("/toggle")
    public ResponseEntity<Map<String, Object>> toggleFavorite(
            @RequestBody ToggleFavoriteRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Map<String, Object> response = new HashMap<>();
        try {
            log.info("[toggleFavorite] 찜하기 토글 API 호출 시작");

            // 1. JWT 토큰에서 사용자 정보 추출
            User user = getUserFromToken(authHeader);
            log.info("[toggleFavorite] JWT 검증 성공 - 사용자 ID: {}", user.getId());

            // 2. 입력값 검증: productId가 null이거나 0 이하면 오류
            if (request.getProductId() == null || request.getProductId() <= 0) {
                response.put("success", false);
                response.put("message", "올바른 상품 ID를 입력하세요.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            log.info("[toggleFavorite] 찜하기 토글 요청 - 사용자 ID: {}, 상품 ID: {}",
                    user.getId(), request.getProductId());

            // 3. 토글 로직 실행: 이미 찜했으면 삭제(null 반환), 아니면 추가(DTO 반환)
            FavoriteDTO result = favoriteService.toggleFavorite(user.getId(), request.getProductId());

            // 4. 결과에 따라 응답 구성
            response.put("success", true);
            if (result == null) {
                // 찜하기 취소된 경우
                response.put("message", "찜하기가 취소되었습니다.");
                response.put("isFavorite", false);
                log.info("[toggleFavorite] 찜하기 취소 성공");
            } else {
                // 찜하기 추가된 경우
                response.put("message", "찜하기에 추가되었습니다.");
                response.put("isFavorite", true);
                response.put("data", result);
                log.info("[toggleFavorite] 찜하기 추가 성공 - favoriteId: {}", result.getId());
            }

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            // 입력값 오류 또는 인증 오류
            log.warn("[toggleFavorite] 입력 값 오류: {}", e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            // 기타 서버 오류
            log.error("[toggleFavorite] 찜하기 토글 중 오류: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "찜하기 처리 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 특정 상품이 찜되어 있는지 확인
     * ========================================
     * 📌 API 정보
     * ========================================
     * - HTTP Method: GET
     * - URL: /api/favorites/check/{productId}
     * - 인증: 선택 (비로그인 시 false 반환)
     *
     * ========================================
     * 📌 처리 흐름
     * ========================================
     * 1. Authorization 헤더 확인
     *    - 없거나 유효하지 않으면 → isFavorite: false 반환
     * 2. JWT 토큰에서 사용자 정보 추출
     * 3. FavoriteService.isFavorite() 호출
     * 4. 결과 반환
     *
     * ========================================
     * 📌 응답 예시
     * ========================================
     * {
     *   "success": true,
     *   "isFavorite": true  // 또는 false
     * }
     * ========================================
     * 📌 프론트엔드 호출
     * ========================================
     * favoriteAPI.check(productId) → ProductDetail.js에서 하트 아이콘 상태 결정
     */
    @GetMapping("/check/{productId}")
    public ResponseEntity<Map<String, Object>> checkFavorite(
            @PathVariable Long productId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 로그인하지 않은 경우 → 무조건 false 반환 (에러 없이)
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.put("success", true);
                response.put("isFavorite", false);
                return ResponseEntity.ok(response);
            }

            // JWT 토큰에서 사용자 정보 추출
            User user = getUserFromToken(authHeader);
            log.info("찜 여부 확인 요청 - 사용자 ID: {}, 상품 ID: {}", user.getId(), productId);

            // 찜 여부 확인
            boolean isFavorite = favoriteService.isFavorite(user.getId(), productId);

            response.put("success", true);
            response.put("isFavorite", isFavorite);
            log.info("찜 여부 확인 성공 - 찜 여부: {}", isFavorite);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            // 오류 발생 시에도 false 반환 (사용자 경험 우선)
            log.error("찜 여부 확인 중 오류: {}", e.getMessage(), e);
            response.put("success", true);
            response.put("isFavorite", false);
            return ResponseEntity.ok(response);
        }
    }

    /**
     * 특정 상품의 찜 개수 조회
     * ========================================
     * 📌 API 정보
     * ========================================
     * - HTTP Method: GET
     * - URL: /api/favorites/count/product/{productId}
     * - 인증: 불필요 (공개 API)
     * ========================================
     * 📌 용도
     * ========================================
     * - 상품 상세 페이지에서 "N명이 찜했습니다" 표시
     * - 상품 인기도 지표로 활용
     * ========================================
     * 📌 응답 예시
     * ========================================
     * {
     *   "success": true,
     *   "count": 42
     * }
     */
    @GetMapping("/count/product/{productId}")
    public ResponseEntity<Map<String, Object>> getFavoriteCountByProduct(
            @PathVariable Long productId) {
        Map<String, Object> response = new HashMap<>();
        try {
            log.info("상품 찜 개수 조회 요청 - 상품 ID: {}", productId);

            // 상품별 찜 개수 조회
            long count = favoriteService.getFavoriteCountByProductId(productId);

            response.put("success", true);
            response.put("count", count);
            log.info("상품 찜 개수 조회 성공 - 개수: {}", count);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("상품 찜 개수 조회 중 오류: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "찜 개수를 조회하는 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 사용자의 찜 개수 조회
     *
     * ========================================
     * 📌 API 정보
     * ========================================
     * - HTTP Method: GET
     * - URL: /api/favorites/count
     * - 인증: 선택 (비로그인 시 0 반환)
     *
     * ========================================
     * 📌 용도
     * ========================================
     * - 마이페이지 또는 헤더에서 찜 개수 배지 표시
     * - "내 찜 목록 (N개)" 형태로 표시
     *
     * ========================================
     * 📌 응답 예시
     * ========================================
     * {
     *   "success": true,
     *   "count": 5
     * }
     *
     * ========================================
     * 📌 프론트엔드 호출
     * ========================================
     * favoriteAPI.getCount() → Header.js에서 찜 개수 배지 표시
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> getFavoriteCount(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 로그인하지 않은 경우 → 0 반환 (에러 없이)
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.put("success", true);
                response.put("count", 0);
                return ResponseEntity.ok(response);
            }

            // JWT 토큰에서 사용자 정보 추출
            User user = getUserFromToken(authHeader);
            log.info("사용자 찜 개수 조회 요청 - 사용자 ID: {}", user.getId());

            // 사용자의 찜 개수 조회
            long count = favoriteService.getFavoriteCountByUserId(user.getId());

            response.put("success", true);
            response.put("count", count);
            log.info("사용자 찜 개수 조회 성공 - 개수: {}", count);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            // 오류 발생 시에도 0 반환 (사용자 경험 우선)
            log.error("사용자 찜 개수 조회 중 오류: {}", e.getMessage(), e);
            response.put("success", true);
            response.put("count", 0);
            return ResponseEntity.ok(response);
        }
    }

    /**
     * 찜하기 삭제 (특정 상품)
     *
     * ========================================
     * 📌 API 정보
     * ========================================
     * - HTTP Method: DELETE
     * - URL: /api/favorites/product/{productId}
     * - 인증: 필수 (JWT 토큰)
     *
     * ========================================
     * 📌 처리 흐름
     * ========================================
     * 1. JWT 토큰에서 사용자 정보 추출
     * 2. FavoriteService.removeFavorite() 호출
     * 3. 성공 응답 반환
     *
     * ========================================
     * 📌 toggle vs delete 차이점
     * ========================================
     * - toggle: 상태에 따라 추가 또는 삭제 (토글 동작)
     * - delete: 무조건 삭제만 수행 (명시적 삭제)
     *
     * ========================================
     * 📌 응답 예시
     * ========================================
     * {
     *   "success": true,
     *   "message": "찜하기에서 제거되었습니다."
     * }
     *
     * ========================================
     * 📌 프론트엔드 호출
     * ========================================
     * MyFavorites.js에서 삭제 버튼 클릭 시 사용 가능
     * (현재는 toggle API 사용 중)
     */
    @DeleteMapping("/product/{productId}")
    public ResponseEntity<Map<String, Object>> removeFavorite(
            @PathVariable Long productId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Map<String, Object> response = new HashMap<>();
        try {
            // JWT 토큰에서 사용자 정보 추출
            User user = getUserFromToken(authHeader);
            log.info("찜하기 삭제 요청 - 사용자 ID: {}, 상품 ID: {}", user.getId(), productId);

            // 찜하기 삭제 실행
            favoriteService.removeFavorite(user.getId(), productId);

            response.put("success", true);
            response.put("message", "찜하기에서 제거되었습니다.");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("찜하기 삭제 중 오류: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "찜하기 제거 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 찜하기 토글 요청 DTO
     *
     * ========================================
     * 📌 필드 설명
     * ========================================
     * - productId: 찜할 상품의 고유 ID
     *
     * ========================================
     * 📌 요청 JSON 예시
     * ========================================
     * {
     *   "productId": 1
     * }
     *
     * ========================================
     * 📌 사용처
     * ========================================
     * - POST /api/favorites/toggle 엔드포인트의 @RequestBody
     */
    @Getter
    @Setter
    public static class ToggleFavoriteRequest {
        /** 찜할 상품의 고유 ID (DB의 product.id) */
        private Long productId;
    }
}
