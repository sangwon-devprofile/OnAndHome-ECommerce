package com.onandhome.admin.adminProduct;

import java.util.List;
import java.util.Optional;

import com.onandhome.admin.adminProduct.dto.ProductDTO;
import com.onandhome.admin.adminProduct.entity.Product;
import com.onandhome.cart.CartItemRepository;
import com.onandhome.order.OrderItemRepository;
import com.onandhome.qna.QnaRepository;
import com.onandhome.review.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
// @Service: 이 클래스가 서비스 계층임을 Spring에게 알림
// 비즈니스 로직을 처리하는 컴포넌트
@RequiredArgsConstructor
// final 필드에 대한 생성자 자동 생성
@Slf4j
@Transactional
// @Transactional: 클래스의 모든 public 메서드에 트랜잭션 적용
// 메서드 실행 중 예외 발생 시 자동 롤백
// 데이터베이스 작업의 원자성 보장
public class ProductService {
    private final ProductRepository productRepository;
    // productRepository: DB와 통신하는 Repository 객체
    // final: 생성자 주입 시 할당됨
    // Spring이 자동으로 ProductRepository 구현체를 주입
    private final CartItemRepository cartItemRepository;
    private final OrderItemRepository orderItemRepository;
    private final ReviewRepository reviewRepository;
    private final QnaRepository qnaRepository;

    /**
     * 모든 상품 조회
     */
    public List<Product> listAll() {
        // listAll(): 모든 상품을 조회하는 메서드
        // List<Product>: 반환 타입 (Product 엔티티의 리스트)
        List<Product> products = productRepository.findAll();
        // productRepository.findAll(): JPA가 제공하는 메서드
        // product 테이블의 모든 레코드를 조회
        // SELECT * FROM product; 쿼리가 자동으로 실행됨
        // 결과: List<Product> (각 행이 Product 객체로 변환됨)
        
        // 이미지 경로 로깅
        log.info("=== 전체 상품 조회 - 총 {} 개 ===", products.size());
        products.forEach(product -> {
            // forEach(): 리스트의 각 요소에 대해 람다 함수 실행
            // product -> {}: 람다 표현식
            // product: 리스트의 각 Product 객체
            log.info("상품 ID: {}, 이름: {}, thumbnailImage: {}", 
                product.getId(), product.getName(), product.getThumbnailImage());
        });
        
        return products;
        // 조회된 상품 리스트를 컨트롤러로 반환
    }

    /**
     * ID로 상품 조회
     */
    @Transactional(readOnly = true)
    public Optional<ProductDTO> getById(Long id) {
        // @Transactional(readOnly = true): 읽기 전용 트랜잭션
        Optional<Product> product = productRepository.findById(id);
        // productRepository.findById(id): JPA가 자동으로 SQL 실행
        // SELECT * FROM product WHERE id = 423;

        if (product.isPresent()) {
            ProductDTO productDTO = ProductDTO.fromEntity(product.get());
            // Product 엔티티를 ProductDTO로 변환

            // 평균 별점 및 리뷰 개수 계산
            Double avgRating = reviewRepository.findAverageRatingByProductId(id);
            Long reviewCount = reviewRepository.countByProductId(id);

            productDTO.setAverageRating(avgRating != null ? avgRating : 0.0);
            productDTO.setReviewCount(reviewCount != null ? reviewCount : 0L);
            // DTO에 평균 별점과 리뷰 개수 설정

            return Optional.of(productDTO);
        }
        return Optional.empty();
        // 상품이 없으면 빈 Optional 반환
    }

    /**
     * 카테고리별 상품 조회
     */
    @Transactional(readOnly = true)
    public List<Product> getByCategory(String category) {
        log.debug("카테고리별 상품 조회: {}", category);
        List<Product> products = productRepository.findByCategory(category);
        
        // 이미지 경로 로깅
        log.info("=== 카테고리 '{}' 상품 조회 - 총 {} 개 ===", category, products.size());
        products.forEach(product -> {
            log.info("상품 ID: {}, 이름: {}, thumbnailImage: {}", 
                product.getId(), product.getName(), product.getThumbnailImage());
        });
        
        return products;
    }

    /**
     * 상품 생성 (DTO 사용)
     */
    public ProductDTO create(ProductDTO productDTO) {
        if (productDTO.getName() == null || productDTO.getName().isEmpty()) {
            throw new IllegalArgumentException("상품명은 필수입니다.");
        }

        if (productDTO.getPrice() < 0) {
            throw new IllegalArgumentException("가격은 0 이상이어야 합니다.");
        }

        if (productDTO.getStock() < 0) {
            throw new IllegalArgumentException("재고는 0 이상이어야 합니다.");
        }

        Product product = productDTO.toEntity();
        Product savedProduct = productRepository.save(product);

        log.info("상품 생성: {} (ID: {})", productDTO.getName(), savedProduct.getId());
        return ProductDTO.fromEntity(savedProduct);
    }

