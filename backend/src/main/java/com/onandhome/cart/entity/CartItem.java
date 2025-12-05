package com.onandhome.cart.entity;

import com.onandhome.admin.adminProduct.entity.Product;
import com.onandhome.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

/**
 * ========================================
 * 📌 클래스 개요
 * ========================================
 * - 역할: 사용자가 장바구니에 담은 상품 정보를 저장
 * - 관계: User(N:1), Product(N:1)
 * ========================================

 * ========================================
 * 📌 연관관계
 * ========================================
 * - User (N:1): 한 사용자는 여러 장바구니 아이템을 가질 수 있음
 * - Product (N:1): 한 상품은 여러 사용자의 장바구니에 담길 수 있음
 *
 * ========================================
 * 📌 FetchType 설명
 * ========================================
 * - EAGER 로딩 사용: 장바구니 조회 시 상품/사용자 정보가 항상 필요하므로
 *   즉시 로딩하여 N+1 문제 방지 및 성능 최적화
 *
 * ========================================
 **/
    @Entity
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @Table(name = "cart_item")
    public class CartItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false, fetch = FetchType.EAGER)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(optional = false, fetch = FetchType.EAGER)
	@JoinColumn(name = "product_id", nullable = false)
	private Product product;

	@Column(nullable = false)
	private int quantity;

}
