package com.onandhome.auth.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onandhome.auth.dto.NaverTokenResponse;
import com.onandhome.auth.dto.NaverUserInfo;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class NaverAuthService {

    private final UserRepository userRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${naver.client-id}")
    private String clientId;

    @Value("${naver.client-secret}")
    private String clientSecret;

    @Value("${naver.redirect-uri}")
    private String redirectUri;

    @Value("${naver.token-url}")
    private String tokenUrl;

    @Value("${naver.user-info-url}")
    private String userInfoUrl;

    /**
     * 네이버 인증 코드로 액세스 토큰 받기
     */
    public NaverTokenResponse getAccessToken(String code, String state) {
        // 파라미터:
        //   code: 네이버가 발급한 인증 코드
        //   state: CSRF 방지용 state 값
        // 리턴: NaverTokenResponse (네이버 액세스 토큰 정보)
        log.info("=== 네이버 액세스 토큰 요청 ===");

        // 요청 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        // HttpHeaders: HTTP 요청의 헤더를 설정하는 객체
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        // Content-Type: application/x-www-form-urlencoded
        // Form 데이터 형식으로 요청을 보냄
        // key1=value1&key2=value2 형태

        // 요청 바디 설정
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        // MultiValueMap: 하나의 키에 여러 값을 가질 수 있는 Map
        // LinkedMultiValueMap: 순서를 유지하는 구현체
        params.add("grant_type", "authorization_code");
        // OAuth2 인증 방식 지정
        // "authorization_code": 인증 코드 방식
        params.add("client_id", clientId);
        // 네이버 개발자 센터에서 발급받은 클라이언트 ID
        // 실제 값: "Cm6XtMBwYekKEI8m6eEg"
        params.add("client_secret", clientSecret);
        // 네이버 개발자 센터에서 발급받은 클라이언트 시크릿
        // 실제 값: "YXhQ750t7C"
        // 보안상 중요한 값이므로 노출되면 안됨
        params.add("code", code);
        // 네이버가 발급한 인증 코드
        // 예: "aBcDeFgHiJkLmNoPqRsTuVwXyZ"
        params.add("state", state);
        // CSRF 방지용 state 값
        // 예: "3f5a7b2e-9c1d-4e6f-8a3b-0d2c4f1e9a7b"

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        // HttpEntity: HTTP 요청의 본문과 헤더를 담는 객체
        // params: 요청 본문 (Form 데이터)
        // headers: 요청 헤더

        try {
            ResponseEntity<NaverTokenResponse> response = restTemplate.exchange(
                    // restTemplate: HTTP 요청을 보내는 Spring 클래스
                    // exchange(): HTTP 요청을 실행하고 응답을 받음
                    tokenUrl,
                    // 요청을 보낼 URL
                    // 실제 값: "https://nid.naver.com/oauth2.0/token"
                    HttpMethod.POST,
                    request,
                    NaverTokenResponse.class
                    // 응답을 변환할 클래스 타입
                    // JSON 응답이 자동으로 NaverTokenResponse 객체로 변환됨
            );

            log.info("네이버 액세스 토큰 받기 성공");
            return response.getBody();
            // ResponseEntity에서 본문만 추출하여 반환
            // NaverTokenResponse 객체가 반환됨
        } catch (Exception e) {
            // HTTP 요청 실패 또는 네트워크 에러 발생 시
            log.error("네이버 액세스 토큰 받기 실패", e);
            throw new RuntimeException("네이버 액세스 토큰 받기 실패", e);
            // RuntimeException을 발생시켜 호출한 쪽으로 에러 전파
            // 컨트롤러의 catch 블록에서 처리됨
        }
    }

    /**
     * 네이버 액세스 토큰으로 사용자 정보 받기
     */
    public NaverUserInfo getUserInfo(String accessToken) {
        // 파라미터: accessToken - 네이버 액세스 토큰
        // 리턴: NaverUserInfo - 네이버 사용자 정보
        log.info("=== 네이버 사용자 정보 요청 ===");

        // 요청 헤더 설정
        HttpHeaders headers = new HttpHeaders();// HTTP 헤더 객체 생성
        headers.setBearerAuth(accessToken);
        // Authorization 헤더에 Bearer 토큰 설정
        // 실제 헤더: "Authorization: Bearer AAAAQosjWDJi..."
        // setBearerAuth(): "Bearer " 접두사를 자동으로 추가해줌

        HttpEntity<String> request = new HttpEntity<>(headers);
        // HttpEntity: HTTP 요청 객체
        // <String>: 본문 타입 (본문은 없고 헤더만 있음)
        // headers: 위에서 설정한 헤더 객체

        try {
            ResponseEntity<NaverUserInfo> response = restTemplate.exchange(
                    // restTemplate.exchange(): HTTP 요청 실행
                    userInfoUrl,
                    // 요청 URL
                    // 실제 값: "https://openapi.naver.com/v1/nid/me"
                    HttpMethod.GET, // HTTP GET 메서드
                    request, // HTTP 요청 객체 (헤더 포함)
                    NaverUserInfo.class // 응답을 변환할 클래스 타입
            );

            log.info("네이버 사용자 정보 받기 성공");
            return response.getBody();
            // 응답 본문을 NaverUserInfo 객체로 반환
        } catch (Exception e) {
            log.error("네이버 사용자 정보 받기 실패", e);
            throw new RuntimeException("네이버 사용자 정보 받기 실패", e);
            // 예외를 다시 던져서 호출한 쪽으로 전파
        }
    }

    /**
     * 네이버 사용자 정보로 로그인 또는 회원가입 처리
     */
    public User processNaverLogin(NaverUserInfo naverUserInfo) {
        // 파라미터: naverUserInfo - 네이버에서 받은 사용자 정보
        // 리턴: User - 로그인된 사용자 엔티티
        log.info("=== 네이버 로그인 처리 ===");

        String provider = "NAVER";
        // 소셜 로그인 제공자 이름
        // DB에 "NAVER"로 저장됨
        String providerId = naverUserInfo.getResponse().getId();
        // 네이버 고유 사용자 ID
        // naverUserInfo.getResponse(): NaverAccount 객체
        // .getId(): 네이버 사용자 ID (예: "32742776")


        // 이미 가입된 사용자인지 확인
        Optional<User> existingUser = userRepository.findByProviderAndProviderId(provider, providerId);
        // userRepository: JPA Repository (DB 접근)
        // findByProviderAndProviderId():
        //   - provider = "NAVER"
        //   - providerId = "32742776"
        //   조건으로 사용자 검색
        // Optional<User>: 결과가 있을 수도, 없을 수도 있음
        //   - 있으면 Optional.of(user)
        //   - 없으면 Optional.empty()

        if (existingUser.isPresent()) {
            // existingUser.isPresent(): Optional에 값이 있는지 확인
            // true: 이미 가입된 사용자
            log.info("기존 네이버 사용자 로그인: {}", providerId);
            return existingUser.get();
            // Optional.get(): Optional에서 User 객체를 꺼냄
            // 기존 사용자 정보를 그대로 반환
            // ⭐ 여기서 메서드 종료 (신규 가입 로직은 실행 안됨)
        }

        // 신규 사용자 생성
        log.info("신규 네이버 사용자 회원가입: {}", providerId);

        NaverUserInfo.NaverAccount account = naverUserInfo.getResponse();
        // 네이버 계정 정보 객체를 변수에 저장
        // 이후 account.getNickname(), account.getEmail() 등으로 사용

        String nickname = account.getNickname() != null
                ? account.getNickname()
                : (account.getName() != null ? account.getName() : "네이버사용자");
        // 삼항 연산자 중첩 사용
        // 1순위: account.getNickname()이 null이 아니면 닉네임 사용
        // 2순위: 닉네임이 null이면 account.getName() 확인
        //   - name이 null이 아니면 name 사용
        //   - name도 null이면 "네이버사용자"로 기본값 설정
        //
        // 예시:
        //   nickname = "홍길동" (네이버에 닉네임이 있는 경우)
        //   nickname = "네이버사용자" (닉네임, 이름 둘 다 없는 경우)

        String email = account.getEmail();
        // 네이버 이메일 가져오기
        // 예: "hong@naver.com"
        // null일 수도 있음 (사용자가 이메일 제공 동의 안한 경우)

        // 이메일이 이미 존재하는지 확인
        if (email != null && userRepository.existsByEmail(email)) {
            // email != null: 이메일이 존재하고
            // userRepository.existsByEmail(email): DB에 이미 해당 이메일이 있으면
            log.warn("이메일이 이미 존재합니다: {}", email);
            // 이메일 중복 시 UUID 추가
            email = "naver_" + providerId + "@naver.user";
            // 이메일을 고유한 값으로 변경
            // 예: "naver_32742776@naver.user"
            // @naver.user는 실제 도메인이 아닌 가상의 도메인
            // 소셜 로그인 사용자는 이메일로 로그인하지 않으므로 문제없음
        }

        // 고유한 userId 생성 (naver_ + providerId)
        String userId = "naver_" + providerId;
        // 로그인 ID를 "naver_" + 네이버 ID로 생성
        // 예: "naver_32742776"

        // userId가 이미 존재하는지 확인
        if (userRepository.existsByUserId(userId)) {
            // DB에 이미 같은 userId가 존재하면 (거의 발생 안함)
            userId = "naver_" + providerId + "_" + UUID.randomUUID().toString().substring(0, 8);
            // UUID의 일부를 추가하여 고유하게 만듦
            // 예: "naver_32742776_a3b4c5d6"
            // substring(0, 8): UUID 문자열의 처음 8자만 사용
        }

        User newUser = User.builder()
                .userId(userId)
                .password(UUID.randomUUID().toString()) // 랜덤 비밀번호 (사용 안함)
                .username(nickname)
                .email(email)
                .provider(provider)
                .providerId(providerId)
                .role(1) // 일반 사용자
                .active(true)
                .build();

        return userRepository.save(newUser);
        // userRepository.save(): JPA Repository의 저장 메서드
        // newUser 엔티티를 DB의 users 테이블에 INSERT
        // 저장 후 ID가 자동 생성되어 newUser 객체에 설정됨
        // 저장된 User 객체를 반환
    }
}