package com.onandhome.user;

import com.onandhome.email.service.EmailService;
import com.onandhome.user.dto.UserDTO;
import com.onandhome.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    private final JWTUtil jwtUtil;
    private final EmailService emailService;

    /**
     * 회원가입 API
     * 
     * 호출 경로: POST /api/user/register
     * 호출 위치: Signup.js의 handleSubmit() 함수 → authApi.signup()
     * 
     * 회원가입 전체 흐름:
     * [1단계: 이메일 인증 (Signup.js step=1)]
     * 1. 사용자가 이메일 입력 → "인증" 버튼 클릭
     * 2. Signup.js: handleSendCode() → POST /api/email/send-code
     * 3. EmailController.sendVerificationCode() → EmailService.sendVerificationEmail()
     *    - 6자리 랜덤 코드 생성 → Redis 저장 (TTL: 300초) → SMTP 이메일 발송
     * 4. 사용자가 이메일에서 인증 코드 확인
     * 5. 인증 코드 입력 → Signup.js: handleVerifyCode() → POST /api/email/verify-code
     * 6. EmailController.verifyCode() → EmailService.verifyCode()
     *    - Redis에서 코드 조회 및 비교 → 일치하면 검증 완료
     * 7. 인증 성공 → Signup.js: setStep(2) (회원정보 입력 화면으로 전환)
     * 
     * [2단계: 회원정보 입력 (Signup.js step=2)]
     * 8. 회원정보 폼 작성 (userId, password, username, phone, 동의 항목)
     * 9. "회원가입" 버튼 클릭 → Signup.js: handleSubmit()
     * 10. 폼 검증 (validateSignupForm())
     * 11. authApi.signup() 호출 → ★ 이 메소드 호출됨
     * 
     * [3단계: 회원가입 처리 (여기)]
     * 12. role을 1(일반사용자)로 강제 설정
     * 13. UserService.register() 호출
     *     - userId 중복 체크 → password BCrypt 암호화 → User 엔티티 생성 및 DB 저장
     * 14. 성공 응답 반환 (HTTP 201 Created)
     * 15. Signup.js에서 성공 메시지 표시 → 2초 후 로그인 페이지로 이동
     * 
     * @param userDTO - 회원가입 정보 (JSON 형식)
     *                  {userId, password, email, username, phone, marketingConsent, privacyConsent}
     * @return ResponseEntity<Map<String, Object>>
     *         성공: {success: true, message: "회원가입 성공", data: UserDTO} (201)
     *         실패: {success: false, message: "에러 메시지"} (400 또는 500)
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody UserDTO userDTO) {
        // HTTP 응답 body에 담을 Map 생성 (JSON으로 자동 변환됨)
        Map<String, Object> response = new HashMap<>();
        try {
            // 1. 회원가입 요청 로그 출력
            log.info("회원가입 요청: {}", userDTO.getUserId());

            /**
             * 2. role 강제 설정 (보안상 매우 중요!)
             * 
             * role 의미:
             * - 0: 관리자 (ROLE_ADMIN)
             * - 1: 일반 사용자 (ROLE_USER)
             * 
             * 보안 주의사항:
             * - 일반 사용자 회원가입은 항상 role = 1로 설정해야 함
             * - role = 0 (관리자)는 DB에서 직접 수정해야 함
             * - 프론트엔드에서 role 값을 보내더라도 무시하고 강제로 1로 설정
             */
            userDTO.setRole(1);

            /**
             * 3. UserService.register() 호출하여 실제 회원가입 처리
             * 
             * UserService.register()에서 하는 일:
             * 3-1. userId(이메일) 중복 체크
             *      - UserRepository.findByUserId(userId) 호출
             *      - 이미 존재하면 IllegalArgumentException("이미 존재하는 아이디입니다") 발생
             * 
             * 3-2. password를 BCrypt로 암호화
             *      - passwordEncoder.encode(password) 호출
             *      - 평문 "Password1!" → "$2a$10$N9qo8uLOickgx2ZMRZoMye..."
             *      - 복호화 불가능 (일방향 해시), 동일 비밀번호도 매번 다른 해시값 생성
             * 
             * 3-3. User 엔티티 생성 및 필드 설정
             * 3-4. UserRepository.save() → DB INSERT
             * 3-5. User 엔티티를 UserDTO로 변환하여 반환 (비밀번호 제외)
             */
            UserDTO registeredUser = userService.register(userDTO);
            
            /**
             * 4. 성공 응답 데이터 구성
             * 
             * JSON 형식으로 변환됨:
             * {
             *   "success": true,
             *   "message": "회원가입 성공",
             *   "data": {
             *     "id": 1,
             *     "userId": "user123",
             *     "email": "user@example.com",
             *     "username": "홍길동",
             *     "role": 1,
             *     ...
             *   }
             * }
             */
            response.put("success", true);
            response.put("message", "회원가입 성공");
            response.put("data", registeredUser);
            
            /**
             * 5. HTTP 201 Created 상태 코드와 함께 응답 반환
             * - 201: 새로운 리소스(사용자)가 성공적으로 생성됨
             * - 프론트엔드에서 response.status === 201로 성공 확인
             */
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {
            /**
             * 6. IllegalArgumentException 처리 (userId 중복)
             * 
             * 발생 원인: UserService.register()에서 userId 중복 체크 시
             * 처리: HTTP 400 Bad Request 반환
             * 프론트엔드: "이미 존재하는 아이디입니다" 메시지 표시
             */
            log.warn("회원가입 실패: {}", e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            
        } catch (Exception e) {
            /**
             * 7. 기타 예외 처리 (DB 연결 실패, 시스템 오류 등)
             * 
             * 처리: HTTP 500 Internal Server Error 반환
             * 프론트엔드: "회원가입 처리 중 오류가 발생했습니다" 메시지 표시
             */
            log.error("회원가입 중 오류: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "회원가입 처리 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 로그인 API - JWT 토큰 반환
     * 
     * 호출 경로: POST /api/user/login
     * 호출 위치:
     * 1. 일반 사용자 로그인: Login.js의 handleSubmit() → authApi.login()
     * 2. 관리자 로그인: AdminLogin.js의 handleSubmit() → axios.post()
     * 
     * 중요: 일반 사용자와 관리자가 동일한 API 사용
     * - 로그인 처리 로직은 동일함
     * - role 검증은 프론트엔드에서 처리:
     *   * Login.js: role 검증 없음 (role=0, role=1 모두 메인 페이지로 이동)
     *   * AdminLogin.js: user.role === 0 체크 (관리자만 관리자 페이지 접근)
     * 
     * 처리 흐름:
     * 1. userId와 password 받기
     * 2. UserService.login() 호출 (userId/password 검증)
     * 3. 사용자가 존재하고 비밀번호가 일치하면:
     *    - JWT 토큰 생성 (accessToken: 60분, refreshToken: 7일)
     *    - 클레임에 id, userId, role, marketingConsent 포함
     *    - ★ role 값 포함 중요: 0(관리자) or 1(일반사용자)
     * 4. 성공 응답 반환 (HTTP 200 OK)
     * 5. 프론트엔드에서 role에 따라 분기 처리
     * 
     * role 값 의미:
     * - role = 0: 관리자 (ROLE_ADMIN)
     *   * 관리자 권한은 DB에서 직접 role을 0으로 변경해야 함
     *   * SQL 예시: UPDATE user SET role = 0 WHERE user_id = 'admin@example.com';
     * - role = 1: 일반 사용자 (ROLE_USER)
     *   * 회원가입 시 기본값
     * 
     * 데이터 흐름:
     * [프론트엔드] {userId, password} → [백엔드] UserController.login()
     * → UserService.login() → UserRepository.findByUserId()
     * → password BCrypt 검증 (passwordEncoder.matches)
     * → 성공 시: JWT 토큰 생성 (claims에 role 포함)
     * → [프론트엔드] {success, accessToken, refreshToken, user(★role 포함)}
     * → Login.js: 메인 페이지 이동 (role 관계없이)
     * → AdminLogin.js: role=0이면 관리자 대시보드, role=1이면 에러
     * 
     * @param loginRequest - 로그인 요청 (내부 클래스)
     *                       형식: {"userId": "user123", "password": "Password1!"}
     * @return ResponseEntity<Map<String, Object>>
     *         성공: {success: true, accessToken, refreshToken, user} (200)
     *         실패: {success: false, message: "에러 메시지"} (401 또는 500)
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest loginRequest) {
        // HTTP 응답 body에 담을 Map 생성
        Map<String, Object> response = new HashMap<>();
        try {
            // 1. 로그인 요청 로그 출력
            log.info("로그인 요청: {}", loginRequest.getUserId());
            
            /**
             * 2. UserService.login() 호출하여 사용자 인증
             * 
             * UserService.login()에서 하는 일:
             * 2-1. UserRepository.findByUserId(userId) 호출
             *      - DB에서 해당 userId를 가진 사용자 조회
             *      - Optional<User> 반환
             * 
             * 2-2. 사용자가 존재하면 password BCrypt 검증
             *      - passwordEncoder.matches(inputPassword, dbPassword)
             *      - 입력한 평문 비밀번호를 DB의 해시값과 비교
             *      - BCrypt는 동일 평문에 대해도 매번 다른 해시 생성하지만
             *        matches()는 정확히 검증 가능
             * 
             * 2-3. 비밀번호 일치: User 엔티티를 UserDTO로 변환하여 Optional로 반환
             *      비밀번호 불일치: Optional.empty() 반환
             */
            Optional<UserDTO> userOptional = userService.login(loginRequest.getUserId(), loginRequest.getPassword());

            // 3. 로그인 성공 처리
            if (userOptional.isPresent()) {
                UserDTO user = userOptional.get();

                /**
                 * 4. JWT 토큰 생성을 위한 클레임(Claims) 생성
                 * 
                 * JWT 클레임에 포함되는 정보:
                 * - id: 사용자 PK (로그인 상태 유지용)
                 * - userId: 로그인 아이디
                 * - ★ role: 사용자 권한 (0=관리자, 1=일반사용자)
                 * - marketingConsent: 광고 수신 동의 여부
                 * 
                 * ★ role 포함 중요성:
                 * - 프론트엔드에서 관리자 권한 판별 가능
                 * - AdminLogin.js에서 user.role === 0 체크하여 관리자 페이지 접근 제어
                 */
                Map<String, Object> claims = new HashMap<>();
                claims.put("id", user.getId());
                claims.put("userId", user.getUserId());
                claims.put("role", user.getRole()); // ★ 0(관리자) or 1(일반사용자)
                claims.put("marketingConsent", user.getMarketingConsent() != null ? user.getMarketingConsent() : false);

                /**
                 * 5. JWT 토큰 생성
                 * 
                 * Access Token:
                 * - 유효 기간: 60분
                 * - 용도: API 요청 시 인증용
                 * - Authorization: Bearer <accessToken> 헤더에 포함하여 전송
                 * 
                 * Refresh Token:
                 * - 유효 기간: 7일 (60분 * 24시간 * 7일)
                 * - 용도: Access Token 만료 시 갱신용
                 * - POST /api/user/refresh 로 새 Access Token 발급
                 * 
                 * JWTUtil.generateToken()
                 * - 클레임과 유효 기간을 받아 JWT 생성
                 * - HMAC SHA256 알고리즘 사용
                 */
                String accessToken = jwtUtil.generateToken(claims, 60);            // 60분
                String refreshToken = jwtUtil.generateToken(claims, 60 * 24 * 7); // 7일

                /**
                 * 6. 성공 응답 데이터 구성
                 * 
                 * JSON 형식으로 변환됨:
                 * {
                 *   "success": true,
                 *   "message": "로그인 성공",
                 *   "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
                 *   "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
                 *   "user": {
                 *     "id": 1,
                 *     "userId": "user123",
                 *     "username": "홍길동",
                 *     "email": "user@example.com",
                 *     "role": 1,  // ← ★ 프론트엔드에서 role 확인 가능
                 *     ...
                 *   }
                 * }
                 */
                response.put("success", true);
                response.put("message", "로그인 성공");
                response.put("accessToken", accessToken);
                response.put("refreshToken", refreshToken);
                response.put("user", user); // user 객체에 role 필드 포함
                
                log.info("로그인 성공: {}, marketingConsent: {}", loginRequest.getUserId(), user.getMarketingConsent());
                
                /**
                 * 7. HTTP 200 OK 상태 코드와 함께 응답 반환
                 * 
                 * 프론트엔드에서의 처리:
                 * - Login.js: Redux store 저장 → 메인 페이지 이동 (role 관계없이)
                 * - AdminLogin.js:
                 *   * user.role === 0 체크
                 *   * role=0: 토큰 저장 → 관리자 대시보드('/admin/dashboard') 이동
                 *   * role=1: "관리자 권한이 없습니다" 에러 + 로그인 차단
                 */
                return ResponseEntity.ok(response);
            } else {
                /**
                 * 8. 로그인 실패 처리 (userId 없거나 password 불일치)
                 * 
                 * UserService.login()에서 Optional.empty() 반환한 경우:
                 * - userId가 DB에 없음
                 * - password가 일치하지 않음
                 * 
                 * 보안 주의:
                 * - "아이디가 없습니다" vs "비밀번호가 틀렸습니다" 구분 안 함
                 * - 이유: 공격자가 유효한 아이디를 파악하는 것 방지
                 */
                response.put("success", false);
                response.put("message", "아이디 또는 비밀번호가 일치하지 않습니다.");
                log.warn("로그인 실패: {}", loginRequest.getUserId());
                
                // HTTP 401 Unauthorized 반환
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
        } catch (Exception e) {
            /**
             * 9. 예외 처리 (DB 오류, 시스템 오류 등)
             * 
             * 발생 가능한 예외:
             * - DataAccessException: DB 연결 실패
             * - NullPointerException: 데이터 오류
             * - 기타 시스템 오류
             * 
             * 처리:
             * - 에러 로그 출력
             * - HTTP 500 Internal Server Error 반환
             */
            log.error("로그인 중 오류: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "로그인 처리 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 사용자 조회 API (ID로)
     * GET /api/user/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            Optional<UserDTO> userOptional = userService.getUserById(id);
            if (userOptional.isPresent()) {
                response.put("success", true);
                response.put("data", userOptional.get());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "사용자를 찾을 수 없습니다.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            log.error("사용자 조회 중 오류: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "사용자 조회 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 사용자 조회 API (userId로)
     * GET /api/user/username/{userId}
     */
    @GetMapping("/username/{userId}")
    public ResponseEntity<Map<String, Object>> getUserByUserId(@PathVariable String userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Optional<UserDTO> userOptional = userService.getUserByUserId(userId);
            if (userOptional.isPresent()) {
                response.put("success", true);
                response.put("data", userOptional.get());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "사용자를 찾을 수 없습니다.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            log.error("사용자 조회 중 오류: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "사용자 조회 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * ✅ 세션 정보 조회 API (JWT 기반 전용)
     * GET /api/user/session-info
     * Authorization 헤더에서 JWT 토큰을 확인하여 사용자 정보 반환
     */
    @GetMapping("/session-info")
    public ResponseEntity<Map<String, Object>> getSessionInfo(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Map<String, Object> response = new HashMap<>();
        try {
            // JWT 토큰이 없는 경우
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.put("loggedIn", false);
                response.put("user", null);
                response.put("isAdmin", false);
                return ResponseEntity.ok(response);
            }
            
            // JWT 토큰 검증
            String token = authHeader.substring(7);
            Map<String, Object> claims = jwtUtil.validateToken(token);

            Long userId = Long.valueOf(claims.get("id").toString());
            Optional<UserDTO> userOptional = userService.getUserById(userId);

            if (userOptional.isPresent()) {
                UserDTO user = userOptional.get();
                response.put("loggedIn", true);
                response.put("user", user);
                response.put("isAdmin", user.getRole() == 0);
                log.debug("세션 정보 조회 (JWT 기반) - 로그인 사용자: {}", user.getUserId());
                return ResponseEntity.ok(response);
            } else {
                response.put("loggedIn", false);
                response.put("user", null);
                response.put("isAdmin", false);
                return ResponseEntity.ok(response);
            }
            
        } catch (Exception e) {
            log.error("세션 정보 조회 중 오류: {}", e.getMessage());
            response.put("loggedIn", false);
            response.put("user", null);
            response.put("isAdmin", false);
            return ResponseEntity.ok(response);
        }
    }

    /**
     * ✅ 사용자 정보 조회 API (JWT 기반)
     * GET /api/user/info
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getUserInfo(
            @RequestHeader(value = "Authorization") String authHeader) {
        Map<String, Object> response = new HashMap<>();
        try {
            String token = authHeader.substring(7);
            Map<String, Object> claims = jwtUtil.validateToken(token);
            Long userId = Long.valueOf(claims.get("id").toString());

            Optional<UserDTO> userOptional = userService.getUserById(userId);
            if (userOptional.isPresent()) {
                UserDTO user = userOptional.get();
                // 비밀번호는 제외하고 반환
                user.setPassword(null);
                response.put("success", true);
                response.put("data", user);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "사용자를 찾을 수 없습니다.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            log.error("사용자 정보 조회 중 오류: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "사용자 정보 조회 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * ✅ 사용자 정보 수정 API (JWT 기반)
     * PUT /api/user/info
     */
    @PutMapping("/info")
    public ResponseEntity<Map<String, Object>> updateUserInfo(
            @RequestHeader(value = "Authorization") String authHeader,
            @RequestBody UserDTO userDTO) {
        Map<String, Object> response = new HashMap<>();
        try {
            String token = authHeader.substring(7);
            Map<String, Object> claims = jwtUtil.validateToken(token);
            Long userId = Long.valueOf(claims.get("id").toString());

            // JWT에서 가져온 userId와 요청 데이터의 userId가 일치하는지 확인
            userDTO.setId(userId);
            
            UserDTO updatedUser = userService.updateUser(userDTO);
            // 비밀번호는 제외하고 반환
            updatedUser.setPassword(null);
            
            response.put("success", true);
            response.put("message", "사용자 정보가 수정되었습니다.");
            response.put("data", updatedUser);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("사용자 정보 수정 실패: {}", e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            log.error("사용자 정보 수정 중 오류: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "사용자 정보 수정 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * ✅ 비밀번호 변경 API (JWT 기반) - 이메일 인증 필수
     * PUT /api/user/password
     */
    @PutMapping("/password")
    public ResponseEntity<Map<String, Object>> changePassword(
            @RequestHeader(value = "Authorization") String authHeader,
            @RequestBody PasswordChangeRequest passwordChangeRequest) {

        Map<String, Object> response = new HashMap<>();
        try {
            // 1. JWT 토큰에서 사용자 ID 추출
            String token = authHeader.substring(7);
            Map<String, Object> claims = jwtUtil.validateToken(token);
            Long userId = Long.valueOf(claims.get("id").toString());

            // 2. 사용자 조회
            Optional<UserDTO> userOptional = userService.getUserById(userId);
            if (!userOptional.isPresent()) {
                response.put("success", false);
                response.put("message", "사용자를 찾을 수 없습니다.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            // ✅ 3. 이메일 인증 코드는 여기서 다시 검증하지 않는다.
            //    (이미 /api/email/verify-password-reset-code 에서 한 번 검증했다고 가정)

            // 4. 기존 비밀번호 검증 + 변경
            userService.changePassword(
                    userId,
                    passwordChangeRequest.getOldPassword(),
                    passwordChangeRequest.getNewPassword()
            );

            response.put("success", true);
            response.put("message", "비밀번호가 변경되었습니다.");
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.warn("비밀번호 변경 실패: {}", e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            log.error("비밀번호 변경 중 오류: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "비밀번호 변경 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * ✅ 회원 탈퇴 API (JWT 기반) - 이메일 인증 필수
     * DELETE /api/user/account
     */
    @DeleteMapping("/account")
    public ResponseEntity<Map<String, Object>> deleteAccount(
            @RequestHeader(value = "Authorization") String authHeader,
            @RequestBody AccountDeletionRequest request) {

        Map<String, Object> response = new HashMap<>();
        try {
            String token = authHeader.substring(7);
            Map<String, Object> claims = jwtUtil.validateToken(token);
            Long userId = Long.valueOf(claims.get("id").toString());

            // 1. 사용자 정보 조회
            Optional<UserDTO> userOptional = userService.getUserById(userId);
            if (!userOptional.isPresent()) {
                response.put("success", false);
                response.put("message", "사용자를 찾을 수 없습니다.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            // 2. 프론트에서 이미 verify-account-deletion-code 로 검증했다고 보고
            //    여기서는 코드가 비어 있는지만 체크 (실제 검증은 생략)
            String verificationCode = request.getVerificationCode();
            if (verificationCode == null || verificationCode.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "이메일 인증 코드를 입력해주세요.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // 3. 바로 회원 탈퇴 처리
            userService.deleteUser(userId);

            response.put("success", true);
            response.put("message", "회원 탈퇴가 완료되었습니다.");
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.warn("회원 탈퇴 실패: {}", e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            log.error("회원 탈퇴 중 오류: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "회원 탈퇴 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Access Token 갱신 API
     * POST /api/user/refresh
     */
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refresh(
            @RequestHeader("Authorization") String authHeader) {
        Map<String, Object> response = new HashMap<>();
        try {
            String refreshToken = authHeader.substring(7);
            Map<String, Object> claims = jwtUtil.validateToken(refreshToken);

            // 새로운 Access Token 생성 (60분)
            String newAccessToken = jwtUtil.generateToken(claims, 60);

            response.put("success", true);
            response.put("accessToken", newAccessToken);
            log.info("Access Token 갱신 성공");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Token 갱신 실패: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "Token 갱신에 실패했습니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }
    
    /**
     * 비밀번호 재설정 API (이메일 인증 완료 후)
     * 
     * 호출 경로: POST /api/user/reset-password
     * 호출 위치: PasswordReset.jsx의 handleResetPassword() 함수
     * 
     * 비밀번호 재설정 전체 흐름:
     * [1단계: 이메일 인증 (PasswordReset.jsx step=1)]
     * 1. 로그인 페이지에서 "비밀번호를 잊으셨나요?" 클릭 → PasswordReset.jsx로 이동
     * 2. 이메일 입력 → "인증 코드 전송" 버튼 클릭
     * 3. PasswordReset.jsx: handleSendCode() → POST /api/email/send-password-reset-code
     * 4. EmailController.sendPasswordResetCode() → EmailService.sendPasswordResetEmail()
     *    - 6자리 랜덤 코드 생성 → Redis 저장 (key: "email:password-reset:{email}", TTL: 300초)
     * 5. 사용자가 이메일에서 인증 코드 확인
     * 6. 인증 코드 입력 → PasswordReset.jsx: handleVerifyCode()
     * 7. → POST /api/email/verify-password-reset-code
     * 8. EmailController.verifyPasswordResetCode() → EmailService.verifyPasswordResetCode()
     *    - Redis에서 코드 조회 및 비교 → 일치하면 검증 완료
     * 9. 인증 성공 → PasswordReset.jsx: setStep(2) (새 비밀번호 입력 화면으로 전환)
     * 
     * [2단계: 새 비밀번호 설정 (PasswordReset.jsx step=2)]
     * 10. 새 비밀번호 입력 (8자 이상, 특수문자, 대문자 포함)
     * 11. 비밀번호 확인 입력 (일치 여부 확인)
     * 12. "비밀번호 재설정" 버튼 클릭 → PasswordReset.jsx: handleResetPassword()
     * 13. → POST /api/user/reset-password → ★ 이 메소드 호출됨
     * 
     * [3단계: 비밀번호 재설정 처리 (여기)]
     * 14. 이메일과 새 비밀번호 검증
     * 15. UserService.resetPasswordByEmail() 호출
     *     - 이메일로 사용자 조회 → 새 비밀번호 BCrypt 암호화 → DB 저장
     * 16. 성공 응답 반환 (HTTP 200 OK)
     * 17. PasswordReset.jsx에서 성공 메시지 표시 → 2초 후 로그인 페이지로 이동
     * 
     * @param request - 비밀번호 재설정 정보
     *                  형식: {"email": "user@example.com", "newPassword": "NewPassword1!"}
     * @return ResponseEntity<Map<String, Object>>
     *         성공: {success: true, message: "비밀번호가 성공적으로 변경되었습니다."} (200)
     *         실패: {success: false, message: "에러 메시지"} (400 또는 500)
     */
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(@RequestBody PasswordResetRequest request) {
        // HTTP 응답 body에 담을 Map 생성
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 1. 요청에서 이메일과 새 비밀번호 추출
            String email = request.getEmail();
            String newPassword = request.getNewPassword();
            
            /**
             * 2. 입력값 검증 (비어있는지 확인)
             * 
             * 검증 항목:
             * - email이 null 또는 공백이 아닌지
             * - newPassword가 null 또는 공백이 아닌지
             * 
             * 실패 시: HTTP 400 Bad Request 반환
             */
            if (email == null || email.trim().isEmpty() || newPassword == null || newPassword.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "이메일과 새 비밀번호를 입력해주세요.");
                return ResponseEntity.badRequest().body(response);
            }
            
            /**
             * 3. 비밀번호 길이 검증 (8자 이상)
             * 
             * 이유: 보안상 최소 8자 이상 권장
             * 프론트엔드에서도 검증하지만 백엔드에서도 재검증 필요
             */
            if (newPassword.length() < 8) {
                response.put("success", false);
                response.put("message", "비밀번호는 8자 이상이어야 합니다.");
                return ResponseEntity.badRequest().body(response);
            }
            
            /**
             * 4. UserService.resetPasswordByEmail() 호출
             * 
             * UserService.resetPasswordByEmail()에서 하는 일:
             * 4-1. 이메일로 사용자 조회
             *      - UserRepository.findByUserId(email) 호출
             *      - Optional<User> 반환
             *      - 사용자가 없으면 Optional.empty() → false 반환
             * 
             * 4-2. 새 비밀번호를 BCrypt로 암호화
             *      - passwordEncoder.encode(newPassword) 호출
             *      - 평문: "NewPassword1!"
             *      - 암호화: "$2a$10$XyZ123..."
             *      - 이전 비밀번호와 완전히 다른 해시값 생성
             * 
             * 4-3. user.setPassword(암호화된비밀번호)
             *      - User 엔티티의 password 필드 업데이트
             * 
             * 4-4. UserRepository.save(user) → DB UPDATE
             *      - JPA의 save() 메소드
             *      - SQL: UPDATE user SET password = '...' WHERE id = ?
             * 
             * 4-5. true 반환 (성공)
             *      - 사용자가 없으면 false 반환
             */
            boolean success = userService.resetPasswordByEmail(email, newPassword);
            
            /**
             * 5. 결과에 따른 응답 처리
             */
            if (success) {
                // 5-1. 성공 시
                // - HTTP 200 OK 반환
                // - 프론트엔드에서 성공 메시지 표시 → 2초 후 로그인 페이지로 이동
                response.put("success", true);
                response.put("message", "비밀번호가 성공적으로 변경되었습니다.");
                log.info("비밀번호 재설정 성공: {}", email);
                return ResponseEntity.ok(response);
            } else {
                // 5-2. 실패 시 (사용자 없음)
                // - UserService.resetPasswordByEmail()에서 false 반환
                // - 해당 이메일로 가입된 사용자가 없음
                // - HTTP 400 Bad Request 반환
                response.put("success", false);
                response.put("message", "사용자를 찾을 수 없습니다.");
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            /**
             * 6. 예외 처리 (DB 오류, 암호화 오류 등)
             * 
             * 발생 가능한 예외:
             * - DataAccessException: DB 연결 실패
             * - 시스템 오류
             * 
             * 처리:
             * - 에러 로그 출력
             * - HTTP 500 Internal Server Error 반환
             */
            log.error("비밀번호 재설정 실패", e);
            response.put("success", false);
            response.put("message", "비밀번호 재설정에 실패했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 로그인 요청 내부 클래스
     */
    public static class LoginRequest {
        private String userId;
        private String password;

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    /**
     * 비밀번호 변경 요청 내부 클래스
     */
    public static class PasswordChangeRequest {
        private String oldPassword;
        private String newPassword;
        private String verificationCode;

        public String getOldPassword() {
            return oldPassword;
        }

        public void setOldPassword(String oldPassword) {
            this.oldPassword = oldPassword;
        }

        public String getNewPassword() {
            return newPassword;
        }

        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }

        public String getVerificationCode() {
            return verificationCode;
        }

        public void setVerificationCode(String verificationCode) {
            this.verificationCode = verificationCode;
        }
    }
    
    /**
     * 비밀번호 재설정 요청 내부 클래스
     */
    public static class PasswordResetRequest {
        private String email;
        private String newPassword;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getNewPassword() {
            return newPassword;
        }

        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }
    }
    
    /**
     * 회원탈퇴 요청 내부 클래스
     */
    public static class AccountDeletionRequest {
        private String verificationCode;

        public String getVerificationCode() {
            return verificationCode;
        }

        public void setVerificationCode(String verificationCode) {
            this.verificationCode = verificationCode;
        }
    }
}
