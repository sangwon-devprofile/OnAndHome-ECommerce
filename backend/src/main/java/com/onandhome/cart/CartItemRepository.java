package com.onandhome.cart;

import com.onandhome.cart.entity.CartItem;
import com.onandhome.admin.adminProduct.entity.Product;
import com.onandhome.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 장바구니 아이템 리포지토리
 *
 * ========================================
 * 📌 인터페이스 개요
 * ========================================
 * - 상속: JpaRepository<CartItem, Long>
 * - 역할: CartItem 엔티티에 대한 데이터 액세스 계층
 * - 기본 제공 메서드: save(), findById(), findAll(), deleteById() 등
 *
 * ========================================
 * 📌 쿼리 메서드 명명 규칙
 * ========================================
 * Spring Data JPA는 메서드 이름을 분석하여 자동으로 쿼리를 생성합니다.
 *
 * | 메서드명                    | 생성되는 쿼리 (JPQL)                                    |
 * |---------------------------|-------------------------------------------------------|
 * | findByUser(user)          | SELECT c FROM CartItem c WHERE c.user = :user         |
 * | findByUserAndProduct(...) | SELECT c FROM CartItem c WHERE c.user = :user         |
 * |                           |   AND c.product = :product                            |
 * | deleteByUser(user)        | DELETE FROM CartItem c WHERE c.user = :user           |
 * | deleteByProduct(product)  | DELETE FROM CartItem c WHERE c.product = :product     |
 * | findByProduct(product)    | SELECT c FROM CartItem c WHERE c.product = :product   |
 *
 * ========================================
 * 📌 사용처 (CartService 메서드)
 * ========================================
 * - findByUser() → getCartItems(): 사용자의 장바구니 목록 조회
 * - findByUserAndProduct() → addToCart(): 중복 상품 확인
 * - deleteByUser() → clearCart(): 장바구니 전체 비우기
 * - deleteByProduct() → (관리자) 상품 삭제 시 장바구니에서도 제거
 * - findByProduct() → (관리자) 특정 상품이 담긴 장바구니 조회
 */
@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    /**
     * 특정 사용자의 장바구니 전체 조회
     *
     * @param user 조회할 사용자 엔티티
     * @return 해당 사용자의 CartItem 리스트
     *
     * 사용 예시:
     * - CartService.getCartItems(userId)
     * - 장바구니 페이지에서 상품 목록 표시
     *
     * 생성 쿼리: SELECT * FROM cart_item WHERE user_id = ?
     */
    List<CartItem> findByUser(User user);

    /**
     * 특정 사용자의 장바구니 전체 삭제
     *
     * @param user 삭제할 사용자 엔티티
     *
     * 사용 예시:
     * - CartService.clearCart(userId)
     * - 주문 완료 후 장바구니 비우기
     * - 회원 탈퇴 시 장바구니 데이터 정리
     *
     * 주의: @Transactional 필수 (벌크 삭제 연산)
     *
     * 생성 쿼리: DELETE FROM cart_item WHERE user_id = ?
     */
    void deleteByUser(User user);

    /**
     * 특정 사용자의 특정 상품 장바구니 아이템 조회
     *
     * @param user 사용자 엔티티
     * @param product 상품 엔티티
     * @return CartItem (없으면 Optional.empty())
     *
     * 사용 예시:
     * - CartService.addToCart(): 이미 담긴 상품인지 확인
     *   → 있으면 수량 증가, 없으면 새로 추가
     *
     * 생성 쿼리: SELECT * FROM cart_item
     *           WHERE user_id = ? AND product_id = ?
     */
    Optional<CartItem> findByUserAndProduct(User user, Product product);

    /**
     * 특정 상품이 담긴 모든 장바구니 아이템 삭제
     *
     * @param product 삭제할 상품 엔티티
     *
     * 사용 예시:
     * - 관리자가 상품을 삭제할 때
     * - 해당 상품이 담긴 모든 사용자의 장바구니에서 제거
     *
     * 주의: @Transactional 필수 (벌크 삭제 연산)
     *
     * 생성 쿼리: DELETE FROM cart_item WHERE product_id = ?
     */
    void deleteByProduct(Product product);

    /**
     * 특정 상품이 담긴 모든 장바구니 아이템 조회
     *
     * @param product 조회할 상품 엔티티
     * @return 해당 상품이 담긴 CartItem 리스트
     *
     * 사용 예시:
     * - 특정 상품의 장바구니 담김 현황 조회
     * - 상품 삭제 전 영향 받는 사용자 파악
     *
     * 생성 쿼리: SELECT * FROM cart_item WHERE product_id = ?
     */
    List<CartItem> findByProduct(Product product);
}
