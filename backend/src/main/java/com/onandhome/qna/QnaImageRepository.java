package com.onandhome.qna;

import com.onandhome.qna.entity.QnaImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QnaImageRepository extends JpaRepository<QnaImage, Long> {

    // 특정 QnA에 속한 이미지 목록
    List<QnaImage> findByQnaId(Long qnaId);

    // 필요 시 QnA 기준으로 전체 삭제 (ON DELETE CASCADE도 있지만 명시적으로 사용 가능)
    void deleteByQnaId(Long qnaId);
}
