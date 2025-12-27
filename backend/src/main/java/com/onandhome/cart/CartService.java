package com.onandhome.cart;

import java.util.List;
import java.util.Optional;

import com.onandhome.cart.entity.CartItem;
import com.onandhome.admin.adminProduct.entity.Product;
import com.onandhome.admin.adminProduct.ProductRepository;
import com.onandhome.user.UserRepository;
import com.onandhome.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 장바구니 서비스
 *
 * ========================================
 * 📌 클래스 개요
 * ========================================
 * - 역할: 장바구니 관련 비즈니스 로직 처리
 * - 계층: Service Layer (Controller ↔ Repository 중간)
 *
 * ========================================
 * 📌 제공 기능
 * ========================================
 * | 메서드명         | 기능                | 트랜잭션 |
 * |-----------------|--------------------|---------:|
 * | getCartItems()  | 장바구니 목록 조회   | X (읽기) |
 * | addToCart()     | 장바구니 담기        | O        |
 * | updateQuantity()| 수량 변경           | O        |
 * | removeItem()    | 아이템 삭제         | O        |
 * | clearCart()     | 전체 비우기         | O        |
 *
 * ========================================
 * 📌 의존성
 * ========================================
 * - CartItemRepository: 장바구니 아이템 데이터 액세스
 * - ProductRepository: 상품 정보 조회
 * - UserRepository: 사용자 정보 조회
 *
 * ========================================
 * 📌 트랜잭션 관리
 * ========================================
 * - @Transactional: 데이터 변경 작업에 적용
 * - 예외 발생 시 자동 롤백
 * - 여러 DB 작업의 원자성(Atomicity) 보장
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class CartService {

    /** 장바구니 아이템 데이터 액세스 리포지토리 */
    private final CartItemRepository cartRepo;

    /** 상품 정보 조회 리포지토리 */
    private final ProductRepository productRepo;

    /** 사용자 정보 조회 리포지토리 */
    private final UserRepository userRepo;


    /**
     * 사용자의 장바구니 목록 조회
     *
     * ========================================
     * 📌 처리 흐름
     * ========================================
     * 1. userId로 User 엔티티 조회
     * 2. User가 없으면 빈 리스트 반환
     * 3. CartItemRepository.findByUser()로 장바구니 조회
     *
     * ========================================
     * 📌 반환 데이터
     * ========================================
     * - CartItem 리스트 (EAGER 로딩으로 Product 정보 포함)
     * - 빈 장바구니면 빈 리스트 반환 (null 아님)
     *
     * @param userId 사용자 PK (user.id)
     * @return 장바구니 아이템 리스트
     */
    public List<CartItem> getCartItems(Long userId) {
        // 사용자 조회 (없으면 빈 리스트 반환)
        User user = userRepo.findById(userId).orElse(null);
        if (user == null)
            return List.of();

        // 사용자의 장바구니 전체 조회
        return cartRepo.findByUser(user);
    }

    /**
     * 장바구니에 상품 담기
     *
     * ========================================
     * 📌 처리 흐름
     * ========================================
     * 1. userId로 User 엔티티 조회 (없으면 예외)
     * 2. productId로 Product 엔티티 조회 (없으면 예외)
     * 3. 기존에 같은 상품이 장바구니에 있는지 확인
     *    - 있으면: 기존 수량 + 요청 수량
     *    - 없으면: 새 CartItem 생성
     * 4. 저장 후 CartItem 반환
     *
     * ========================================
     * 📌 수량 처리
     * ========================================
     * - Math.max(qty, 1): 최소 수량 1 보장
     * - 음수나 0이 입력되어도 1로 처리됨
     *
     * ========================================
     * 📌 중복 상품 처리
     * ========================================
     * - 이미 장바구니에 있는 상품 → 수량만 증가
     * - 새 CartItem을 생성하지 않음 (중복 방지)
     *
     * @param userId 사용자 PK
     * @param productId 상품 PK
     * @param qty 담을 수량
     * @return 저장된 CartItem
     * @throws IllegalArgumentException 사용자 또는 상품이 없는 경우
     */
    @Transactional
    public CartItem addToCart(Long userId, Long productId, int qty) {
        log.info("장바구니 담기 시작 - userId: {}, productId: {}, qty: {}", userId, productId, qty);

        // 1. 사용자 조회 (필수)
        User user = userRepo.findById(userId).orElseThrow(() -> 
            new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 2. 상품 조회 (필수)
        Product p = productRepo.findById(productId).orElseThrow(() -> 
            new IllegalArgumentException("상품을 찾을 수 없습니다. ID: " + productId));
        
        log.info("사용자 조회 성공: {}, 상품 조회 성공: {}", user.getId(), p.getId());

        // 3. 이미 장바구니에 있는 상품인지 확인
        Optional<CartItem> existing = cartRepo.findByUserAndProduct(user, p);
        if (existing.isPresent()) {
            // 3-1. 기존 아이템이 있으면 수량 증가
            CartItem item = existing.get();
            int newQty = item.getQuantity() + Math.max(qty, 1);
            item.setQuantity(newQty);
            log.info("기존 장바구니 아이템 수량 증가: {}", newQty);
            return cartRepo.save(item);
        }

        // 3-2. 새로운 아이템 추가
        CartItem item = new CartItem();
        item.setUser(user);
        item.setProduct(p);
        item.setQuantity(Math.max(qty, 1));  // 최소 수량 1 보장
        log.info("새로운 장바구니 아이템 생성 - quantity: {}", item.getQuantity());
        return cartRepo.save(item);
    }

    /**
     * 장바구니 아이템 수량 변경
     *
     * ========================================
     * 📌 처리 흐름
     * ========================================
     * 1. cartItemId로 CartItem 조회 (없으면 예외)
     * 2. 수량 업데이트 (최소 1 보장)
     * 3. 저장 후 반환
     *
     * ========================================
     * 📌 프론트엔드 연동
     * ========================================
     * - Cart.js: +/- 버튼으로 수량 조절
     * - CartSidePanel.js: 사이드 패널에서 수량 변경
     *
     * @param cartItemId 장바구니 아이템 PK
     * @param quantity 변경할 수량
     * @return 업데이트된 CartItem
     */
    @Transactional
    public CartItem updateQuantity(Long cartItemId, int quantity) {
        // 장바구니 아이템 조회
        CartItem item = cartRepo.findById(cartItemId).orElseThrow();

        // 수량 업데이트 (최소 1 보장)
        item.setQuantity(Math.max(quantity, 1));
        return cartRepo.save(item);
    }

    /**
     * 장바구니 아이템 삭제
     *
     * ========================================
     * 📌 처리 흐름
     * ========================================
     * 1. cartItemId로 해당 아이템 삭제
     *
     * ========================================
     * 📌 사용 시나리오
     * ========================================
     * - 장바구니에서 개별 상품 삭제
     * - "X" 버튼 클릭 시 호출
     *
     * @param cartItemId 삭제할 장바구니 아이템 PK
     */
    @Transactional
    public void removeItem(Long cartItemId) {
        cartRepo.deleteById(cartItemId);
    }

    /**
     * 사용자의 장바구니 전체 비우기
     *
     * ========================================
     * 📌 처리 흐름
     * ========================================
     * 1. userId로 User 엔티티 조회 (없으면 예외)
     * 2. 해당 사용자의 모든 CartItem 삭제
     *
     * ========================================
     * 📌 사용 시나리오
     * ========================================
     * - "장바구니 비우기" 버튼 클릭
     * - 주문 완료 후 장바구니 정리
     *
     * @param userId 사용자 PK
     */
    @Transactional
    public void clearCart(Long userId) {
        // 사용자 조회 (필수)
        User user = userRepo.findById(userId).orElseThrow();

        // 사용자의 모든 장바구니 아이템 삭제
        cartRepo.deleteByUser(user);
    }
}
