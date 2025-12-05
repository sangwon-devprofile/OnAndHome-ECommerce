package com.onandhome.company;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 회사 정보 REST API 컨트롤러
 */
@RestController
@RequestMapping("/api/company")
@RequiredArgsConstructor
@Slf4j
public class CompanyInfoRestController {
    
    private final CompanyInfoService companyInfoService;
    
    /**
     * 회사 정보 조회
     * GET /api/company/info
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getCompanyInfo() {
        Map<String, Object> response = new HashMap<>();
        try {
            log.info("회사 정보 조회 요청");
            
            Optional<CompanyInfo> companyInfoOpt = companyInfoService.getCompanyInfo();
            
            if (companyInfoOpt.isPresent()) {
                CompanyInfo companyInfo = companyInfoOpt.get();
                response.put("success", true);
                response.put("data", companyInfo);
                log.info("회사 정보 조회 성공");
            } else {
                // 기본값 반환 (DB에 없을 경우)
                CompanyInfo defaultInfo = new CompanyInfo();
                defaultInfo.setCompanyName("(주)하이미디어");
                defaultInfo.setCeo("이상혁");
                defaultInfo.setFax("02-1544-7778");
                defaultInfo.setEmail("faker@naver.com");
                defaultInfo.setAddress("서울 서초구 서초동 123-456");
                defaultInfo.setBusinessNumber("123-456789");
                defaultInfo.setMailOrderNumber("카456-7894");
                defaultInfo.setPrivacyOfficer("최우제");
                defaultInfo.setPhone("1544-7777");
                
                response.put("success", true);
                response.put("data", defaultInfo);
                response.put("message", "DB에 회사 정보가 없어 기본값을 반환합니다.");
                log.warn("DB에 회사 정보가 없음, 기본값 반환");
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("회사 정보 조회 오류", e);
            response.put("success", false);
            response.put("message", "회사 정보를 불러올 수 없습니다: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 회사 정보 저장/수정 (관리자용)
     * POST /api/company/info
     */
    @PostMapping("/info")
    public ResponseEntity<Map<String, Object>> saveCompanyInfo(@RequestBody CompanyInfo companyInfo) {
        Map<String, Object> response = new HashMap<>();
        try {
            log.info("회사 정보 저장 요청: {}", companyInfo);
            
            CompanyInfo saved = companyInfoService.save(companyInfo);
            
            response.put("success", true);
            response.put("data", saved);
            response.put("message", "회사 정보가 저장되었습니다.");
            log.info("회사 정보 저장 성공");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("회사 정보 저장 오류", e);
            response.put("success", false);
            response.put("message", "회사 정보 저장 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