    /**
     * 상품 수정
     */
    public ProductDTO update(Long id, ProductDTO productDTO) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다."));

        if (productDTO.getName() != null && !productDTO.getName().isEmpty()) {
            product.setName(productDTO.getName());
        }
        if (productDTO.getDescription() != null) {
            product.setDescription(productDTO.getDescription());
        }
        if (productDTO.getPrice() >= 0) {
            product.setPrice(productDTO.getPrice());
        }
        if (productDTO.getSalePrice() != null && productDTO.getSalePrice() >= 0) {
            product.setSalePrice(productDTO.getSalePrice());
        }
        if (productDTO.getStock() >= 0) {
            product.setStock(productDTO.getStock());
        }
        if (productDTO.getThumbnailImage() != null) {
            product.setThumbnailImage(productDTO.getThumbnailImage());
        }
        if (productDTO.getDetailImage() != null) {
            product.setDetailImage(productDTO.getDetailImage());
        }
        if (productDTO.getCategory() != null) {
            product.setCategory(productDTO.getCategory());
        }
        if (productDTO.getManufacturer() != null) {
            product.setManufacturer(productDTO.getManufacturer());
        }
        if (productDTO.getCountry() != null) {
            product.setCountry(productDTO.getCountry());
        }
        if (productDTO.getStatus() != null) {
            product.setStatus(productDTO.getStatus());
        }

        Product updatedProduct = productRepository.save(product);
        log.info("상품 수정: {} (ID: {})", id, updatedProduct.getName());

        return ProductDTO.fromEntity(updatedProduct);
    }

    /**
     * 상품 상태 변경
     */
    public void updateStatus(Long id, String status) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다."));
        
        product.setStatus(status);
        productRepository.save(product);
        log.info("상품 상태 변경: {} -> {}", id, status);
    }

    /**
     * 상품 삭제
     * 상품 삭제 전에 관련된 모든 데이터를 먼저 삭제합니다.
     */
    public void delete(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다."));

        log.info("상품 삭제 시작: {} (ID: {})", product.getName(), id);

        try {
            // 1. 장바구니 아이템 삭제
            cartItemRepository.deleteByProduct(product);
            log.debug("장바구니 아이템 삭제 완료");

            // 2. 리뷰 삭제
            reviewRepository.deleteByProduct(product);
            log.debug("리뷰 삭제 완료");

            // 3. QnA 삭제
            qnaRepository.deleteByProduct(product);
            log.debug("QnA 삭제 완료");

            // 4. 주문 아이템 삭제
            orderItemRepository.deleteByProduct(product);
            log.debug("주문 아이템 삭제 완료");

            // 5. 마지막으로 상품 삭제
            productRepository.delete(product);
            log.info("상품 삭제 완료: {} (ID: {})", product.getName(), id);
        } catch (Exception e) {
            log.error("상품 삭제 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("상품 삭제 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 상품 검색
     */
    @Transactional(readOnly = true)
    public List<Product> search(String keyword) {
        List<Product> products = productRepository.findByNameContainingIgnoreCase(keyword);
        
        // 이미지 경로 로깅
        log.info("=== 검색어 '{}' 상품 조회 - 총 {} 개 ===", keyword, products.size());
        products.forEach(product -> {
            log.info("상품 ID: {}, 이름: {}, thumbnailImage: {}", 
                product.getId(), product.getName(), product.getThumbnailImage());
        });
        
        return products;
    }

    /**
     * 기존 save 메서드 유지 (하위호환성)
     */
    public Product save(Product p) {
        return productRepository.save(p);
    }

    // ========== 사용자용 메서드 (재고 있는 상품만 조회) ==========

    /**
     * 모든 상품 조회 (사용자용 - 재고 있는 상품만)
     */
    @Transactional(readOnly = true)
    public List<Product> listAllInStock() {
        List<Product> products = productRepository.findAllInStock();
        log.info("=== 판매 가능 상품 조회 - 총 {} 개 ===", products.size());
        return products;
    }

    /**
     * 카테고리별 상품 조회 (사용자용 - 재고 있는 상품만)
     */
    @Transactional(readOnly = true)
    public List<Product> getByCategoryInStock(String category) {
        List<Product> products = productRepository.findByCategoryInStock(category);
        log.info("=== 카테고리 '{}' 판매 가능 상품 - 총 {} 개 ===", category, products.size());
        return products;
    }

    /**
     * 상품 검색 (사용자용 - 재고 있는 상품만)
     */
    @Transactional(readOnly = true)
    public List<Product> searchInStock(String keyword) {
        List<Product> products = productRepository.findByNameContainingIgnoreCaseInStock(keyword);
        log.info("=== 검색어 '{}' 판매 가능 상품 - 총 {} 개 ===", keyword, products.size());
        return products;
    }
}
