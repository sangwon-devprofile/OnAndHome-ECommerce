package com.onandhome.user;

import com.onandhome.user.dto.UserDTO;
import com.onandhome.user.entity.User;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 사용자 REST API 컨트롤러
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Slf4j
public class UserRestController {

    private final UserService userService;
    private final com.onandhome.util.JWTUtil jwtUtil;
    private static final String SESSION_USER_KEY = "loginUser";

    /**
     * 현재 로그인한 사용자 정보 조회 - JWT 기반
     * GET /api/user/my-info
     */
    @GetMapping("/my-info")
    public ResponseEntity<Map<String, Object>> getMyInfo(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Map<String, Object> response = new HashMap<>();
        try {
            log.info("사용자 정보 조회 요청");
            
            // JWT 토큰이 없는 경우
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("Authorization 헤더가 없거나 형식이 잘못됨");
                response.put("success", false);
                response.put("message", "로그인이 필요합니다.");
                return ResponseEntity.status(401).body(response);
            }
            
            // JWT 토큰에서 사용자 정보 추출
            String token = authHeader.substring(7);
            Map<String, Object> claims = jwtUtil.validateToken(token);
            
            Long userId = Long.valueOf(claims.get("id").toString());
            
            // DB에서 최신 사용자 정보 다시 조회
            Optional<UserDTO> userOptional = userService.getUserById(userId);
            
            if (userOptional.isEmpty()) {
                response.put("success", false);
                response.put("message", "사용자를 찾을 수 없습니다.");
                return ResponseEntity.status(404).body(response);
            }
            
            UserDTO userDTO = userOptional.get();
            
            response.put("success", true);
            response.put("data", userDTO);
            log.info("사용자 정보 조회 성공 - userId: {}", userDTO.getUserId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("사용자 정보 조회 오류", e);
            response.put("success", false);
            response.put("message", "사용자 정보를 불러올 수 없습니다: " + e.getMessage());
            return ResponseEntity.status(401).body(response);
        }
    }

    /**
     * 사용자 정보 업데이트 - JWT 기반
     * PUT /api/user/update
     */
    @PutMapping("/update")
    public ResponseEntity<Map<String, Object>> updateUserInfo(
            @RequestBody UserDTO userDTO,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Map<String, Object> response = new HashMap<>();
        try {
            log.info("사용자 정보 수정 요청: {}", userDTO.getUserId());
            
            // JWT 토큰이 없는 경우
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("Authorization 헤더가 없거나 형식이 잘못됨");
                response.put("success", false);
                response.put("message", "로그인이 필요합니다.");
                return ResponseEntity.status(401).body(response);
            }
            
            // JWT 토큰에서 사용자 정보 추출
            String token = authHeader.substring(7);
            Map<String, Object> claims = jwtUtil.validateToken(token);
            Long tokenUserId = Long.valueOf(claims.get("id").toString());
            
            // 본인 정보만 수정 가능
            if (!tokenUserId.equals(userDTO.getId())) {
                response.put("success", false);
                response.put("message", "본인의 정보만 수정할 수 있습니다.");
                return ResponseEntity.status(403).body(response);
            }
            
            UserDTO updatedUser = userService.updateUser(userDTO);
            
            response.put("success", true);
            response.put("message", "정보가 수정되었습니다.");
            response.put("data", updatedUser);
            log.info("사용자 정보 수정 성공 - userId: {}", updatedUser.getUserId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("사용자 정보 수정 오류", e);
            response.put("success", false);
            response.put("message", "정보 수정 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 관리자용 회원 일괄 삭제 - JWT 기반
     * POST /api/user/delete
     */
    @PostMapping("/delete")
    public ResponseEntity<Map<String, Object>> deleteUsers(
            @RequestBody Map<String, Object> request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Map<String, Object> response = new HashMap<>();
        try {
            // JWT 토큰이 없는 경우
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다.");
                return ResponseEntity.status(401).body(response);
            }
            
            // JWT 토큰에서 사용자 정보 추출
            String token = authHeader.substring(7);
            Map<String, Object> claims = jwtUtil.validateToken(token);
            Integer role = (Integer) claims.get("role");
            
            if (role == null || role != 0) {
                log.warn("관리자가 아닌 사용자의 회원 삭제 시도");
                response.put("success", false);
                response.put("message", "관리자만 이 기능을 사용할 수 있습니다.");
                return ResponseEntity.status(403).body(response);
            }
            
            // 요청에서 userIds 배열 가져오기
            @SuppressWarnings("unchecked")
            java.util.List<String> userIdStrings = (java.util.List<String>) request.get("userIds");
            
            if (userIdStrings == null || userIdStrings.isEmpty()) {
                response.put("success", false);
                response.put("message", "삭제할 회원을 선택해주세요.");
                return ResponseEntity.status(400).body(response);
            }
            
            // String을 Long으로 변환
            java.util.List<Long> userIds = new java.util.ArrayList<>();
            for (String idStr : userIdStrings) {
                try {
                    userIds.add(Long.parseLong(idStr));
                } catch (NumberFormatException e) {
                    log.error("잘못된 userId 형식: {}", idStr);
                }
            }
            
            log.info("회원 일괄 삭제 요청 - 삭제할 회원 수: {}", userIds.size());
            
            // 각 사용자 삭제
            int successCount = 0;
            int failCount = 0;
            for (Long userId : userIds) {
                try {
                    userService.deleteUser(userId);
                    successCount++;
                    log.info("회원 삭제 성공 - userId: {}", userId);
                } catch (Exception e) {
                    failCount++;
                    log.error("회원 삭제 실패 - userId: {}, error: {}", userId, e.getMessage());
                }
            }
            
            response.put("success", true);
            response.put("message", String.format("총 %d명 중 %d명 삭제 성공, %d명 삭제 실패", 
                userIds.size(), successCount, failCount));
            response.put("successCount", successCount);
            response.put("failCount", failCount);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("회원 일괄 삭제 오류", e);
            response.put("success", false);
            response.put("message", "회원 삭제 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 회원 탈퇴 - JWT 기반
     * DELETE /api/user/withdraw
     */
    @DeleteMapping("/withdraw")
    public ResponseEntity<Map<String, Object>> withdrawUser(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Map<String, Object> response = new HashMap<>();
        try {
            log.info("회원 탈퇴 요청");
            
            // JWT 토큰이 없는 경우
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다.");
                return ResponseEntity.status(401).body(response);
            }
            
            // JWT 토큰에서 사용자 정보 추출
            String token = authHeader.substring(7);
            Map<String, Object> claims = jwtUtil.validateToken(token);
            Long userId = Long.valueOf(claims.get("id").toString());
            String userIdStr = (String) claims.get("userId");
            
            // 사용자 삭제
            userService.deleteUser(userId);
            
            response.put("success", true);
            response.put("message", "회원 탈퇴가 완료되었습니다.");
            log.info("회원 탈퇴 성공 - userId: {}", userIdStr);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("회원 탈퇴 오류", e);
            response.put("success", false);
            response.put("message", "회원 탈퇴 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
