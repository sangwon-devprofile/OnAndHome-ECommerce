package com.onandhome.notification;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.onandhome.notification.dto.NotificationDTO;
import com.onandhome.util.JWTUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Log4j2
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class NotificationRestController {

    /* 알림 비즈니스 로직을 처리하는 서비스 */
    private final NotificationService notificationService;

    /* JWT 토큰 검증 및 claims 추출 */
    private final JWTUtil jwtUtil;

    /* Authorization 헤더에서 사용자 ID를 추출하는 메서드 */
    private String getUserIdFromToken(String authHeader) {

        /* Authorization 헤더가 없거나 Bearer 토큰이 아닌 경우 */
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("유효하지 않은 토큰입니다.");
        }

        /* "Bearer " 제거하고 실제 토큰 값만 추출 */
        String token = authHeader.substring(7);

        /* JWT에서 claims 추출 */
        Map<String, Object> claims = jwtUtil.validateToken(token);

        /* claims 내부에서 userId 꺼내기 */
        return (String) claims.get("userId");
    }

    /*
       1. 알림 목록 조회
       사용자의 전체 알림(읽음/안 읽음 모두)을 최신순으로 반환
        */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getNotifications(
            @RequestHeader("Authorization") String authHeader) {

        try {
            /* 토큰에서 userId 추출 */
            String userId = getUserIdFromToken(authHeader);

            /* 해당 유저의 알림 목록 조회 */
            List<NotificationDTO> notifications = notificationService.getUserNotifications(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("notifications", notifications);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("알림 조회 실패", e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /*
       2. 읽지 않은 알림 개수 조회
       헤더에서 userId를 얻고, unread_count 반환
        */
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Object>> getUnreadCount(
            @RequestHeader("Authorization") String authHeader) {

        try {
            String userId = getUserIdFromToken(authHeader);

            /* 읽지 않은 알림 개수 조회 */
            long count = notificationService.getUnreadCount(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("count", count);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("읽지 않은 알림 개수 조회 실패", e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /*
       3. 단일 알림 읽음 처리
       특정 알림 ID를 읽음 처리함
       */
    @PutMapping("/{id}/read")
    public ResponseEntity<Map<String, Object>> markAsRead(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {

        try {
            /* userId만 검증한다. 권한 체크 용도 */
            getUserIdFromToken(authHeader);

            /* 해당 알림 읽음 처리 */
            notificationService.markAsRead(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "알림을 읽음 처리했습니다.");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("알림 읽음 처리 실패", e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /*
       4. 사용자의 모든 알림 읽음 처리
       한 번에 전체 알림 읽음 처리하기
        */
    @PutMapping("/read-all")
    public ResponseEntity<Map<String, Object>> markAllAsRead(
            @RequestHeader("Authorization") String authHeader) {

        try {
            String userId = getUserIdFromToken(authHeader);

            /* 유저의 모든 알림 읽음 처리 */
            notificationService.markAllAsRead(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "모든 알림을 읽음 처리했습니다.");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("모든 알림 읽음 처리 실패", e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /*
       5. 알림 삭제
       사용자가 특정 알림을 직접 삭제하는 경우
        */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteNotification(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {

        try {
            getUserIdFromToken(authHeader);

            /* 알림 삭제 */
            notificationService.deleteNotification(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "알림을 삭제했습니다.");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("알림 삭제 실패", e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /*
       6. 관리자: 대량 알림 발송
       전체 사용자 또는 특정 조건 사용자에게 알림 전송
       광고 알림이나 공지 알림에 사용 가능
        */
    @PostMapping("/admin/bulk")
    public ResponseEntity<Map<String, Object>> createBulkNotification(
            @RequestBody Map<String, Object> request,
            @RequestHeader("Authorization") String authHeader) {

        try {
            /* 관리자 권한은 TODO로 남겨둠. 현재는 토큰 검증만 함 */
            getUserIdFromToken(authHeader);

            /* 요청에서 필요한 필드 추출 */
            String title = (String) request.get("title");
            String content = (String) request.get("content");
            String type = (String) request.get("type");

            /* referenceId는 null일 수 있으므로 안전하게 변환 */
            Long referenceId = request.get("referenceId") != null
                    ? Long.valueOf(request.get("referenceId").toString())
                    : null;

            /* 실제 알림 전송 */
            int count = notificationService.createBulkNotification(title, content, type, referenceId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", count + "명의 사용자에게 알림을 전송했습니다.");
            response.put("count", count);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("대량 알림 전송 실패", e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }
}

/*
요약
1. 이 컨트롤러는 알림 CRUD와 조회 API를 제공하는 REST 전용 엔드포인트
2. JWT 토큰에서 userId 추출 → 모든 요청에서 사용자 인증을 수행
3. 개별 알림/전체 알림 읽음 처리 기능이 포함되어 있음
4. 관리자용 대량 알림 전송 기능도 이 컨트롤러가 담당
%. 실시간 알림 자체는 서비스(NotificationService)에서 처리되고,
   이 컨트롤러는 목록/읽음/삭제 같은 일반 알림을 담당
 */