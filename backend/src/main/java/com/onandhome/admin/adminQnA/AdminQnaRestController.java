package com.onandhome.admin.adminQnA;

import com.onandhome.qna.QnaReplyService;
import com.onandhome.qna.QnaRepository;
import com.onandhome.qna.dto.QnaDTO;
import com.onandhome.qna.dto.QnaReplyDTO;
import com.onandhome.qna.entity.Qna;
import com.onandhome.qna.entity.QnaReply;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/qna")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class AdminQnaRestController {

    private final QnaRepository qnaRepository;
    private final QnaReplyService qnaReplyService;

    /**
     * 전체 QnA 목록 조회
     * GET /api/admin/qna
     */
    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getAllQnas() {
        Map<String, Object> response = new HashMap<>();
        try {
            log.info("=== 관리자 QnA 전체 목록 조회 ===");

            List<Qna> qnaList = qnaRepository.findAllByOrderByCreatedAtDesc();

            List<QnaDTO> qnaDTOList = qnaList.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            response.put("success", true);
            response.put("data", qnaDTOList);
            log.info("QnA 목록 조회 성공 - 개수: {}", qnaDTOList.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("QnA 목록 조회 실패", e);
            response.put("success", false);
            response.put("message", "QnA 목록을 불러올 수 없습니다: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 특정 QnA 상세 조회
     * GET /api/admin/qna/{id}
     */
    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getQnaById(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            log.info("=== 관리자 QnA 상세 조회 - id: {} ===", id);

            Qna qna = qnaRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("QnA를 찾을 수 없습니다."));

            QnaDTO qnaDTO = convertToDTO(qna);

            response.put("success", true);
            response.put("data", qnaDTO);
            log.info("QnA 상세 조회 성공");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("QnA를 찾을 수 없음", e);
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(404).body(response);
        } catch (Exception e) {
            log.error("QnA 상세 조회 실패", e);
            response.put("success", false);
            response.put("message", "QnA를 불러올 수 없습니다: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * QnA 답변 등록
     * POST /api/admin/qna/{id}/reply
     */
    @PostMapping("/{id}/reply")
    @Transactional
    public ResponseEntity<Map<String, Object>> createReply(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            log.info("=== QnA 답변 등록 - qnaId: {} ===", id);

            String content = request.get("content");
            if (content == null || content.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "답변 내용을 입력해주세요.");
                return ResponseEntity.badRequest().body(response);
            }

            QnaReply reply = qnaReplyService.createReply(id, content, "관리자");

            response.put("success", true);
            response.put("message", "답변이 등록되었습니다.");
            response.put("data", convertReplyToDTO(reply));
            log.info("답변 등록 성공 - replyId: {}", reply.getId());

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("QnA를 찾을 수 없음", e);
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(404).body(response);
        } catch (Exception e) {
            log.error("답변 등록 실패", e);
            response.put("success", false);
            response.put("message", "답변 등록 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * QnA 답변 수정
     * PUT /api/admin/qna/reply/{replyId}
     */
    @PutMapping("/reply/{replyId}")
    @Transactional
    public ResponseEntity<Map<String, Object>> updateReply(
            @PathVariable Long replyId,
            @RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            log.info("=== QnA 답변 수정 - replyId: {} ===", replyId);

            String content = request.get("content");
            if (content == null || content.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "답변 내용을 입력해주세요.");
                return ResponseEntity.badRequest().body(response);
            }

            QnaReply reply = qnaReplyService.updateReply(replyId, content);

            response.put("success", true);
            response.put("message", "답변이 수정되었습니다.");
            response.put("data", convertReplyToDTO(reply));
            log.info("답변 수정 성공");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("답변을 찾을 수 없음", e);
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(404).body(response);
        } catch (Exception e) {
            log.error("답변 수정 실패", e);
            response.put("success", false);
            response.put("message", "답변 수정 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * QnA 답변 삭제
     * DELETE /api/admin/qna/reply/{replyId}
     */
    @DeleteMapping("/reply/{replyId}")
    @Transactional
    public ResponseEntity<Map<String, Object>> deleteReply(@PathVariable Long replyId) {
        Map<String, Object> response = new HashMap<>();
        try {
            log.info("=== QnA 답변 삭제 - replyId: {} ===", replyId);

            qnaReplyService.deleteReply(replyId);

            response.put("success", true);
            response.put("message", "답변이 삭제되었습니다.");
            log.info("답변 삭제 성공");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("답변 삭제 실패", e);
            response.put("success", false);
            response.put("message", "답변 삭제 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * QnA 삭제
     * DELETE /api/admin/qna/{id}
     */
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Map<String, Object>> deleteQna(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            log.info("=== 관리자 QnA 삭제 - id: {} ===", id);

            Qna qna = qnaRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("QnA를 찾을 수 없습니다."));

            qnaRepository.delete(qna);

            response.put("success", true);
            response.put("message", "QnA가 삭제되었습니다.");
            log.info("QnA 삭제 성공");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("QnA를 찾을 수 없음", e);
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(404).body(response);
        } catch (Exception e) {
            log.error("QnA 삭제 실패", e);
            response.put("success", false);
            response.put("message", "QnA 삭제 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * QnA 엔티티를 DTO로 변환
     */
    private QnaDTO convertToDTO(Qna qna) {
        QnaDTO dto = new QnaDTO();
        dto.setId(qna.getId());
        dto.setTitle(qna.getTitle());
        dto.setWriter(qna.getWriter());
        dto.setQuestion(qna.getQuestion());
        dto.setCreatedAt(qna.getCreatedAt());
        dto.setIsPrivate(qna.getIsPrivate());

        if (qna.getProduct() != null) {
            dto.setProductId(qna.getProduct().getId());
            dto.setProductName(qna.getProduct().getName());
        }

        if (qna.getReplies() != null && !qna.getReplies().isEmpty()) {
            List<QnaReplyDTO> replyDTOList = qna.getReplies().stream()
                    .map(this::convertReplyToDTO)
                    .collect(Collectors.toList());
            dto.setReplies(replyDTOList);
        }

        return dto;
    }

    /**
     * QnaReply 엔티티를 DTO로 변환
     */
    private QnaReplyDTO convertReplyToDTO(QnaReply reply) {
        QnaReplyDTO dto = new QnaReplyDTO();
        dto.setId(reply.getId());
        dto.setContent(reply.getContent());
        dto.setAuthor(reply.getResponder());
        dto.setResponder(reply.getResponder());
        dto.setCreatedAt(reply.getCreatedAt());
        if (reply.getQna() != null) {
            dto.setQnaId(reply.getQna().getId());
        }
        return dto;
    }
}