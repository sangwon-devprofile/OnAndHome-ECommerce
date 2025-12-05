package com.onandhome.Notice;

import com.onandhome.Notice.dto.NoticeDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 공지사항 REST API 컨트롤러
 */
@RestController
@RequestMapping("/api/notices")
@RequiredArgsConstructor
@Slf4j
public class NoticeRestController {

    private final NoticeService noticeService;

    /**
     * 공지사항 전체 목록 조회
     * GET /api/notices
     */
    @GetMapping
    public ResponseEntity<List<NoticeDto>> getAllNotices() {
        try {
            log.info("공지사항 전체 목록 조회 요청");
            List<NoticeDto> notices = noticeService.findAll();
            log.info("공지사항 목록 조회 성공 - 개수: {}", notices.size());
            return ResponseEntity.ok(notices);
        } catch (Exception e) {
            log.error("공지사항 목록 조회 오류", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 공지사항 개별 조회
     * GET /api/notices/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<NoticeDto> getNoticeById(@PathVariable Long id) {
        try {
            log.info("공지사항 상세 조회 요청 - id: {}", id);
            NoticeDto notice = noticeService.findById(id);
            log.info("공지사항 조회 성공");
            return ResponseEntity.ok(notice);
        } catch (IllegalArgumentException e) {
            log.error("공지사항을 찾을 수 없음", e);
            return ResponseEntity.status(404).build();
        } catch (Exception e) {
            log.error("공지사항 조회 오류", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 최근 공지사항 목록 조회
     * GET /api/notices/recent?limit=8
     */
    @GetMapping("/recent")
    public ResponseEntity<List<NoticeDto>> getRecentNotices(
            @RequestParam(defaultValue = "8") int limit) {
        try {
            log.info("최근 공지사항 목록 조회 요청 - limit: {}", limit);
            List<NoticeDto> notices = noticeService.findAll();
            // limit 적용
            if (notices.size() > limit) {
                notices = notices.subList(0, limit);
            }
            log.info("최근 공지사항 조회 성공 - 개수: {}", notices.size());
            return ResponseEntity.ok(notices);
        } catch (Exception e) {
            log.error("최근 공지사항 조회 오류", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 공지사항 검색
     * GET /api/notices/search?keyword=검색어
     */
    @GetMapping("/search")
    public ResponseEntity<List<NoticeDto>> searchNotices(
            @RequestParam String keyword) {
        try {
            log.info("공지사항 검색 요청 - keyword: {}", keyword);
            List<NoticeDto> notices = noticeService.search(keyword);
            log.info("공지사항 검색 성공 - 개수: {}", notices.size());
            return ResponseEntity.ok(notices);
        } catch (Exception e) {
            log.error("공지사항 검색 오류", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 공지사항 작성
     * POST /api/notices
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createNotice(@RequestBody NoticeDto noticeDto) {
        Map<String, Object> response = new HashMap<>();
        try {
            log.info("공지사항 작성 요청: {}", noticeDto);
            
            if (noticeDto.getTitle() == null || noticeDto.getTitle().trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "제목은 필수 입력 항목입니다.");
                return ResponseEntity.badRequest().body(response);
            }
            
            NoticeDto createdNotice = noticeService.createNotice(noticeDto);
            
            response.put("success", true);
            response.put("message", "공지사항이 등록되었습니다.");
            response.put("data", createdNotice);
            log.info("공지사항 작성 성공 - id: {}", createdNotice.getId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("공지사항 작성 오류", e);
            response.put("success", false);
            response.put("message", "공지사항 작성 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 공지사항 수정
     * PUT /api/notices/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateNotice(
            @PathVariable Long id,
            @RequestBody NoticeDto noticeDto) {
        Map<String, Object> response = new HashMap<>();
        try {
            log.info("공지사항 수정 요청 - id: {}, data: {}", id, noticeDto);
            
            NoticeDto updatedNotice = noticeService.update(id, noticeDto);
            
            response.put("success", true);
            response.put("message", "공지사항이 수정되었습니다.");
            response.put("data", updatedNotice);
            log.info("공지사항 수정 성공");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("공지사항을 찾을 수 없음", e);
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(404).body(response);
        } catch (Exception e) {
            log.error("공지사항 수정 오류", e);
            response.put("success", false);
            response.put("message", "공지사항 수정 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 공지사항 삭제
     * DELETE /api/notices/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteNotice(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            log.info("공지사항 삭제 요청 - id: {}", id);
            
            noticeService.delete(id);
            
            response.put("success", true);
            response.put("message", "공지사항이 삭제되었습니다.");
            log.info("공지사항 삭제 성공");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("공지사항을 찾을 수 없음", e);
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(404).body(response);
        } catch (Exception e) {
            log.error("공지사항 삭제 오류", e);
            response.put("success", false);
            response.put("message", "공지사항 삭제 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
