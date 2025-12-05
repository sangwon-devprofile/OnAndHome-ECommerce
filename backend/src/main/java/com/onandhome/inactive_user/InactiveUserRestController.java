package com.onandhome.inactive_user;

import com.onandhome.inactive_user.dto.InactiveUserDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 탈퇴 회원 REST API 컨트롤러
 * 관리자 페이지에서 탈퇴 회원 관리에 사용
 */
@RestController
@RequestMapping("/api/admin/inactive-users")
@RequiredArgsConstructor
@Slf4j
public class InactiveUserRestController {

    private final InactiveUserService inactiveUserService;

    /**
     * 탈퇴 회원 목록 조회
     * GET /api/admin/inactive-users
     */
    @GetMapping
    public ResponseEntity<List<InactiveUserDTO>> getInactiveUserList(
            @RequestParam(value = "kw", required = false) String keyword) {
        try {
            log.info("=== 탈퇴 회원 목록 조회 ===");
            log.info("검색어: {}", keyword);

            List<InactiveUserDTO> users;

            if (keyword != null && !keyword.trim().isEmpty()) {
                users = inactiveUserService.searchInactiveUsers(keyword);
            } else {
                users = inactiveUserService.getAllInactiveUsers();
            }

            log.info("조회된 탈퇴 회원 수: {}", users.size());
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            log.error("탈퇴 회원 목록 조회 오류: {}", e.getMessage());
            return ResponseEntity.ok(List.of());
        }
    }

    /**
     * 탈퇴 회원 상세 조회
     * GET /api/admin/inactive-users/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<InactiveUserDTO> getInactiveUserDetail(@PathVariable Long id) {
        try {
            InactiveUserDTO user = inactiveUserService.getInactiveUserById(id)
                    .orElseThrow(() -> new IllegalArgumentException("탈퇴 회원을 찾을 수 없습니다."));
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            log.error("탈퇴 회원 상세 조회 오류: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 탈퇴 회원 영구 삭제 (단일)
     * DELETE /api/admin/inactive-users/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteInactiveUser(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            inactiveUserService.permanentDelete(id);
            response.put("success", true);
            response.put("message", "탈퇴 회원이 영구 삭제되었습니다.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("탈퇴 회원 영구 삭제 오류: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "삭제에 실패했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 탈퇴 회원 영구 삭제 (다중)
     * POST /api/admin/inactive-users/delete
     */
    @PostMapping("/delete")
    public ResponseEntity<Map<String, Object>> deleteMultipleInactiveUsers(
            @RequestBody Map<String, List<Long>> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Long> ids = request.get("ids");

            if (ids == null || ids.isEmpty()) {
                response.put("success", false);
                response.put("message", "삭제할 회원을 선택해주세요.");
                return ResponseEntity.badRequest().body(response);
            }

            int deletedCount = inactiveUserService.permanentDeleteMultiple(ids);

            response.put("success", true);
            response.put("message", deletedCount + "명의 탈퇴 회원이 영구 삭제되었습니다.");
            response.put("deletedCount", deletedCount);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("탈퇴 회원 다중 삭제 오류: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "삭제 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 탈퇴 회원 수 조회
     * GET /api/admin/inactive-users/count
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> getInactiveUserCount() {
        Map<String, Object> response = new HashMap<>();
        try {
            long count = inactiveUserService.countInactiveUsers();
            response.put("success", true);
            response.put("count", count);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("탈퇴 회원 수 조회 오류: {}", e.getMessage());
            response.put("success", false);
            response.put("count", 0);
            return ResponseEntity.ok(response);
        }
    }
}

