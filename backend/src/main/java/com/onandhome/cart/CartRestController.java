package com.onandhome.cart;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.onandhome.cart.entity.CartItem;
import com.onandhome.user.UserRepository;
import com.onandhome.user.entity.User;
import com.onandhome.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 장바구니 REST API 컨트롤러
 * 장바구니 담기, 조회, 수정, 삭제 기능 제공
 *
 * ========================================
 * 📌 클래스 개요
 * ========================================
 * - Base URL: /api/cart
 * - 역할: 사용자의 장바구니 관리를 위한 RESTful API 제공
 * - 인증: JWT 토큰 기반 (Authorization 헤더)
 *
 * ========================================
 * 📌 제공 API 엔드포인트
 * ========================================
 * | HTTP   | URI                  | 설명                 | 인증 필요 |
 * |--------|---------------------|----------------------|----------|
 * | POST   | /api/cart/add       | 장바구니 담기         | O        |
 * | GET    | /api/cart           | 장바구니 목록 조회    | O        |
 * | GET    | /api/cart/count     | 장바구니 아이템 개수  | △ (선택) |
 * | PUT    | /api/cart/{id}      | 수량 변경            | O        |
 * | DELETE | /api/cart/{id}      | 아이템 삭제          | O        |
 * | DELETE | /api/cart/clear/all | 전체 비우기          | O        |
 *
 * ========================================
 * 📌 응답 형식 (공통)
 * ========================================
 * {
 *   "success": boolean,      // 요청 성공 여부
 *   "message": string,       // 결과 메시지 (선택)
 *   "data": object/array,    // 응답 데이터 (선택)
 *   "count": number          // 개수 (선택)
 * }
 *
 * ========================================
 * 📌 의존성
 * ========================================
 * - CartService: 장바구니 비즈니스 로직
 * - JWTUtil: JWT 토큰 검증 및 클레임 추출
 * - UserRepository: 사용자 정보 조회
 *
 * ========================================
 * 📌 프론트엔드 연동
 * ========================================
 * - 연동 파일: src/api/cartApi.js
 * - 사용 컴포넌트: Cart.js, CartFloatingButton.js, CartSidePanel.js
 * - Redux: src/store/slices/cartSlice.js
 */
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Slf4j
public class CartRestController {

    /** 장바구니 비즈니스 로직 처리 서비스 */
    private final CartService cartService;

    /** JWT 토큰 검증 및 파싱 유틸리티 */
    private final JWTUtil jwtUtil;

    /** 사용자 정보 조회 리포지토리 */
    private final UserRepository userRepository;

