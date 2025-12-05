package com.onandhome.email.controller;

// 이메일 서비스 - 실제 이메일 발송 및 인증 코드 관리 담당
import com.onandhome.email.service.EmailService;
// Lombok 어노테이션 - 생성자 자동 생성 (@RequiredArgsConstructor)
import lombok.RequiredArgsConstructor;
// 로깅 어노테이션 - log.info(), log.error() 등 사용 가능
import lombok.extern.slf4j.Slf4j;
// Spring HTTP 응답 객체 - 상태 코드와 body를 함께 반환
import org.springframework.http.ResponseEntity;
// REST API 어노테이션들
import org.springframework.web.bind.annotation.*;

// Map 자료구조 - JSON 응답을 만들기 위해 사용
import java.util.HashMap;
import java.util.Map;

/**
 * 이메일 인증 컨트롤러
 * 
 * 역할:
 * - 회원가입, 비밀번호 재설정, 회원탈퇴 시 이메일 인증 코드 발송 및 검증
 * - 모든 이메일 인증 관련 API 엔드포인트 제공
 * 
 * 주요 기능:
 * 1. 회원가입 인증: /send-code, /verify-code
 * 2. 비밀번호 재설정: /send-password-reset-code, /verify-password-reset-code
 * 3. 회원탈퇴 인증: /send-account-deletion-code, /verify-account-deletion-code
 * 
 * 인증 코드 관리:
 * - EmailService에서 Redis를 사용하여 인증 코드 저장 및 검증
 * - 모든 인증 코드는 5분(300초) 후 자동 만료
 */
@Slf4j // 로깅 기능 활성화
@RestController // REST API 컨트롤러임을 명시 (JSON 응답 반환)
@RequestMapping("/api/email") // 기본 경로: /api/email
@RequiredArgsConstructor // final 필드를 파라미터로 받는 생성자 자동 생성
public class EmailController {

    // EmailService 주입 - 실제 이메일 발송 및 인증 코드 관리 담당
    // @RequiredArgsConstructor에 의해 생성자로 자동 주입됨
    private final EmailService emailService;

