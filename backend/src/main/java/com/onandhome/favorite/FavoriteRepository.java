package com.onandhome.favorite;

import com.onandhome.favorite.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

//역할 : 찜 데이터에 대한 DB CRUD 작업 처리

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    
    // 특정 사용자의 모든 찜 목록 조회(최신순)
    List<Favorite> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    // 특정 사용자가 특정 상품을 찜했는지 확인
    Optional<Favorite> findByUserIdAndProductId(Long userId, Long productId);
    
    // 특정 사용자가 특정 상품을 찜했는지 여부
    boolean existsByUserIdAndProductId(Long userId, Long productId);
    
    // 특정 상품의 찜 개수
    long countByProductId(Long productId);
    
    // 특정 사용자의 찜 개수
    long countByUserId(Long userId);

}

