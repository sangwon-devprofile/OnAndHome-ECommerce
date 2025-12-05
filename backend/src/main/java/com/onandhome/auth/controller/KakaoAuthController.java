package com.onandhome.auth.controller;

import com.onandhome.auth.dto.KakaoTokenResponse; // 토큰 응답 DTO
import com.onandhome.auth.dto.KakaoUserInfo; // 사용자 정보 DTO
import com.onandhome.auth.service.KakaoAuthService; // 카카오 로그인 핵심 로직 서비스
import com.onandhome.user.entity.User; // User 엔티티
import com.onandhome.util.JWTUtil; // JWT 생성 유틸
import jakarta.servlet.http.HttpSession; // 세션 저장/조회용
import lombok.RequiredArgsConstructor; // final 필드 생성자 주입
import lombok.extern.slf4j.Slf4j; // 로그 출력
import org.springframework.beans.factory.annotation.Value; // application.yml 값 주입
import org.springframework.http.ResponseEntity; // HTTP 응답 래핑
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j // 로그 기능 활성화
@RestController // REST API 컨트롤러
@RequestMapping("/api/auth/kakao") // 공통 URL prefix
@RequiredArgsConstructor // final 필드를 자동 생성자로 주입
public class KakaoAuthController {

    // 카카오 인증/로그인 처리 서비스
    private final KakaoAuthService kakaoAuthService;

    // JWT 토큰 생성 유틸
    private final JWTUtil jwtUtil;

    // application.yml ↓
    @Value("${kakao.client-id}")
    private String clientId;

    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    @Value("${kakao.auth-url}")
    private String authUrl;


    /**
     * 카카오 로그인 URL 반환
     * 프론트에서 이 URL을 받아서 카카오 로그인 페이지로 이동함
     * GET /api/auth/kakao/login-url
     */
    @GetMapping("/login-url")
    public ResponseEntity<Map<String, String>> getKakaoLoginUrl() {
        log.info("=== 카카오 로그인 URL 요청 ===");

        // 카카오 로그인 페이지 URL 직접 구성
        String loginUrl = authUrl
                + "?client_id=" + clientId
                + "&redirect_uri=" + redirectUri
                + "&response_type=code"; // 카카오가 요구하는 고정값

        // 프론트로 보낼 응답 Map
        Map<String, String> response = new HashMap<>();
        response.put("loginUrl", loginUrl);

        log.info("카카오 로그인 URL: {}", loginUrl);
        return ResponseEntity.ok(response); // 200 OK + JSON 응답
    }

    /**
     * 카카오 로그인 콜백 처리
     * 카카오가 로그인 후 보낸 code를 받아서
     * 1) 액세스 토큰 발급
     * 2) 사용자 정보 조회
     * 3) 로그인 or 회원가입 처리
     * 4) 세션 저장
     * 5) JWT 발급
     *
     * GET /api/auth/kakao/callback?code=xxx
     */
    @GetMapping("/callback")
    public ResponseEntity<Map<String, Object>> kakaoCallback(
            @RequestParam("code") String code, // 카카오가 넘겨준 인가 코드
            HttpSession session) {             // 서버 세션 객체 (로그인 정보 저장)

        log.info("=== 카카오 로그인 콜백 처리 시작 ===");
        log.info("인증 코드: {}", code);

        // 클라이언트로 보낼 JSON 구성용 Map
        Map<String, Object> response = new HashMap<>();

        try {
            // 1) 액세스 토큰 요청
            KakaoTokenResponse tokenResponse = kakaoAuthService.getAccessToken(code);
            log.info("액세스 토큰 받기 성공");

            // 2) 액세스 토큰으로 사용자 정보 요청
            KakaoUserInfo kakaoUserInfo = kakaoAuthService.getUserInfo(tokenResponse.getAccessToken());
            log.info("사용자 정보 받기 성공: {}", kakaoUserInfo.getId());

            // 3) 로그인 or 회원가입 처리
            User user = kakaoAuthService.processKakaoLogin(kakaoUserInfo);
            log.info("카카오 로그인 처리 완료: userId={}, username={}", user.getUserId(), user.getUsername());

            // 4) 세션에 로그인 정보 저장
            session.setAttribute("userId", user.getUserId());
            session.setAttribute("username", user.getUsername());
            session.setAttribute("role", user.getRole());

            // 5) JWT 생성 (Access + Refresh)
            Map<String, Object> claims = new HashMap<>();
            claims.put("id", user.getId());
            claims.put("userId", user.getUserId());
            claims.put("role", user.getRole());

            String accessToken = jwtUtil.generateToken(claims, 60);               // 60분 유효
            String refreshToken = jwtUtil.generateToken(claims, 60 * 24 * 7);     // 7일 유효

            // 응답 JSON 구성
            response.put("success", true);
            response.put("message", "카카오 로그인 성공");
            response.put("accessToken", accessToken);
            response.put("refreshToken", refreshToken);
            response.put("user", Map.of(
                    "id", user.getId(),
                    "userId", user.getUserId(),
                    "username", user.getUsername(),
                    "email", user.getEmail() != null ? user.getEmail() : "",
                    "role", user.getRole()
            ));

            return ResponseEntity.ok(response); // 200 + 로그인 성공 JSON

        } catch (Exception e) {
            log.error("카카오 로그인 실패", e);

            response.put("success", false);
            response.put("message", "카카오 로그인 중 오류: " + e.getMessage());

            return ResponseEntity.status(500).body(response); // 500 에러 응답
        }
    }
}

/*
요약
1. 이 컨트롤러는 프론트에게 로그인 URL 제공하고, 카카오에서 받은 code를 처리함
2. 콜백에서 액세스 토큰 받고 → 사용자 정보 조회 → 로그인/회원가입 처리함
3. 로그인 성공하면 세션에 사용자 정보를 저장
4. JWTUtil로 Access/Refresh Token을 발급해 응답으로 내려줌
5. 전체 흐름이 카카오 OAuth 인증의 표준 구조 그대로 구현돼 있음
 */