package com.onandhome.admin.adminProduct;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.onandhome.admin.adminProduct.entity.Product;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    // interface: 인터페이스 선언 (구현체는 Spring Data JPA가 자동 생성)
    // extends JpaRepository<Product, Long>: JpaRepository 인터페이스 상속
    //   - Product: 이 Repository가 다루는 엔티티 타입
    //   - Long: 엔티티의 기본 키(Primary Key) 타입

    // JpaRepository가 제공하는 기본 메서드들:
    // - findAll(): 모든 레코드 조회
    // - findById(Long id): ID로 단일 레코드 조회
    // - save(Product product): 레코드 저장/수정
    // - delete(Product product): 레코드 삭제
    // - count(): 전체 레코드 개수
    // - existsById(Long id): ID 존재 여부 확인

    // findAll() 메서드 실행 시:
    // 1. JPA가 자동으로 SQL 생성: SELECT * FROM product;
    // 2. MySQL DB에 쿼리 전송
    // 3. 결과를 Product 엔티티 객체로 변환
    // 4. List<Product>로 반환
    
    /**
     * 상품명으로 검색
     */
    List<Product> findByNameContainingIgnoreCase(String keyword);
    
    /**
     * 카테고리별 상품 조회
     */
    List<Product> findByCategory(String category);
    
    /**
     * 카테고리명 포함 상품 조회
     */
    List<Product> findByCategoryContaining(String category);
    
    /**
     * 품절 상품 개수 조회 (stock = 0)
     */
    @Query("SELECT COUNT(p) FROM Product p WHERE p.stock = 0")
    long countOutOfStockProducts();

    /**
     * 재고가 있는 상품만 조회 (사용자용)
     */
    @Query("SELECT p FROM Product p WHERE p.stock > 0")
    List<Product> findAllInStock();

    /**
     * 카테고리별 재고가 있는 상품만 조회 (사용자용)
     */
    @Query("SELECT p FROM Product p WHERE p.category = :category AND p.stock > 0")
    List<Product> findByCategoryInStock(@Param("category") String category);

    /**
     * 상품명 검색 - 재고가 있는 상품만 (사용자용)
     */
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) AND p.stock > 0")
    List<Product> findByNameContainingIgnoreCaseInStock(@Param("keyword") String keyword);
}
