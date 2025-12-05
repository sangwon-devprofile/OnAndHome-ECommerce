package com.onandhome.qna;

import com.onandhome.qna.entity.Qna;
import com.onandhome.qna.entity.QnaReply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * QnaReply 레포지토리
 */
@Repository
public interface QnaReplyRepository extends JpaRepository<QnaReply, Long> {

    // ✅ 특정 질문에 속한 모든 리플라이 조회
    List<QnaReply> findByQnaId(Long qnaId);

    List<QnaReply> findByQnaOrderByCreatedAtAsc(Qna qna);
}