    /**
     * 장바구니 담기
     *
     * ========================================
     * 📌 API 정보
     * ========================================
     * - HTTP Method: POST
     * - URL: /api/cart/add
     * - 인증: 필수 (JWT 토큰)
     * - Content-Type: application/json
     *
     * ========================================
     * 📌 요청 Body
     * ========================================
     * {
     *   "productId": 1,      // 담을 상품 ID (필수)
     *   "quantity": 1        // 수량 (필수, 1 이상)
     * }
     *
     * ========================================
     * 📌 처리 흐름
     * ========================================
     * 1. JWT 토큰 검증 및 사용자 추출
     * 2. 입력값 검증 (productId, quantity)
     * 3. CartService.addToCart() 호출
     *    - 기존 상품이면 수량 증가
     *    - 새 상품이면 CartItem 생성
     * 4. 결과 반환
     *
     * ========================================
     * 📌 응답 예시 (성공)
     * ========================================
     * {
     *   "success": true,
     *   "message": "장바구니에 상품이 추가되었습니다.",
     *   "data": {
     *     "id": 1,
     *     "user": {...},
     *     "product": {...},
     *     "quantity": 2
     *   }
     * }
     *
     * ========================================
     * 📌 프론트엔드 호출
     * ========================================
     * cartAPI.addToCart(productId, quantity)
     * → ProductDetail.js "장바구니 담기" 버튼
     */
    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addToCart(
            @RequestBody AddToCartRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Map<String, Object> response = new HashMap<>();
        try {
            log.info("[addToCart] 장바구니 담기 API 호출 시작");
            
            // 1. JWT 토큰 추출 및 검증
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("[addToCart] Authorization 헤더가 없거나 형식이 올바르지 않음");
                response.put("success", false);
                response.put("message", "로그인이 필요합니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            // "Bearer " (7글자) 제거 후 토큰 추출
            String token = authHeader.substring(7);
            Map<String, Object> claims = jwtUtil.validateToken(token);
            String userId = (String) claims.get("userId");
            
            log.info("[addToCart] JWT 검증 성공 - 사용자: {}", userId);
            
            // 2. 사용자 조회
            User user = userRepository.findByUserId(userId)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

            log.info("[addToCart] 장바구니 담기 요청 - 사용자 ID: {}, 상품 ID: {}, 수량: {}", 
                    user.getId(), request.getProductId(), request.getQuantity());

            // 3. 입력값 검증
            if (request.getProductId() == null || request.getProductId() <= 0) {
                response.put("success", false);
                response.put("message", "올바른 상품 ID를 입력하세요.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            if (request.getQuantity() <= 0) {
                response.put("success", false);
                response.put("message", "수량은 1 이상이어야 합니다.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // 4. 장바구니 담기 실행
            CartItem cartItem = cartService.addToCart(
                    user.getId(), 
                    request.getProductId(),
                    request.getQuantity()
            );

            // 5. 성공 응답
            response.put("success", true);
            response.put("message", "장바구니에 상품이 추가되었습니다.");
            response.put("data", cartItem);
            log.info("[addToCart] 장바구니 담기 성공 - 장바구니 ID: {}", cartItem.getId());
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.warn("[addToCart] 입력 값 오류: {}", e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            log.error("[addToCart] 장바구니 담기 중 오류: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "장바구니에 상품을 추가하는 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 사용자의 장바구니 조회
     *
     * ========================================
     * 📌 API 정보
     * ========================================
     * - HTTP Method: GET
     * - URL: /api/cart
     * - 인증: 필수 (JWT 토큰)
     *
     * ========================================
     * 📌 처리 흐름
     * ========================================
     * 1. JWT 토큰 검증 및 사용자 추출
     * 2. CartService.getCartItems() 호출
     * 3. CartItem 리스트 반환 (Product 정보 포함)
     *
     * ========================================
     * 📌 응답 예시 (성공)
     * ========================================
     * {
     *   "success": true,
     *   "data": [
     *     {
     *       "id": 1,
     *       "product": {
     *         "id": 100,
     *         "name": "상품명",
     *         "price": 10000,
     *         "salePrice": 8000,
     *         "thumbnailImage": "image.jpg"
     *       },
     *       "quantity": 2
     *     }
     *   ],
     *   "count": 1
     * }
     *
     * ========================================
     * 📌 프론트엔드 호출
     * ========================================
     * cartAPI.getCartItems() → Cart.js, CartSidePanel.js
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getCart(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 1. JWT 토큰 추출 및 검증
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("로그인하지 않은 사용자의 장바구니 조회 시도");
                response.put("success", false);
                response.put("message", "로그인이 필요합니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            String token = authHeader.substring(7);
            Map<String, Object> claims = jwtUtil.validateToken(token);
            String userId = (String) claims.get("userId");
            
            // 2. 사용자 조회
            User user = userRepository.findByUserId(userId)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

            log.info("장바구니 조회 요청 - 사용자 ID: {}", user.getId());

            // 3. 장바구니 목록 조회
            List<CartItem> cartItems = cartService.getCartItems(user.getId());

            // 4. 성공 응답
            response.put("success", true);
            response.put("data", cartItems);
            response.put("count", cartItems.size());
            log.info("장바구니 조회 성공 - 아이템 개수: {}", cartItems.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("장바구니 조회 중 오류: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "장바구니를 조회하는 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 장바구니 아이템 개수 조회
     *
     * ========================================
     * 📌 API 정보
     * ========================================
     * - HTTP Method: GET
     * - URL: /api/cart/count
     * - 인증: 선택 (비로그인 시 0 반환)
     *
     * ========================================
     * 📌 용도
     * ========================================
     * - 헤더 또는 플로팅 버튼에 장바구니 배지 표시
     * - CartFloatingButton.js에서 5초마다 호출
     *
     * ========================================
     * 📌 응답 예시
     * ========================================
     * {
     *   "success": true,
     *   "count": 3
     * }
     *
     * ========================================
     * 📌 프론트엔드 호출
     * ========================================
     * cartAPI.getCartCount() → CartFloatingButton.js
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> getCartCount(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 1. 로그인하지 않은 경우 → 0 반환 (에러 없이)
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.put("success", true);
                response.put("count", 0);
                return ResponseEntity.ok(response);
            }

            // 2. JWT 토큰 검증
            String token = authHeader.substring(7);
            Map<String, Object> claims = jwtUtil.validateToken(token);
            String userId = (String) claims.get("userId");
            
            // 3. 사용자 조회
            User user = userRepository.findByUserId(userId)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

            // log.info("장바구니 개수 조회 요청 - 사용자 ID: {}", user.getId());

            // 4. 장바구니 개수 조회
            List<CartItem> cartItems = cartService.getCartItems(user.getId());
            int count = cartItems.size();

            // 5. 성공 응답
            response.put("success", true);
            response.put("count", count);
            // log.info("장바구니 개수 조회 성공 - 아이템 개수: {}", count);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            // 오류 발생 시에도 0 반환 (사용자 경험 우선)
            log.error("장바구니 개수 조회 중 오류: {}", e.getMessage(), e);
            response.put("success", true);
            response.put("count", 0);
            return ResponseEntity.ok(response);
        }
    }

    /**
     * 장바구니 아이템 수량 수정
     *
     * ========================================
     * 📌 API 정보
     * ========================================
     * - HTTP Method: PUT
     * - URL: /api/cart/{cartItemId}
     * - 인증: 필수 (JWT 토큰)
     * - Content-Type: application/json
     *
     * ========================================
     * 📌 요청 Body
     * ========================================
     * {
     *   "quantity": 5    // 변경할 수량 (1 이상)
     * }
     *
     * ========================================
     * 📌 처리 흐름
     * ========================================
     * 1. JWT 토큰 검증 및 사용자 확인
     * 2. 수량 유효성 검증 (1 이상)
     * 3. CartService.updateQuantity() 호출
     * 4. 업데이트된 CartItem 반환
     *
     * ========================================
     * 📌 프론트엔드 호출
     * ========================================
     * cartAPI.updateQuantity(cartItemId, quantity)
     * → Cart.js "+/-" 버튼, CartSidePanel.js
     */
    @PutMapping("/{cartItemId}")
    public ResponseEntity<Map<String, Object>> updateQuantity(
            @PathVariable Long cartItemId,
            @RequestBody UpdateQuantityRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 1. JWT 토큰 추출 및 검증
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            String token = authHeader.substring(7);
            Map<String, Object> claims = jwtUtil.validateToken(token);
            String userId = (String) claims.get("userId");
            
            // 2. 사용자 조회
            User user = userRepository.findByUserId(userId)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

            // 3. 수량 유효성 검증
            if (request.getQuantity() <= 0) {
                response.put("success", false);
                response.put("message", "수량은 1 이상이어야 합니다.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            log.info("장바구니 수량 수정 요청 - 사용자 ID: {}, 아이템 ID: {}, 새 수량: {}", 
                    user.getId(), cartItemId, request.getQuantity());

            // 4. 수량 업데이트
            CartItem updatedItem = cartService.updateQuantity(cartItemId, request.getQuantity());

            // 5. 성공 응답
            response.put("success", true);
            response.put("message", "장바구니 아이템 수량이 수정되었습니다.");
            response.put("data", updatedItem);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("장바구니 수량 수정 중 오류: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "장바구니 수량을 수정하는 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 장바구니 아이템 삭제
     *
     * ========================================
     * 📌 API 정보
     * ========================================
     * - HTTP Method: DELETE
     * - URL: /api/cart/{cartItemId}
     * - 인증: 필수 (JWT 토큰)
     *
     * ========================================
     * 📌 처리 흐름
     * ========================================
     * 1. JWT 토큰 검증 및 사용자 확인
     * 2. CartService.removeItem() 호출
     * 3. 성공 응답 반환
     *
     * ========================================
     * 📌 프론트엔드 호출
     * ========================================
     * cartAPI.removeItem(cartItemId)
     * → Cart.js "X" 버튼, CartSidePanel.js
     */
    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<Map<String, Object>> removeItem(
            @PathVariable Long cartItemId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 1. JWT 토큰 추출 및 검증
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            String token = authHeader.substring(7);
            Map<String, Object> claims = jwtUtil.validateToken(token);
            String userId = (String) claims.get("userId");
            
            // 2. 사용자 조회
            User user = userRepository.findByUserId(userId)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

            log.info("장바구니 아이템 삭제 요청 - 사용자 ID: {}, 아이템 ID: {}", 
                    user.getId(), cartItemId);

            // 3. 아이템 삭제
            cartService.removeItem(cartItemId);

            // 4. 성공 응답
            response.put("success", true);
            response.put("message", "장바구니에서 상품이 제거되었습니다.");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("장바구니 아이템 삭제 중 오류: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "장바구니에서 상품을 제거하는 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 장바구니 전체 비우기
     *
     * ========================================
     * 📌 API 정보
     * ========================================
     * - HTTP Method: DELETE
     * - URL: /api/cart/clear/all
     * - 인증: 필수 (JWT 토큰)
     *
     * ========================================
     * 📌 처리 흐름
     * ========================================
     * 1. JWT 토큰 검증 및 사용자 확인
     * 2. CartService.clearCart() 호출
     * 3. 성공 응답 반환
     *
     * ========================================
     * 📌 사용 시나리오
     * ========================================
     * - "장바구니 비우기" 버튼 클릭
     * - 주문 완료 후 장바구니 정리
     *
     * ========================================
     * 📌 프론트엔드 호출
     * ========================================
     * cartAPI.clearCart()
     */
    @DeleteMapping("/clear/all")
    public ResponseEntity<Map<String, Object>> clearCart(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 1. JWT 토큰 추출 및 검증
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            String token = authHeader.substring(7);
            Map<String, Object> claims = jwtUtil.validateToken(token);
            String userId = (String) claims.get("userId");
            
            // 2. 사용자 조회
            User user = userRepository.findByUserId(userId)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

            log.info("장바구니 전체 비우기 요청 - 사용자 ID: {}", user.getId());

            // 3. 장바구니 전체 삭제
            cartService.clearCart(user.getId());

            // 4. 성공 응답
            response.put("success", true);
            response.put("message", "장바구니가 비워졌습니다.");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("장바구니 전체 비우기 중 오류: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "장바구니를 비우는 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 장바구니 담기 요청 DTO
     *
     * ========================================
     * 📌 필드 설명
     * ========================================
     * - productId: 담을 상품의 고유 ID (필수)
     * - quantity: 담을 수량 (필수, 1 이상)
     *
     * ========================================
     * 📌 요청 JSON 예시
     * ========================================
     * {
     *   "productId": 1,
     *   "quantity": 2
     * }
     */
    public static class AddToCartRequest {
        /** 담을 상품의 고유 ID */
        private Long productId;

        /** 담을 수량 (최소 1) */
        private int quantity;

        public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
    }

    /**
     * 수량 수정 요청 DTO
     *
     * ========================================
     * 📌 필드 설명
     * ========================================
     * - quantity: 변경할 수량 (1 이상)
     *
     * ========================================
     * 📌 요청 JSON 예시
     * ========================================
     * {
     *   "quantity": 5
     * }
     */
    public static class UpdateQuantityRequest {
        /** 변경할 수량 (최소 1) */
        private int quantity;

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
    }
}

/*
 * ========================================
 * 📌 Cart & Favorite 전체 아키텍처 다이어그램
 * ========================================
 *
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │                              CLIENT (Frontend)                              │
 * │                                                                             │
 * │  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────────────────┐  │
 * │  │   Cart.js       │  │ CartSidePanel.js│  │ CartFloatingButton.js       │  │
 * │  │ (장바구니 페이지) │  │ (사이드 패널)    │  │ (플로팅 버튼 + 배지)         │  │
 * │  └────────┬────────┘  └────────┬────────┘  └─────────────┬───────────────┘  │
 * │           │                    │                         │                  │
 * │           └────────────────────┼─────────────────────────┘                  │
 * │                                ↓                                            │
 * │  ┌─────────────────────────────────────────────────────────────────────┐    │
 * │  │                        src/api/cartApi.js                           │    │
 * │  │  - addToCart(productId, quantity)                                   │    │
 * │  │  - getCartItems()                                                   │    │
 * │  │  - getCartCount()                                                   │    │
 * │  │  - updateQuantity(cartItemId, quantity)                             │    │
 * │  │  - removeItem(cartItemId)                                           │    │
 * │  │  - clearCart()                                                      │    │
 * │  └─────────────────────────────┬───────────────────────────────────────┘    │
 * │                                │                                            │
 * │  ┌─────────────────────────────────────────────────────────────────────┐    │
 * │  │                      src/api/axiosConfig.js                         │    │
 * │  │  - Request Interceptor: Authorization 헤더에 JWT 토큰 자동 첨부       │    │
 * │  │  - Response Interceptor: 401 에러 시 토큰 갱신 시도                   │    │
 * │  └─────────────────────────────┬───────────────────────────────────────┘    │
 * │                                │                                            │
 * │  ┌─────────────────────────────────────────────────────────────────────┐    │
 * │  │                     Redux Store (cartSlice.js)                      │    │
 * │  │  State: { items, totalItems, totalPrice, loading, error }           │    │
 * │  │  Actions: setCartItems, addCartItem, updateCartItem, removeCartItem │    │
 * │  └─────────────────────────────────────────────────────────────────────┘    │
 * └─────────────────────────────────────────────────────────────────────────────┘
 *                                  │
 *                                  │ HTTP Request + JWT Token
 *                                  │ (Authorization: Bearer <token>)
 *                                  ↓
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │                           SERVER (Backend)                                  │
 * │                                                                             │
 * │  ┌─────────────────────────────────────────────────────────────────────┐    │
 * │  │                    REST Controller Layer                            │    │
 * │  │                                                                     │    │
 * │  │  ┌─────────────────────────┐  ┌──────────────────────────────────┐  │    │
 * │  │  │  CartRestController     │  │  FavoriteRestController          │  │    │
 * │  │  │  /api/cart/*            │  │  /api/favorites/*                │  │    │
 * │  │  │                         │  │                                  │  │    │
 * │  │  │  POST /add              │  │  GET /                           │  │    │
 * │  │  │  GET /                  │  │  POST /toggle                    │  │    │
 * │  │  │  GET /count             │  │  GET /check/{productId}          │  │    │
 * │  │  │  PUT /{cartItemId}      │  │  GET /count                      │  │    │
 * │  │  │  DELETE /{cartItemId}   │  │  GET /count/product/{productId}  │  │    │
 * │  │  │  DELETE /clear/all      │  │  DELETE /product/{productId}     │  │    │
 * │  │  └────────────┬────────────┘  └───────────────┬──────────────────┘  │    │
 * │  │               │                               │                     │    │
 * │  │               │         ┌─────────────────────┘                     │    │
 * │  │               │         │                                           │    │
 * │  │               │    ┌────┴────┐                                      │    │
 * │  │               │    │ JWTUtil │  JWT 토큰 검증                        │    │
 * │  │               │    └────┬────┘                                      │    │
 * │  │               │         │                                           │    │
 * │  └───────────────┼─────────┼───────────────────────────────────────────┘    │
 * │                  ↓         ↓                                                │
 * │  ┌─────────────────────────────────────────────────────────────────────┐    │
 * │  │                      Service Layer                                  │    │
 * │  │                                                                     │    │
 * │  │  ┌─────────────────────────┐  ┌──────────────────────────────────┐  │    │
 * │  │  │     CartService         │  │     FavoriteService              │  │    │
 * │  │  │                         │  │                                  │  │    │
 * │  │  │  - getCartItems()       │  │  - getFavoritesByUserId()        │  │    │
 * │  │  │  - addToCart()          │  │  - toggleFavorite()              │  │    │
 * │  │  │  - updateQuantity()     │  │  - addFavorite()                 │  │    │
 * │  │  │  - removeItem()         │  │  - removeFavorite()              │  │    │
 * │  │  │  - clearCart()          │  │  - isFavorite()                  │  │    │
 * │  │  │                         │  │  - getFavoriteCountByProductId() │  │    │
 * │  │  │  @Transactional 적용     │  │  - getFavoriteCountByUserId()    │  │    │
 * │  │  └────────────┬────────────┘  └───────────────┬──────────────────┘  │    │
 * │  │               │                               │                     │    │
 * │  └───────────────┼───────────────────────────────┼─────────────────────┘    │
 * │                  ↓                               ↓                          │
 * │  ┌─────────────────────────────────────────────────────────────────────┐    │
 * │  │                     Repository Layer                                │    │
 * │  │                                                                     │    │
 * │  │  ┌─────────────────────────┐  ┌──────────────────────────────────┐  │    │
 * │  │  │  CartItemRepository     │  │  FavoriteRepository              │  │    │
 * │  │  │  extends JpaRepository  │  │  extends JpaRepository           │  │    │
 * │  │  │                         │  │                                  │  │    │
 * │  │  │  - findByUser()         │  │  - findByUserIdOrderByCreatedAt  │  │    │
 * │  │  │  - findByUserAndProduct │  │  - findByUserIdAndProductId()    │  │    │
 * │  │  │  - deleteByUser()       │  │  - existsByUserIdAndProductId()  │  │    │
 * │  │  │  - deleteByProduct()    │  │  - countByProductId()            │  │    │
 * │  │  │  - findByProduct()      │  │  - countByUserId()               │  │    │
 * │  │  └────────────┬────────────┘  └───────────────┬──────────────────┘  │    │
 * │  │               │                               │                     │    │
 * │  │               │    ┌──────────────────────────┤                     │    │
 * │  │               │    │                          │                     │    │
 * │  │  ┌────────────┴────┴──────────────────────────┴──────────────────┐  │    │
 * │  │  │              공통 Repository                                  │  │    │
 * │  │  │  - UserRepository: 사용자 정보 조회                            │  │    │
 * │  │  │  - ProductRepository: 상품 정보 조회                          │  │    │
 * │  │  └───────────────────────────────────────────────────────────────┘  │    │
 * │  └─────────────────────────────────────────────────────────────────────┘    │
 * │                  │                               │                          │
 * └──────────────────┼───────────────────────────────┼──────────────────────────┘
 *                    ↓                               ↓
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │                           DATABASE (MySQL)                                  │
 * │                                                                             │
 * │  ┌─────────────────────────┐  ┌──────────────────────────────────────────┐  │
 * │  │      cart_item          │  │            favorite                      │  │
 * │  │  ┌──────────────────┐   │  │  ┌────────────────────────────────────┐  │  │
 * │  │  │ id (PK)          │   │  │  │ id (PK)                            │  │  │
 * │  │  │ user_id (FK)     │───┤  │  │ user_id (FK)                       │──┤  │
 * │  │  │ product_id (FK)  │───┤  │  │ product_id (FK)                    │──┤  │
 * │  │  │ quantity         │   │  │  │ created_at                         │  │  │
 * │  │  └──────────────────┘   │  │  │ UNIQUE(user_id, product_id)        │  │  │
 * │  │                         │  │  └────────────────────────────────────┘  │  │
 * │  └────────────┬────────────┘  └──────────────────┬───────────────────────┘  │
 * │               │                                  │                          │
 * │               │         ┌────────────────────────┘                          │
 * │               ↓         ↓                                                   │
 * │  ┌──────────────────────────────┐  ┌──────────────────────────────────────┐ │
 * │  │          user                │  │            product                   │ │
 * │  │  ┌────────────────────────┐  │  │  ┌────────────────────────────────┐  │ │
 * │  │  │ id (PK)                │  │  │  │ id (PK)                        │  │ │
 * │  │  │ user_id (아이디)        │  │  │  │ name (상품명)                   │  │ │
 * │  │  │ username               │  │  │  │ price (가격)                    │  │ │
 * │  │  │ email                  │  │  │  │ sale_price (할인가)              │  │ │
 * │  │  │ ...                    │  │  │  │ thumbnail_image (이미지)         │  │ │
 * │  │  └────────────────────────┘  │  │  │ ...                            │  │ │
 * │  └──────────────────────────────┘  │  └────────────────────────────────┘  │ │
 * │                                    └──────────────────────────────────────┘ │
 * └─────────────────────────────────────────────────────────────────────────────┘
 *
 * ========================================
 * 📌 데이터 흐름 예시: 장바구니 담기
 * ========================================
 *
 * 1. [Frontend] 사용자가 "장바구니 담기" 버튼 클릭
 *    ↓
 * 2. [cartApi.js] addToCart(productId, quantity) 호출
 *    ↓
 * 3. [axiosConfig.js] Request Interceptor가 JWT 토큰을 헤더에 추가
 *    ↓
 * 4. [HTTP] POST /api/cart/add { productId: 1, quantity: 2 }
 *    ↓
 * 5. [CartRestController] addToCart() 메서드 실행
 *    - JWT 토큰 검증 → userId 추출
 *    - User 엔티티 조회
 *    - 입력값 검증
 *    ↓
 * 6. [CartService] addToCart(userId, productId, qty) 호출
 *    - User, Product 엔티티 조회
 *    - 기존 CartItem 확인 (findByUserAndProduct)
 *    - 있으면 수량 증가, 없으면 새로 생성
 *    ↓
 * 7. [CartItemRepository] save(cartItem)
 *    ↓
 * 8. [Database] INSERT/UPDATE cart_item 테이블
 *    ↓
 * 9. [Response] { success: true, message: "...", data: CartItem }
 *    ↓
 * 10. [Frontend] 장바구니 UI 업데이트, 배지 개수 갱신
 *
 */
