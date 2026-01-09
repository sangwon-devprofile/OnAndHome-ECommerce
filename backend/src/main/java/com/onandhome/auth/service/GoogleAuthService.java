package com.onandhome.auth.service;

import com.onandhome.auth.dto.GoogleTokenResponse;
import com.onandhome.auth.dto.GoogleUserInfo;
import com.onandhome.user.UserRepository;
import com.onandhome.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.UUID;

/**
 * 구글 OAuth 인증 서비스
 *
 * 역할: 구글 소셜 로그인의 OAuth 2.0 인증 흐름을 처리하는 서비스
 *
 * 전체 처리 흐름:
 * 1. [프론트엔드] "Google로 로그인" 버튼 클릭
 * 2. [GoogleAuthController.getLoginUrl()] 구글 로그인 URL 생성 및 반환
 * 3. [프론트엔드] 구글 로그인 페이지로 리다이렉트
 * 4. [구글] 사용자 로그인 및 권한 동의
 * 5. [구글] callback URL로 리다이렉트 (인증 코드 포함)
 * 6. [프론트엔드 GoogleCallback.js] 인증 코드를 백엔드로 전송
 * 7. [GoogleAuthController.callback()] → [이 서비스의 processGoogleLogin()]
 * 8. [이 서비스] 인증 코드로 액세스 토큰 획득
 * 9. [이 서비스] 액세스 토큰으로 사용자 정보 조회
 * 10. [이 서비스] 사용자 정보로 회원가입/로그인 처리
 * 11. [UserService] JWT 토큰 생성 및 반환
 * 12. [프론트엔드] JWT 저장 후 로그인 완료
 *
 * 주요 메서드:
 * - getLoginUrl(): 구글 로그인 URL 생성
 * - processGoogleLogin(): 인증 코드로 로그인 처리 (전체 흐름 조율)
 * - getAccessToken(): 인증 코드로 액세스 토큰 획득
 * - getUserInfo(): 액세스 토큰으로 사용자 정보 조회
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleAuthService {
    
    private final UserRepository userRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    // application.properties에서 주입받는 구글 OAuth 설정값
    @Value("${google.client-id}")
    private String clientId; // 구글 클라이언트 ID
    
    @Value("${google.client-secret}")
    private String clientSecret; // 구글 클라이언트 시크릿
    
    @Value("${google.redirect-uri}")
    private String redirectUri; // 구글 로그인 후 리다이렉트될 프론트엔드 URL
    
    @Value("${google.token-url}")
    private String tokenUrl;
    
    @Value("${google.user-info-url}")
    private String userInfoUrl;
    
    /**
     * 구글 인증 코드로 액세스 토큰 받기
     * 
     * 호출 위치: GoogleAuthController.callback() → processGoogleLogin() 내부
     * 
     * 처리 흐름:
     * 1. 구글 토큰 API에 보낼 요청 파라미터 구성
     *    - grant_type: "authorization_code" (인증 코드 교환 방식)
     *    - code: 구글로부터 받은 인증 코드
     *    - client_id, client_secret: OAuth 앱 인증 정보
     *    - redirect_uri: 콜백 URL (검증용)
     * 2. POST 요청으로 구글 토큰 API 호출
     * 3. 응답을 GoogleTokenResponse 객체로 변환
     * 4. accessToken 필드 포함한 응답 객체 반환
     * 
     * @param code 구글 인증 코드
     * @return GoogleTokenResponse 토큰 정보 객체
     */
    public GoogleTokenResponse getAccessToken(String code) {
        log.info("=== 구글 액세스 토큰 요청 ===");
        
        // 요청 헤더 설정 - Form 데이터 형식으로 전송
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        // 요청 바디 설정 - 구글 토큰 API에 필요한 파라미터들
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code"); // 권한 부여 타입
        params.add("client_id", clientId);              // 클라이언트 ID
        params.add("client_secret", clientSecret);      // 클라이언트 시크릿
        params.add("redirect_uri", redirectUri);        // 리다이렉트 URI
        params.add("code", code);                       // 인증 코드
        
        // HTTP 요청 엔티티 생성 (헤더 + 파라미터)
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        
        try {
            // POST 요청으로 구글 토큰 API 호출
            // tokenUrl: https://oauth2.googleapis.com/token
            ResponseEntity<GoogleTokenResponse> response = restTemplate.exchange(
                tokenUrl,                       // 구글 토큰 API URL
                HttpMethod.POST,                // POST 방식
                request,                        // 요청 데이터
                GoogleTokenResponse.class       // 응답을 이 클래스로 자동 변환
            );
            
            log.info("구글 액세스 토큰 받기 성공");
            return response.getBody();
        } catch (Exception e) {
            log.error("구글 액세스 토큰 받기 실패", e);
            throw new RuntimeException("구글 액세스 토큰 받기 실패", e);
        }
    }
    
    /**
     * 구글 액세스 토큰으로 사용자 정보 받기
     * 
     * 호출 위치: GoogleAuthController.callback() → processGoogleLogin() 내부
     * 
     * 처리 흐름:
     * 1. HTTP 헤더에 액세스 토큰 설정
     *    - Authorization: Bearer {accessToken}
     * 2. GET 요청으로 구글 사용자 정보 API 호출
     * 3. 응답을 GoogleUserInfo 객체로 변환
     * 4. 사용자 정보 반환 (이메일, 이름 등)
     * 
     * @param accessToken 구글 액세스 토큰
     * @return GoogleUserInfo 사용자 정보 객체
     */
    public GoogleUserInfo getUserInfo(String accessToken) {
        log.info("=== 구글 사용자 정보 요청 ===");
        
        // 요청 헤더 설정 - Bearer 토큰 방식
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);              // Authorization: Bearer {token}
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        // HTTP 요청 엔티티 생성 (헤더만 포함, GET 요청이므로 바디 없음)
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        try {
            // GET 요청으로 구글 사용자 정보 API 호출
            // userInfoUrl: https://www.googleapis.com/oauth2/v2/userinfo
            ResponseEntity<GoogleUserInfo> response = restTemplate.exchange(
                userInfoUrl,                    // 구글 사용자 정보 API URL
                HttpMethod.GET,                 // GET 방식
                request,                        // 요청 헤더
                GoogleUserInfo.class            // 응답을 이 클래스로 자동 변환
            );
            
            log.info("구글 사용자 정보 받기 성공");
            return response.getBody();
        } catch (Exception e) {
            log.error("구글 사용자 정보 받기 실패", e);
            throw new RuntimeException("구글 사용자 정보 받기 실패", e);
        }
    }
    
    /**
     * 구글 사용자 정보로 로그인 또는 회원가입 처리
     * 
     * 호출 위치: GoogleAuthController.callback()
     * 
     * 처리 흐름:
     * 1. provider("GOOGLE")와 providerId로 기존 회원 확인
     * 2. 기존 회원이면 해당 User 객체 반환
     * 3. 신규 회원이면:
     *    - 이메일 중복 체크 (중복 시 UUID 추가)
     *    - userId 생성 ("google_" + providerId)
     *    - 새 User 객체 생성 및 DB 저장
     * 4. User 객체 반환 → JWT 토큰 생성에 사용
     * 
     * @param googleUserInfo 구글에서 받은 사용자 정보
     * @return User DB에 저장된 사용자 객체
     */
    public User processGoogleLogin(GoogleUserInfo googleUserInfo) {
        log.info("=== 구글 로그인 처리 ===");
        
        // 소셜 로그인 제공자 정보
        String provider = "GOOGLE";                   // 제공자 이름
        String providerId = googleUserInfo.getId();   // 구글 고유 ID
        
        // 이미 가입된 사용자인지 확인 (provider + providerId로 조회)
        Optional<User> existingUser = userRepository.findByProviderAndProviderId(provider, providerId);
        
        // 기존 회원이면 로그인 처리 (해당 User 객체 반환)
        if (existingUser.isPresent()) {
            log.info("기존 구글 사용자 로그인: {}", providerId);
            return existingUser.get();
        }
        
        // 신규 사용자 생성 - 회원가입 처리
        log.info("신규 구글 사용자 회원가입: {}", providerId);
        
        // 이름 설정 (없으면 기본값)
        String name = googleUserInfo.getName() != null 
            ? googleUserInfo.getName()
            : "구글사용자";
        
        // 이메일 설정 및 중복 체크
        String email = googleUserInfo.getEmail();
        
        // 이메일이 이미 존재하는지 확인 (다른 계정과 중복 방지)
        if (email != null && userRepository.existsByEmail(email)) {
            log.warn("이메일이 이미 존재합니다: {}", email);
            // 이메일 중복 시 대체 이메일 생성
            email = "google_" + providerId + "@google.user";
        }
        
        // 고유한 userId 생성 ("google_" + providerId)
        String userId = "google_" + providerId;
        
        // userId가 이미 존재하는지 확인 (중복 시 UUID 추가)
        if (userRepository.existsByUserId(userId)) {
            userId = "google_" + providerId + "_" + UUID.randomUUID().toString().substring(0, 8);
        }
        
        // 새 User 객체 생성
        User newUser = User.builder()
            .userId(userId)                              // 생성한 userId
            .password(UUID.randomUUID().toString())      // 랜덤 비밀번호 (소셜 로그인이므로 사용 안함)
            .username(name)                              // 사용자 이름
            .email(email)                                // 이메일
            .provider(provider)                          // "GOOGLE"
            .providerId(providerId)                      // 구글 고유 ID
            .role(1)                                     // 일반 사용자 (0: 관리자, 1: 일반)
            .active(true)                                // 활성화 상태
            .build();
        
        // DB에 저장하고 저장된 User 객체 반환
        return userRepository.save(newUser);}
    }

