package com.onandhome.auth.service;

import java.util.Optional; // JSON 파싱을 위해 사용하는 Jackson의 핵심 객체
import java.util.UUID; // 카카오 토큰 응답 DTO

import org.springframework.beans.factory.annotation.Value; // 카카오 사용자 정보 DTO
import org.springframework.http.HttpEntity; // DB에서 User 엔티티를 조회/저장하는 저장소
import org.springframework.http.HttpHeaders; // User 엔티티
import org.springframework.http.HttpMethod; // final 필드를 자동 생성자로 생성해주는 Lombok
import org.springframework.http.MediaType; // 로그 출력용 Lombok
import org.springframework.http.ResponseEntity; // application.yml에 있는 값을 주입받기 위한 어노테이션
import org.springframework.stereotype.Service; // HTTP 요청/응답 관련 클래스들
import org.springframework.util.LinkedMultiValueMap; // 스프링 서비스 빈으로 등록
import org.springframework.util.MultiValueMap; // key-value 형태의 body를 보내기 위한 Map
import org.springframework.web.client.RestTemplate; // 그 Map의 인터페이스

import com.onandhome.auth.dto.KakaoTokenResponse;
import com.onandhome.auth.dto.KakaoUserInfo;
import com.onandhome.user.UserRepository;
import com.onandhome.user.entity.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j // 로그 출력 기능 사용
@Service // 스프링 서비스 계층 등록
@RequiredArgsConstructor // final 필드(=userRepository)를 자동으로 생성자 주입
public class KakaoAuthService {

    // 사용자 정보를 찾거나 저장할 때 사용하는 Repository
    private final UserRepository userRepository;

    // 외부 API 호출용 객체 (카카오 서버와 통신)
    private final RestTemplate restTemplate = new RestTemplate();

    // application.yml에서 값 주입 (client-id)
    @Value("${kakao.client-id}")
    private String clientId;

    // 카카오가 인증 후 리다이렉트 시키는 URI
    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    // 액세스 토큰을 요청하는 카카오 API 주소
    @Value("${kakao.token-url}")
    private String tokenUrl;

    // 사용자 정보를 요청하는 카카오 API 주소
    @Value("${kakao.user-info-url}")
    private String userInfoUrl;


