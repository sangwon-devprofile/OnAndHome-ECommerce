package com.onandhome.qna;

import com.onandhome.qna.entity.Qna;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface QnaRepository extends JpaRepository<Qna, Long> {
    /**
     * 상품 ID로 QnA 목록 조회 (최신순)
     */
    List<Qna> findByProductIdOrderByCreatedAtDesc(Long productId);

    /**
     * 상품 ID로 QnA 목록 조회
     */
    List<Qna> findByProductId(Long productId);

    /**
     * 상품으로 QnA 삭제
     */
    void deleteByProduct(com.onandhome.admin.adminProduct.entity.Product product);

    /**
     * 최근 QnA 100개 조회 (최신순)
     */
    List<Qna> findTop100ByOrderByCreatedAtDesc();

    /**
     * 전체 QnA 목록 조회 (최신순)
     */
    List<Qna> findAllByOrderByCreatedAtDesc();

    /**
     * 작성자별 QnA 목록 조회 (최신순)
     */
    List<Qna> findByWriterOrderByCreatedAtDesc(String writer);

    /**
     * Product를 fetch join으로 함께 조회 (웹소켓 알림용)
     */
    @Query("SELECT q FROM Qna q LEFT JOIN FETCH q.product WHERE q.id = :id")
    Optional<Qna> findByIdWithProduct(@Param("id") Long id);
}
