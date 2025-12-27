package com.onandhome.admin.adminProduct.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Table(name = "product")
public class Product {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "product_code", unique = true, length = 50)
	@JsonProperty("productCode")
	private String productCode; // 상품코드 (PC-001, PC-002 등)

	@Column(nullable = false)
	@JsonProperty("name")
	private String name;

	@Column(length = 1000)
	@JsonProperty("description")
	private String description;

	@Column(nullable = false)
	@JsonProperty("price")
	private int price; // 정상가 (원 단위)

	@Column(name = "sale_price")
	@JsonProperty("salePrice")
	private Integer salePrice; // 할인가 (원 단위)

	@Column(nullable = false)
	@JsonProperty("stock")
	private int stock; // 재고수량

	@Column(name = "thumbnail_image")
	@JsonProperty("thumbnailImage")
	private String thumbnailImage; // 썸네일 이미지 URL (컬럼명: thumbnail_image)

	@Column(name = "detail_image")
	@JsonProperty("detailImage")
	private String detailImage; // 상품 상세 이미지 URL (컬럼명: detail_image)

	@Column(name = "category")
	@JsonProperty("category")
	private String category; // 소 카테고리 (TV, 오디오, 냉장고 등)

	@Column(name = "manufacturer")
	@JsonProperty("manufacturer")
	private String manufacturer; // 제조사

	@Column(name = "country")
	@JsonProperty("country")
	private String country; // 제조국

	@Column(length = 20)
	@JsonProperty("status")
	@Builder.Default
	private String status = "판매중"; // 상품 상태 (판매중, 품절, 판매중지 등)

	@Column(name = "created_at")
	@JsonProperty("createdAt")
	private LocalDateTime createdAt; // 등록일자

	@Column(name = "updated_at")
	@JsonProperty("updatedAt")
	private LocalDateTime updatedAt; // 수정일자

	@PrePersist
	protected void onCreate() {
		this.createdAt = LocalDateTime.now();
		this.updatedAt = LocalDateTime.now();
	}

	@PreUpdate
	protected void onUpdate() {
		this.updatedAt = LocalDateTime.now();
	}

	/**
	 * 재고 차감 (주문 시)
	 */
	public void removeStock(int quantity) {
		if (this.stock < quantity) {
			throw new IllegalArgumentException("재고가 부족합니다. 현재 재고: " + this.stock);
		}
		this.stock -= quantity;
	}

	/**
	 * 재고 증가 (주문 취소 시)
	 */
	public void addStock(int quantity) {
		this.stock += quantity;
	}
}
