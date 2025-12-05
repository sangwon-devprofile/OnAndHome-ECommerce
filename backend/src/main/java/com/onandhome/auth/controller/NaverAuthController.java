package com.onandhome.auth.controller;

import com.onandhome.auth.dto.NaverTokenResponse;
import com.onandhome.auth.dto.NaverUserInfo;
import com.onandhome.auth.service.NaverAuthService;
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
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/auth/naver")
@RequiredArgsConstructor
public class NaverAuthController {

    private final NaverAuthService naverAuthService;
    // 네이버 인증 관련 비즈니스 로직을 처리하는 서비스
    // final: 한 번 할당되면 변경 불가
    // @RequiredArgsConstructor가 자동으로 생성자를 만들어서 주입해줌
    private final JWTUtil jwtUtil;
    // JWT 토큰 생성/검증을 위한 유틸리티 클래스

    @Value("${naver.client-id}")
    private String clientId;
    // application.properties에서 naver.client-id 값을 읽어옴
    // 실제 값: "Cm6XtMBwYekKEI8m6eEg"

    @Value("${naver.redirect-uri}")
    private String redirectUri;
    // application.properties에서 naver.redirect-uri 값을 읽어옴
    // 실제 값: "http://localhost:3000/auth/naver/callback"

    @Value("${naver.auth-url}")
    private String authUrl;
    // application.properties에서 naver.auth-url 값을 읽어옴
    // 실제 값: "https://nid.naver.com/oauth2.0/authorize"

    /**
     * 네이버 로그인 URL 반환
     * GET /api/auth/naver/login-url
     */
    @GetMapping("/login-url")
    public ResponseEntity<Map<String, String>> getNaverLoginUrl() {
        // ResponseEntity: HTTP 응답을 만들기 위한 Spring 클래스
        // Map<String, String>: JSON 형태로 변환될 데이터 타입
        // 메서드 이름: getNaverLoginUrl
        log.info("=== 네이버 로그인 URL 요청 ===");

        // CSRF 방지를 위한 state 값 생성
        String state = UUID.randomUUID().toString();
        // UUID.randomUUID(): 고유한 랜덤 문자열 생성
        // 예: "3f5a7b2e-9c1d-4e6f-8a3b-0d2c4f1e9a7b"
        // toString(): UUID 객체를 문자열로 변환

        String loginUrl = authUrl
                + "?client_id=" + clientId
                + "&redirect_uri=" + redirectUri
                + "&response_type=code"
                + "&state=" + state;
        // authUrl: "https://nid.naver.com/oauth2.0/authorize"
        // + "?client_id=" + clientId → "?client_id=Cm6XtMBwYekKEI8m6eEg"
        // + "&redirect_uri=" + redirectUri → "&redirect_uri=http://localhost:3000/auth/naver/callback"
        // + "&response_type=code" → 인증 코드 방식 사용 (OAuth2 표준)
        // + "&state=" + state → "&state=3f5a7b2e-9c1d-4e6f-8a3b-0d2c4f1e9a7b"

        Map<String, String> response = new HashMap<>();
        // HashMap: 키-값 쌍으로 데이터를 저장하는 자료구조
        // String, String: 키도 문자열, 값도 문자열
        response.put("loginUrl", loginUrl);
        // "loginUrl"이라는 키로 생성한 네이버 로그인 URL을 저장
        response.put("state", state);
        // "state"라는 키로 생성한 state 값을 저장

        log.info("네이버 로그인 URL: {}", loginUrl);
        return ResponseEntity.ok(response);
        // ResponseEntity.ok(): HTTP 200 OK 상태 코드와 함께 응답
        // response 객체를 자동으로 JSON으로 변환하여 클라이언트에게 전송
        // 프론트엔드는 이 JSON 데이터를 받게 됨
    }

