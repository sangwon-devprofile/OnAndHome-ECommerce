package com.onandhome.auth.controller;

import com.onandhome.auth.dto.GoogleTokenResponse;
import com.onandhome.auth.dto.GoogleUserInfo;
import com.onandhome.auth.service.GoogleAuthService;
import com.onandhome.user.entity.User;
import com.onandhome.util.JWTUtil;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth/google")
@RequiredArgsConstructor
public class GoogleAuthController {

    private final GoogleAuthService googleAuthService;
    private final JWTUtil jwtUtil;

    @Value("${google.client-id}")
    private String clientId;

    @Value("${google.redirect-uri}")
    private String redirectUri;

    @Value("${google.auth-url}")
    private String authUrl;

    /**
     * 구글 로그인 URL 반환
     * 
     * API: GET /api/auth/google/login-url
     * 
     * 호출 위치: 프론트엔드 Login.js 또는 회원가입 페이지
     * 
     * 처리 흐름:
     * 1. 프론트엔드에서 이 API 호출
     * 2. 구글 OAuth URL 생성 (client_id, redirect_uri 등 포함)
     * 3. URL을 JSON 형태로 프론트엔드에 반환
     * 4. 프론트엔드가 window.location.href로 해당 URL로 이동
     * 
     * 응답 예시:
     * {
     *   "loginUrl": "https://accounts.google.com/o/oauth2/v2/auth?client_id=...&redirect_uri=..."
     * }
     * 
     * 데이터 흐름:
     * [프론트엔드] → GET /api/auth/google/login-url
     * → [이 메서드] → URL 생성 → JSON 응답
     * → [프론트엔드] → window.location.href = loginUrl
     * 
     * @return 구글 로그인 URL을 포함한 Map
     */
    @GetMapping("/login-url")
    public ResponseEntity<Map<String, String>> getGoogleLoginUrl() {
        log.info("=== 구글 로그인 URL 요청 ===");

        // 구글 OAuth 인증 URL 구성
        // authUrl: https://accounts.google.com/o/oauth2/v2/auth
        String loginUrl = authUrl
                + "?client_id=" + clientId                // 클라이언트 ID
                + "&redirect_uri=" + redirectUri          // 콜백 URL
                + "&response_type=code"                   // 인증 코드 방식
                + "&scope=openid email profile";         // 요청 권한

        // JSON 응답 데이터 구성
        Map<String, String> response = new HashMap<>();
        response.put("loginUrl", loginUrl);

        log.info("구글 로그인 URL: {}", loginUrl);
        return ResponseEntity.ok(response);
    }

    /**
     * 구글 로그인 콜백 처리
     * 
     * API: GET /api/auth/google/callback?code=xxx
     * 
     * 호출 위치: 구글에서 리다이렉트된 후 프론트엔드가 자동 호출
     * 
     * 처리 흐름:
     * 1. 구글이 redirect_uri로 리다이렉트 (인증 코드 포함)
     * 2. 프론트엔드가 이 API로 인증 코드 전달
     * 3. GoogleAuthService로 전체 로그인 로직 처리:
     *    - code로 액세스 토큰 획득
     *    - 액세스 토큰으로 사용자 정보 조회
     *    - 이메일로 기존 회원 확인
     *    - 신규 회원이면 회원가입, 기존 회원이면 로그인
     * 4. JWT 토큰 생성 (Access + Refresh)
     * 5. 세션에 사용자 정보 저장
     * 6. JWT 토큰 및 사용자 정보를 프론트엔드로 반환
     * 7. 프론트엔드가 JWT를 localStorage에 저장
     * 8. 프론트엔드가 Redux에 로그인 상태 저장
     * 9. 메인 페이지로 리다이렉트
     * 
     * 데이터 흐름:
     * [구글] → redirect_uri + code → [프론트엔드]
     * → GET /api/auth/google/callback?code=xxx
     * → [이 메서드] → GoogleAuthService
     * → getAccessToken() → getUserInfo() → processGoogleLogin()
     * → User 객체 반환 → JWT 생성
     * → 응답 반환 → [프론트엔드]
     * → localStorage + Redux 저장 → 메인 페이지
     * 
     * @param code 구글에서 받은 인증 코드
     * @param session HTTP 세션
     * @return JWT 토큰 및 사용자 정보
     */
    @GetMapping("/callback")
    public ResponseEntity<Map<String, Object>> googleCallback(
            @RequestParam("code") String code,
            HttpSession session) {

        log.info("=== 구글 로그인 콜백 처리 시작 ===");
        log.info("인증 코드: {}", code);

        // 응답 데이터 객체
        Map<String, Object> response = new HashMap<>();

        try {
            // 1. 구글 액세스 토큰 받기
            // GoogleAuthService.getAccessToken() 호출
            // → 인증 코드로 구글 토큰 API 호출
            // → GoogleTokenResponse 객체 반환
            GoogleTokenResponse tokenResponse = googleAuthService.getAccessToken(code);
            log.info("액세스 토큰 받기 성공");

            // 2. 사용자 정보 받기
            // GoogleAuthService.getUserInfo() 호출
            // → 액세스 토큰으로 구글 사용자 정보 API 호출
            // → GoogleUserInfo 객체 반환 (id, email, name 등)
            GoogleUserInfo googleUserInfo = googleAuthService.getUserInfo(tokenResponse.getAccessToken());
            log.info("사용자 정보 받기 성공: {}", googleUserInfo.getId());

            // 3. 로그인 처리
            // GoogleAuthService.processGoogleLogin() 호출
            // → provider + providerId로 기존 회원 확인
            // → 없으면 신규 회원가입, 있으면 로그인
            // → User 객체 반환
            User user = googleAuthService.processGoogleLogin(googleUserInfo);
            log.info("구글 로그인 처리 완료: userId={}, username={}", user.getUserId(), user.getUsername());

            // 4. 세션에 사용자 정보 저장
            // 서버 측 세션에 로그인 상태 저장
            session.setAttribute("userId", user.getUserId());       // 사용자 ID
            session.setAttribute("username", user.getUsername());   // 사용자 이름
            session.setAttribute("role", user.getRole());           // 권한 (0: 관리자, 1: 일반)

            // 5. JWT 토큰 생성
            // JWT 페이로드(claims)에 사용자 정보 포함
            Map<String, Object> claims = new HashMap<>();
            claims.put("id", user.getId());              // 내부 ID
            claims.put("userId", user.getUserId());      // 사용자 ID
            claims.put("role", user.getRole());          // 권한

            // Access Token (1시간) & Refresh Token (7일) 발급
            // JWTUtil.generateToken()을 통해 토큰 생성
            String accessToken = jwtUtil.generateToken(claims, 60);          // 60분 = 1시간
            String refreshToken = jwtUtil.generateToken(claims, 60 * 24 * 7); // 7일

            // 6. 응답 데이터 구성
            // 프론트엔드로 반환할 데이터
            response.put("success", true);
            response.put("message", "구글 로그인 성공");
            response.put("accessToken", accessToken);      // 액세스 토큰
            response.put("refreshToken", refreshToken);    // 리프레시 토큰
            response.put("user", Map.of(                   // 사용자 정보
                    "id", user.getId(),
                    "userId", user.getUserId(),
                    "username", user.getUsername(),
                    "email", user.getEmail() != null ? user.getEmail() : "",
                    "role", user.getRole()
            ));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            // 에러 처리
            log.error("구글 로그인 실패", e);
            response.put("success", false);
            response.put("message", "구글 로그인 중 오류: " + e.getMessage());
            return ResponseEntity.status(500).body(response);  // 500 Internal Server Error
        }
    }
}