    /**
     * 회원가입용 이메일 인증 코드 전송
     * 
     * 호출 경로: POST /api/email/send-code
     * 호출 위치: Signup.js의 handleSendCode() 함수
     * 
     * 처리 흐름:
     * 1. [프론트엔드] 사용자가 이메일 입력 → "인증" 버튼 클릭
     * 2. [프론트엔드] fetch()로 이 API 호출 (POST 요청, body: {email: "user@example.com"})
     * 3. [여기] 이메일 유효성 검증 (비어있는지, 형식이 맞는지)
     * 4. [여기] EmailService.sendVerificationEmail() 호출
     * 5. [EmailService] 6자리 랜덤 코드 생성 → Redis에 저장 (TTL 5분) → SMTP로 이메일 발송
     * 6. [프론트엔드] 성공 메시지 표시 → 타이머 시작 (5분 카운트다운)
     * 
     * @param request - HTTP 요청 body (JSON)
     *                  형식: {"email": "user@example.com"}
     *                  프론트엔드에서 JSON.stringify()로 전송됨
     * @return ResponseEntity<Map<String, Object>> - HTTP 응답
     *         성공: {success: true, message: "인증 코드가 이메일로 전송되었습니다."} (200 OK)
     *         실패: {success: false, message: "에러 메시지"} (400 Bad Request 또는 500 Internal Server Error)
     */
    @PostMapping("/send-code")
    public ResponseEntity<Map<String, Object>> sendVerificationCode(@RequestBody Map<String, String> request) {
        // HTTP 응답 body에 담을 Map 생성 (JSON으로 변환됨)
        Map<String, Object> response = new HashMap<>();

        try {
            // 1. 요청 body에서 email 값 추출
            // request는 {"email": "user@example.com"} 형태의 Map
            String email = request.get("email");
            
            // 로그 출력 (서버 콘솔에 표시)
            log.info("=== 이메일 인증 코드 전송 요청 ===");
            log.info("이메일: {}", email);

            // 2. 이메일이 비어있는지 검증
            if (email == null || email.trim().isEmpty()) {
                // 실패 응답 구성
                response.put("success", false);
                response.put("message", "이메일을 입력해주세요.");
                
                // HTTP 400 Bad Request 반환
                // ResponseEntity.badRequest() = 400 상태 코드
                // .body(response) = 응답 body에 Map을 JSON으로 변환하여 담음
                return ResponseEntity.badRequest().body(response);
            }

            // 3. 이메일 형식 검증 (정규식 사용)
            // 정규식 설명:
            // ^[A-Za-z0-9+_.-]+ : 영문, 숫자, +, _, ., - 중 하나 이상
            // @ : @ 문자 (필수)
            // [A-Za-z0-9.-]+ : 도메인 부분 (영문, 숫자, ., -)
            // \\. : . 문자 (이스케이프 필요)
            // [A-Za-z]{2,}$ : 최상위 도메인 (2글자 이상, 예: com, kr, org)
            if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                response.put("success", false);
                response.put("message", "올바른 이메일 형식이 아닙니다.");
                return ResponseEntity.badRequest().body(response);
            }

            // 4. EmailService.sendVerificationEmail() 호출
            // 이 메소드에서 하는 일:
            // - 6자리 랜덤 인증 코드 생성 (예: "123456")
            // - Redis에 저장 (key: "email:verify:{email}", value: "123456", TTL: 300초)
            // - JavaMailSender로 이메일 발송 (SMTP 사용)
            //   제목: "이메일 인증 코드"
            //   본문: "인증 코드: 123456 (5분 내에 입력해주세요)"
            emailService.sendVerificationEmail(email);

            // 5. 성공 응답 구성
            response.put("success", true);
            response.put("message", "인증 코드가 이메일로 전송되었습니다.");
            
            // HTTP 200 OK 반환
            // ResponseEntity.ok() = 200 상태 코드
            // 프론트엔드에서 response.ok === true로 성공 여부 확인
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            // 6. 예외 처리 (이메일 발송 실패, Redis 연결 실패 등)
            // 발생 가능한 예외:
            // - MailException: SMTP 서버 연결 실패, 인증 실패
            // - RedisConnectionException: Redis 서버 연결 실패
            // - 기타 시스템 오류
            
            response.put("success", false);
            response.put("message", "인증 코드 전송 중 오류가 발생했습니다.");
            
            // HTTP 500 Internal Server Error 반환
            // ResponseEntity.internalServerError() = 500 상태 코드
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 비밀번호 재설정용 이메일 인증 코드 전송
     * 
     * 호출 경로: POST /api/email/send-password-reset-code
     * 호출 위치: PasswordReset.jsx의 handleSendCode() 함수
     * 
     * 처리 흐름:
     * 1. [프론트엔드] 비밀번호 재설정 페이지에서 이메일 입력 → "인증 코드 전송" 버튼 클릭
     * 2. [프론트엔드] fetch()로 이 API 호출
     * 3. [여기] 이메일 유효성 검증
     * 4. [여기] EmailService.sendPasswordResetEmail() 호출
     * 5. [EmailService] 인증 코드 생성 → Redis에 저장 (key: "email:password-reset:{email}") → 이메일 발송
     * 6. [프론트엔드] 성공 메시지 표시 → 타이머 시작
     * 
     * 회원가입 인증과의 차이점:
     * - Redis 키가 다름: "email:password-reset:{email}" vs "email:verify:{email}"
     * - 이메일 제목이 다름: "비밀번호 재설정 인증 코드" vs "이메일 인증 코드"
     * - 나머지 로직은 동일함 (6자리 코드, 5분 만료)
     * 
     * @param request - {"email": "user@example.com"}
     * @return ResponseEntity<Map<String, Object>>
     */
    @PostMapping("/send-password-reset-code")
    public ResponseEntity<Map<String, Object>> sendPasswordResetCode(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 1. 요청에서 이메일 추출
            String email = request.get("email");
            
            // 로그 출력 - 디버깅 및 모니터링용
            log.info("=== 비밀번호 재설정 코드 전송 요청 ===");
            log.info("이메일: {}", email);

            // 2. 이메일 비어있는지 검증
            if (email == null || email.trim().isEmpty()) {
                log.warn("이메일이 비어있음"); // 경고 로그
                response.put("success", false);
                response.put("message", "이메일을 입력해주세요.");
                return ResponseEntity.badRequest().body(response);
            }

            // 3. 이메일 형식 검증
            if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                log.warn("잘못된 이메일 형식: {}", email);
                response.put("success", false);
                response.put("message", "올바른 이메일 형식이 아닙니다.");
                return ResponseEntity.badRequest().body(response);
            }

            // 4. EmailService.sendPasswordResetEmail() 호출
            // 이 메소드에서 하는 일:
            // - 6자리 랜덤 코드 생성
            // - Redis에 저장 (key: "email:password-reset:{email}", TTL: 300초)
            // - 이메일 발송 (제목: "비밀번호 재설정 인증 코드")
            log.info("비밀번호 재설정 이메일 전송 시작: {}", email);
            emailService.sendPasswordResetEmail(email);
            log.info("비밀번호 재설정 이메일 전송 성공: {}", email);

            // 5. 성공 응답
            response.put("success", true);
            response.put("message", "비밀번호 재설정 코드가 이메일로 전송되었습니다.");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            // 6. 예외 처리
            // 로그에 전체 스택 트레이스 출력 (디버깅용)
            log.error("비밀번호 재설정 코드 전송 중 오류", e);
            
            response.put("success", false);
            // 에러 메시지도 함께 전달 (개발 환경에서 유용)
            response.put("message", "비밀번호 재설정 코드 전송 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 비밀번호 재설정 인증 코드 검증
     * 
     * 호출 경로: POST /api/email/verify-password-reset-code
     * 호출 위치: PasswordReset.jsx의 handleVerifyCode() 함수
     * 
     * 처리 흐름:
     * 1. [프론트엔드] 사용자가 이메일에서 받은 6자리 코드 입력 → "확인" 버튼 클릭
     * 2. [프론트엔드] fetch()로 이 API 호출 (body: {email: "...", code: "123456"})
     * 3. [여기] 입력값 검증 (이메일, 코드 둘 다 입력했는지)
     * 4. [여기] EmailService.verifyPasswordResetCode() 호출
     * 5. [EmailService] Redis에서 코드 조회 → 입력 코드와 비교 → 일치하면 true
     * 6. [프론트엔드] 성공 시 다음 단계(새 비밀번호 입력)로 이동
     * 
     * @param request - {"email": "user@example.com", "code": "123456"}
     * @return ResponseEntity<Map<String, Object>>
     *         성공: {success: true, message: "인증 완료되었습니다."}
     *         실패: {success: false, message: "코드가 올바르지 않거나 만료되었습니다."}
     */
    @PostMapping("/verify-password-reset-code")
    public ResponseEntity<Map<String, Object>> verifyPasswordResetCode(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 1. 요청에서 이메일과 코드 추출
            String email = request.get("email");
            String code = request.get("code");

            // 2. 입력값 검증 (둘 중 하나라도 비어있으면 안 됨)
            if (email == null || email.trim().isEmpty() || code == null || code.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "이메일과 인증 코드를 입력해주세요.");
                return ResponseEntity.badRequest().body(response);
            }

            // 3. EmailService.verifyPasswordResetCode() 호출
            // 이 메소드에서 하는 일:
            // - Redis에서 "email:password-reset:{email}" 키로 저장된 코드 조회
            // - 입력 코드와 비교
            // - 일치하면 true 반환, 불일치/만료 시 false 반환
            // - 검증 성공 시 Redis에서 해당 키 삭제 (재사용 방지)
            boolean isVerified = emailService.verifyPasswordResetCode(email, code);

            // 4. 검증 결과에 따른 응답
            if (isVerified) {
                // 검증 성공
                response.put("success", true);
                response.put("message", "인증 완료되었습니다.");
                return ResponseEntity.ok(response); // HTTP 200
            } else {
                // 검증 실패 (코드 불일치 또는 만료)
                // 실패 원인:
                // - 잘못된 코드 입력
                // - 5분(300초) 경과하여 Redis에서 자동 삭제됨
                // - 이미 검증에 사용되어 삭제됨
                response.put("success", false);
                response.put("message", "코드가 올바르지 않거나 만료되었습니다.");
                return ResponseEntity.badRequest().body(response); // HTTP 400
            }

        } catch (Exception e) {
            // 5. 예외 처리 (Redis 연결 실패 등)
            response.put("success", false);
            response.put("message", "코드 검증 중 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 회원가입용 이메일 인증 코드 검증
     * 
     * 호출 경로: POST /api/email/verify-code
     * 호출 위치: Signup.js의 handleVerifyCode() 함수
     * 
     * 처리 흐름:
     * 1. [프론트엔드] 사용자가 이메일에서 받은 코드 입력 → "확인" 버튼 클릭
     * 2. [프론트엔드] fetch()로 이 API 호출
     * 3. [여기] EmailService.verifyCode() 호출
     * 4. [EmailService] Redis에서 "email:verify:{email}" 키로 코드 조회 및 비교
     * 5. [프론트엔드] 성공 시 타이머 중지 → 다음 단계(회원정보 입력)로 이동
     * 
     * @param request - {"email": "user@example.com", "code": "123456"}
     * @return ResponseEntity<Map<String, Object>>
     */
    @PostMapping("/verify-code")
    public ResponseEntity<Map<String, Object>> verifyCode(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 1. 요청에서 이메일과 코드 추출
            String email = request.get("email");
            String code = request.get("code");

            // 2. 입력값 검증
            if (email == null || email.trim().isEmpty() || code == null || code.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "이메일과 인증 코드를 입력해주세요.");
                return ResponseEntity.badRequest().body(response);
            }

            // 3. EmailService.verifyCode() 호출
            // Redis에서 "email:verify:{email}" 키로 저장된 코드와 비교
            boolean isVerified = emailService.verifyCode(email, code);

            // 4. 검증 결과 응답
            if (isVerified) {
                // 검증 성공
                // 이후 프론트엔드에서:
                // - 타이머 중지 (clearInterval)
                // - emailVerification.codeVerified = true
                // - 1.5초 후 setStep(2)로 회원정보 입력 화면으로 전환
                response.put("success", true);
                response.put("message", "이메일 인증이 완료되었습니다.");
                return ResponseEntity.ok(response);
            } else {
                // 검증 실패
                response.put("success", false);
                response.put("message", "코드가 올바르지 않거나 만료되었습니다.");
                return ResponseEntity.badRequest().body(response);
            }

        } catch (Exception e) {
            // 5. 예외 처리
            response.put("success", false);
            response.put("message", "코드 검증 중 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 이메일 인증 완료 여부 확인
     * 
     * 호출 경로: GET /api/email/check-verification?email=user@example.com
     * 호출 위치: 프론트엔드에서 인증 상태 확인 시 (선택적 사용)
     * 
     * 용도:
     * - 페이지 새로고침 후에도 인증 상태 유지 확인
     * - 현재는 사용하지 않을 수 있음 (프론트엔드 상태로 관리)
     * 
     * @param email - 쿼리 파라미터로 전달되는 이메일 주소
     *                예: /api/email/check-verification?email=user@example.com
     * @return ResponseEntity<Map<String, Object>>
     *         {success: true, verified: true/false}
     */
    @GetMapping("/check-verification")
    public ResponseEntity<Map<String, Object>> checkVerification(@RequestParam String email) {
        Map<String, Object> response = new HashMap<>();

        try {
            // EmailService.isEmailVerified() 호출
            // Redis에서 인증 완료 여부 확인
            boolean isVerified = emailService.isEmailVerified(email);

            response.put("success", true);
            response.put("verified", isVerified);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "인증 상태 확인 중 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 회원탈퇴용 이메일 인증 코드 전송
     * 
     * 호출 경로: POST /api/email/send-account-deletion-code
     * 호출 위치: 회원탈퇴 페이지
     * 
     * 처리 흐름:
     * 1. [프론트엔드] 회원탈퇴 페이지에서 이메일 입력 → "인증 코드 전송" 버튼
     * 2. [여기] EmailService.sendAccountDeletionEmail() 호출
     * 3. [EmailService] Redis에 저장 (key: "email:account-deletion:{email}")
     * 4. [프론트엔드] 인증 코드 입력 → 검증 후 회원탈퇴 처리
     * 
     * @param request - {"email": "user@example.com"}
     * @return ResponseEntity<Map<String, Object>>
     */
    @PostMapping("/send-account-deletion-code")
    public ResponseEntity<Map<String, Object>> sendAccountDeletionCode(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 1. 요청에서 이메일 추출
            String email = request.get("email");
            log.info("=== 회원탈퇴 인증 코드 전송 요청 ===");
            log.info("이메일: {}", email);

            // 2. 이메일 유효성 검증
            if (email == null || email.trim().isEmpty()) {
                log.warn("이메일이 비어있음");
                response.put("success", false);
                response.put("message", "이메일을 입력해주세요.");
                return ResponseEntity.badRequest().body(response);
            }

            // 3. 이메일 형식 검증
            if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                log.warn("잘못된 이메일 형식: {}", email);
                response.put("success", false);
                response.put("message", "올바른 이메일 형식이 아닙니다.");
                return ResponseEntity.badRequest().body(response);
            }

            // 4. EmailService.sendAccountDeletionEmail() 호출
            // Redis 키: "email:account-deletion:{email}"
            log.info("회원탈퇴 인증 이메일 전송 시작: {}", email);
            emailService.sendAccountDeletionEmail(email);
            log.info("회원탈퇴 인증 이메일 전송 성공: {}", email);

            // 5. 성공 응답
            response.put("success", true);
            response.put("message", "회원탈퇴 인증 코드가 이메일로 전송되었습니다.");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            // 6. 예외 처리
            log.error("회원탈퇴 인증 코드 전송 중 오류", e);
            response.put("success", false);
            response.put("message", "회원탈퇴 인증 코드 전송 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 회원탈퇴용 인증 코드 검증
     * 
     * 호출 경로: POST /api/email/verify-account-deletion-code
     * 호출 위치: 회원탈퇴 페이지
     * 
     * 처리 흐름:
     * 1. [프론트엔드] 인증 코드 입력 → "확인" 버튼
     * 2. [여기] EmailService.verifyAccountDeletionCode() 호출
     * 3. [EmailService] Redis에서 "email:account-deletion:{email}" 키로 코드 검증
     * 4. [프론트엔드] 성공 시 회원탈퇴 API 호출 (UserController.deleteAccount)
     * 
     * @param request - {"email": "user@example.com", "code": "123456"}
     * @return ResponseEntity<Map<String, Object>>
     */
    @PostMapping("/verify-account-deletion-code")
    public ResponseEntity<Map<String, Object>> verifyAccountDeletionCode(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 1. 요청에서 이메일과 코드 추출
            String email = request.get("email");
            String code = request.get("code");

            // 2. 입력값 검증
            if (email == null || email.trim().isEmpty() || code == null || code.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "이메일과 인증 코드를 입력해주세요.");
                return ResponseEntity.badRequest().body(response);
            }

            // 3. EmailService.verifyAccountDeletionCode() 호출
            // Redis에서 코드 검증
            boolean isVerified = emailService.verifyAccountDeletionCode(email, code);

            // 4. 검증 결과 응답
            if (isVerified) {
                // 검증 성공
                // 프론트엔드에서 회원탈퇴 API 호출로 이어짐
                response.put("success", true);
                response.put("message", "인증 완료되었습니다.");
                return ResponseEntity.ok(response);
            } else {
                // 검증 실패
                response.put("success", false);
                response.put("message", "코드가 올바르지 않거나 만료되었습니다.");
                return ResponseEntity.badRequest().body(response);
            }

        } catch (Exception e) {
            // 5. 예외 처리
            response.put("success", false);
            response.put("message", "코드 검증 중 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