    /**
     * 카카오 인증 코드로 액세스 토큰 받기
     * 사용자가 로그인하면 카카오가 code를 보내주고
     * 그 code로 액세스 토큰을 발급받는 역할
     */
    public KakaoTokenResponse getAccessToken(String code) {
        log.info("=== 카카오 액세스 토큰 요청 ===");

        // HTTP 요청 헤더 설정 (폼 전송 방식)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // POST로 보낼 파라미터 생성
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code"); // 인증 방식 (고정)
        params.add("client_id", clientId); // 카카오 REST API 키
        params.add("redirect_uri", redirectUri); // 설정한 리다이렉트 URI
        params.add("code", code); // 카카오가 보내준 인가 코드

        // 헤더 + 바디를 하나의 객체로 합침
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            // 카카오 토큰 API 호출
            ResponseEntity<KakaoTokenResponse> response = restTemplate.exchange(
                    tokenUrl, // 호출할 URL
                    HttpMethod.POST, // POST로 호출
                    request, // 우리가 만든 request 객체
                    KakaoTokenResponse.class // 응답을 매핑할 DTO
            );

            log.info("카카오 액세스 토큰 받기 성공");
            return response.getBody();
        } catch (Exception e) {
            log.error("카카오 액세스 토큰 받기 실패", e);
            throw new RuntimeException("카카오 액세스 토큰 받기 실패", e);
        }
    }


    /**
     * 카카오 액세스 토큰으로 사용자 정보 받기
     */
    public KakaoUserInfo getUserInfo(String accessToken) {
        log.info("=== 카카오 사용자 정보 요청 ===");

        // HTTP 요청 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken); // Authorization: Bearer 액세스토큰
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // GET 요청은 바디가 필요 없어서 헤더만 넣음
        HttpEntity<String> request = new HttpEntity<>(headers);

        try {
            // 카카오 사용자 정보 API 호출
            ResponseEntity<KakaoUserInfo> response = restTemplate.exchange(
                    userInfoUrl, // 호출할 URL
                    HttpMethod.GET, // GET 방식
                    request, // 헤더만 있는 request
                    KakaoUserInfo.class // 응답을 매핑할 DTO
            );

            log.info("카카오 사용자 정보 받기 성공");
            return response.getBody();
        } catch (Exception e) {
            log.error("카카오 사용자 정보 받기 실패", e);
            throw new RuntimeException("카카오 사용자 정보 받기 실패", e);
        }
    }


    /**
     * 카카오 사용자 정보로 로그인 또는 회원가입 처리
     * 기존 계정이 있으면 로그인
     * 없으면 회원가입 후 로그인 처리
     */
    public User processKakaoLogin(KakaoUserInfo kakaoUserInfo) {
        log.info("=== 카카오 로그인 처리 ===");

        String provider = "KAKAO"; // 어떤 로그인 제공자인지 저장
        String providerId = String.valueOf(kakaoUserInfo.getId()); // 카카오 UID → 문자열로 변환

        // provider + providerId 조합으로 기존 회원 찾기
        Optional<User> existingUser = userRepository.findByProviderAndProviderId(provider, providerId);

        // 이미 존재하는 사용자면 바로 로그인 처리
        if (existingUser.isPresent()) {
            log.info("기존 카카오 사용자 로그인: {}", providerId);
            return existingUser.get();
        }

        // 존재하지 않는 경우 → 신규 회원가입 처리
        log.info("신규 카카오 사용자 회원가입: {}", providerId);

        // 카카오 닉네임이 존재하는지 안전하게 확인하는 코드 (null-safe)
        String nickname = kakaoUserInfo.getKakaoAccount() != null
                && kakaoUserInfo.getKakaoAccount().getProfile() != null
                ? kakaoUserInfo.getKakaoAccount().getProfile().getNickname()
                : "카카오사용자"; // 닉네임 정보가 없으면 기본값 생성

        // 이메일도 존재 여부 확인해서 저장
        String email = kakaoUserInfo.getKakaoAccount() != null
                ? kakaoUserInfo.getKakaoAccount().getEmail()
                : null;

        // 이메일이 이미 가입된 이메일인지 확인
        if (email != null && userRepository.existsByEmail(email)) {
            log.warn("이메일이 이미 존재합니다: {}", email);
            // 중복 방지를 위해 별도 이메일 생성
            email = "kakao_" + providerId + "@kakao.user";
        }

        // userId 생성 (예: kakao_123456)
        String userId = "kakao_" + providerId;

        // userId가 이미 존재하는지 확인
        if (userRepository.existsByUserId(userId)) {
            // 중복이면 랜덤 ID 추가해서 충돌 방지
            userId = "kakao_" + providerId + "_" + UUID.randomUUID().toString().substring(0, 8);
        }

        // User 엔티티 생성
        User newUser = User.builder()
                .userId(userId) // 유저 ID
                .password(UUID.randomUUID().toString()) // 소셜 로그인은 비밀번호 사용 안 함 → 랜덤 값 저장
                .username(nickname) // 닉네임
                .email(email) // 이메일 (중복 시 수정됨)
                .provider(provider) // 로그인 제공자
                .providerId(providerId) // 제공자 ID
                .role(1) // 기본 사용자 권한
                .active(true) // 활성 계정 여부
                .build();

        // DB 저장 후 반환
        return userRepository.save(newUser);
    }
}

/*
오약
1. KakaoAuthService는 카카오 로그인 과정 전체를 담당하는 서비스
2. getAccessToken()은 인가코드를 액세스 토큰으로 교환하는 역할
3. getUserInfo()는 액세스 토큰으로 카카오 프로필 정보를 가져오는 메서드
4. processKakaoLogin()은 기존 회원이면 로그인, 아니면 새로 회원가입 함
5.외부 API 호출은 RestTemplate을 쓰고, JSON은 DTO로 자동 매핑됨
 */