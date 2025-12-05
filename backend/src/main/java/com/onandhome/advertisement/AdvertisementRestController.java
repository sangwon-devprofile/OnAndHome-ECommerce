package com.onandhome.advertisement;

import com.onandhome.advertisement.dto.AdvertisementDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/advertisements")
@RequiredArgsConstructor
@Log4j2
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class AdvertisementRestController {
    
    private final AdvertisementService advertisementService;
    
    // 광고 목록 조회
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllAdvertisements() {
        try {
            List<AdvertisementDTO> advertisements = advertisementService.getAllAdvertisements();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("advertisements", advertisements);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("광고 목록 조회 실패", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // 광고 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getAdvertisement(@PathVariable Long id) {
        try {
            AdvertisementDTO advertisement = advertisementService.getAdvertisement(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("advertisement", advertisement);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("광고 조회 실패: id={}", id, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // 광고 생성
    @PostMapping
    public ResponseEntity<Map<String, Object>> createAdvertisement(
            @RequestBody AdvertisementDTO dto) {
        try {
            AdvertisementDTO created = advertisementService.createAdvertisement(dto);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "광고가 생성되었습니다.");
            response.put("advertisement", created);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("광고 생성 실패", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // 광고 수정
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateAdvertisement(
            @PathVariable Long id,
            @RequestBody AdvertisementDTO dto) {
        try {
            AdvertisementDTO updated = advertisementService.updateAdvertisement(id, dto);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "광고가 수정되었습니다.");
            response.put("advertisement", updated);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("광고 수정 실패: id={}", id, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // 광고 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteAdvertisement(@PathVariable Long id) {
        try {
            advertisementService.deleteAdvertisement(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "광고가 삭제되었습니다.");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("광고 삭제 실패: id={}", id, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // 광고 알림 발송
    @PostMapping("/{id}/send")
    public ResponseEntity<Map<String, Object>> sendAdvertisementNotification(@PathVariable Long id) {
        try {
            int count = advertisementService.sendAdvertisementNotification(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", count + "명의 사용자에게 광고 알림을 발송했습니다.");
            response.put("count", count);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("광고 알림 발송 실패: id={}", id, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}

// 사용자용 광고 조회 API (마케팅 동의한 사용자만 접근 가능)
@RestController
@RequestMapping("/api/user/advertisements")
@RequiredArgsConstructor
@Log4j2
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
class UserAdvertisementRestController {
    
    private final AdvertisementService advertisementService;
    
    // 광고 상세 조회 (마케팅 동의한 사용자만)
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getAdvertisement(
            @PathVariable Long id,
            @RequestAttribute("userId") Long userId,
            @RequestAttribute("marketingConsent") Boolean marketingConsent) {
        try {
            log.info("광고 조회 요청: advertisementId={}, userId={}, marketingConsent={}", 
                    id, userId, marketingConsent);
            
            // 마케팅 동의 확인
            if (marketingConsent == null || !marketingConsent) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "마케팅 정보 수신에 동의한 사용자만 광고를 볼 수 있습니다.");
                return ResponseEntity.status(403).body(response);
            }
            
            AdvertisementDTO advertisement = advertisementService.getAdvertisement(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("advertisement", advertisement);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("광고 조회 실패: id={}", id, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