    /**
     * 네이버 로그인 콜백 처리
     * GET /api/auth/naver/callback?code=xxx&state=xxx
     */
    @GetMapping("/callback")
    // HTTP GET 요청 처리
    // URL: /api/auth/naver/callback
    public ResponseEntity<Map<String, Object>> naverCallback(
            // ResponseEntity: HTTP 응답을 만들기 위한 클래스
            // Map<String, Object>: JSON으로 변환될 응답 데이터
            @RequestParam("code") String code,
            // URL 쿼리 파라미터에서 "code" 값을 추출하여 code 변수에 저장
            // 예: ?code=aBcDeFg... → code = "aBcDeFg..."
            @RequestParam("state") String state,
            // URL 쿼리 파라미터에서 "state" 값을 추출
            HttpSession session) {
            // HttpSession: 사용자의 세션 객체
            // 세션에 로그인 정보를 저장할 수 있음

        log.info("=== 네이버 로그인 콜백 처리 시작 ===");
        log.info("인증 코드: {}", code);
        log.info("State: {}", state);

        Map<String, Object> response = new HashMap<>();
        // 응답 데이터를 담을 Map 객체 생성
        // String: 키 이름, Object: 값 (어떤 타입이든 가능)

        try {
            // 1. 네이버 액세스 토큰 받기
            NaverTokenResponse tokenResponse = naverAuthService.getAccessToken(code, state);
            // naverAuthService: 네이버 인증 관련 비즈니스 로직을 처리하는 서비스
            // getAccessToken(): code와 state를 네이버 서버로 보내서 액세스 토큰을 받아옴
            // tokenResponse: 네이버가 발급한 토큰 정보를 담은 객체
            log.info("액세스 토큰 받기 성공");

            // 2. 사용자 정보 받기
            NaverUserInfo naverUserInfo = naverAuthService.getUserInfo(tokenResponse.getAccessToken());
            // getUserInfo(): 받은 액세스 토큰으로 네이버 사용자 정보 API를 호출
            // naverUserInfo: 네이버 사용자 정보를 담은 객체
            log.info("사용자 정보 받기 성공: {}", naverUserInfo.getResponse().getId());

            // 3. 로그인 처리
            User user = naverAuthService.processNaverLogin(naverUserInfo);
            // processNaverLogin(): 네이버 사용자 정보로 로그인 처리
            // - DB에서 해당 네이버 ID로 가입된 사용자를 찾음
            // - 없으면 새로 회원가입 처리
            // - 있으면 기존 사용자 정보 반환
            // user: 최종 로그인된 사용자 엔티티
            log.info("네이버 로그인 처리 완료: userId={}, username={}", user.getUserId(), user.getUsername());

            // 4. 세션 저장
            session.setAttribute("userId", user.getUserId());
            // 세션에 "userId"라는 이름으로 사용자 ID를 저장
            // 서버 측에서 로그인 상태를 유지하기 위함
            session.setAttribute("username", user.getUsername());
            // 세션에 사용자 이름 저장
            session.setAttribute("role", user.getRole());
            // 세션에 사용자 권한(role) 저장
            // role: 0 = 관리자, 1 = 일반 사용자

            // 5. JWT 토큰 생성
            Map<String, Object> claims = new HashMap<>();
            // JWT 토큰에 포함할 정보(claims)를 담을 Map 생성
            claims.put("id", user.getId());
            // 사용자의 DB primary key ID
            claims.put("userId", user.getUserId());
            // 사용자의 로그인 ID
            claims.put("role", user.getRole());
            // 사용자 권한

            // Access / Refresh Token 발급
            String accessToken = jwtUtil.generateToken(claims, 60);
            // jwtUtil: JWT 토큰을 생성/검증하는 유틸리티
            // generateToken(claims, 60):
            //   - claims: 토큰에 담을 정보
            //   - 60: 유효 시간 (60분)
            // accessToken: 생성된 JWT 액세스 토큰
            // 예: "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
            String refreshToken = jwtUtil.generateToken(claims, 60 * 24 * 7);
            // refreshToken: 리프레시 토큰 생성
            // 유효 시간: 60분 * 24시간 * 7일 = 7일
            // 액세스 토큰이 만료되면 리프레시 토큰으로 새로 발급받을 수 있음

            // 응답
            response.put("success", true);
            // 로그인 성공 여부
            response.put("message", "네이버 로그인 성공");
            // 성공 메시지
            response.put("accessToken", accessToken);
            // JWT 액세스 토큰
            response.put("refreshToken", refreshToken);
            // JWT 리프레시 토큰
            response.put("user", Map.of(
                    "id", user.getId(),
                    "userId", user.getUserId(),
                    "username", user.getUsername(),
                    "email", user.getEmail() != null ? user.getEmail() : "",
                    "role", user.getRole()
            ));

            return ResponseEntity.ok(response);
            // ResponseEntity.ok(): HTTP 200 OK 상태로 응답
            // response 객체를 JSON으로 변환하여 클라이언트에 전송
            // 프론트엔드는 이 JSON 데이터를 받게 됨

        } catch (Exception e) {
            // try 블록에서 에러 발생 시 여기로 옴
            log.error("네이버 로그인 실패", e);
            response.put("success", false);
            // 실패 표시
            response.put("message", "네이버 로그인 중 오류: " + e.getMessage());
            // 에러 메시지
            return ResponseEntity.status(500).body(response);
            // HTTP 500 Internal Server Error 상태로 응답
            // status(500): 상태 코드 500
            // body(response): 응답 본문에 에러 정보 포함
        }
    }
}