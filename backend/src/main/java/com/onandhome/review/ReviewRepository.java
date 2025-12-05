package com.onandhome.review;

import com.onandhome.review.entity.Review;
import com.onandhome.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 리뷰 레포지토리
 */
@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    /**
     * 상품 ID로 리뷰 목록 조회 (최신순)
     */
    List<Review> findByProductIdOrderByCreatedAtDesc(Long productId);
    
    /**
     * 상품 ID로 리뷰 목록 조회
     */
    List<Review> findByProductId(Long productId);
    
    /**
     * 사용자 ID로 리뷰 목록 조회 (Product, Replies, Images eager fetch)
     */
    @Query("SELECT DISTINCT r FROM Review r LEFT JOIN FETCH r.product LEFT JOIN FETCH r.replies LEFT JOIN FETCH r.images WHERE r.user.id = :userId ORDER BY r.createdAt DESC")
    List<Review> findByUserIdWithProduct(@Param("userId") Long userId);
    
    /**
     * 사용자 ID로 리뷰 목록 조회
     */
    List<Review> findByUserId(Long userId);
    
    /**
     * 사용자로 리뷰 목록 조회
     */
    List<Review> findByUser(User user);
    
    /**
     * 작성자명(author)으로 리뷰 목록 조회 (최신순, Product, Replies eager fetch)
     */
    @Query("SELECT DISTINCT r FROM Review r LEFT JOIN FETCH r.product LEFT JOIN FETCH r.replies WHERE r.author = :author ORDER BY r.createdAt DESC")
    List<Review> findByAuthorWithProduct(@Param("author") String author);
    
    /**
     * 작성자명(author)으로 리뷰 목록 조회 (최신순)
     */
    List<Review> findByAuthorOrderByCreatedAtDesc(String writer);
    
    /**
     * username으로 리뷰 목록 조회 (최신순, Product, Replies eager fetch)
     */
    @Query("SELECT DISTINCT r FROM Review r LEFT JOIN FETCH r.product LEFT JOIN FETCH r.replies WHERE r.username = :username ORDER BY r.createdAt DESC")
    List<Review> findByUsernameWithProduct(@Param("username") String username);
    
    /**
     * username으로 리뷰 목록 조회 (최신순)
     */
    List<Review> findByUsernameOrderByCreatedAtDesc(String username);
    
    /**
     * 상품으로 리뷰 삭제
     */
    void deleteByProduct(com.onandhome.admin.adminProduct.entity.Product product);
    
    /**
     * 최근 리뷰 100개 조회 (최신순)
     */
    List<Review> findTop100ByOrderByCreatedAtDesc();

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId")
        // rating 별칭은 r, : 동적 변수를 상품 id로 줌
    Double findAverageRatingByProductId(@Param("productId") Long productId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.product.id = :productId")
    Long countByProductId(@Param("productId") Long productId);

    @Query("SELECT r FROM Review r LEFT JOIN FETCH r.images LEFT JOIN FETCH r.replies WHERE r.id = :id")
    Optional<Review> findByIdWithDetails(Long id);
}

