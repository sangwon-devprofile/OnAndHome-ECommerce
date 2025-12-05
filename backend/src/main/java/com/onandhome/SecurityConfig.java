package com.onandhome;

import java.util.Arrays;
import java.util.List;

// JWT 토큰 생성/검증 유틸리티 - JWTCheckFilter에 주입됨
import com.onandhome.util.JWTUtil;
// Spring Bean 등록 어노테이션
import org.springframework.context.annotation.Bean;
// Spring 설정 클래스임을 나타내는 어노테이션
import org.springframework.context.annotation.Configuration;
// Spring Security HTTP 설정 빌더
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
// 세션 생성 정책 (JWT 사용 시 STATELESS 설정)
import org.springframework.security.config.http.SessionCreationPolicy;
// BCrypt 암호화 (비밀번호 해싱)
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
// 패스워드 인코더 인터페이스
import org.springframework.security.crypto.password.PasswordEncoder;
// Security 필터 체인 (Spring Security의 핵심)
import org.springframework.security.web.SecurityFilterChain;
// 기본 인증 필터 (JWT 필터를 이 필터 앞에 추가)
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
// CORS 설정 클래스
import org.springframework.web.cors.CorsConfiguration;
// CORS 설정 소스 인터페이스
import org.springframework.web.cors.CorsConfigurationSource;
// URL 기반 CORS 설정 구현체
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

// JWT 인증 필터 - 모든 요청을 가로채서 JWT 검증
import com.onandhome.security.filter.JWTCheckFilter;

// 로깅
import lombok.extern.slf4j.Slf4j;

/**
 * Spring Security 설정 클래스
 * 
 * 역할:
 * 1. JWT 기반 인증 시스템 설정
 * 2. CORS(Cross-Origin Resource Sharing) 정책 설정
 * 3. 세션 정책 설정 (Stateless)
 * 4. URL별 접근 권한 설정
 * 5. JWTCheckFilter를 Spring Security Filter Chain에 등록
 * 
 * 핵심 개념:
 * - JWT 방식: 서버가 세션을 관리하지 않음 (Stateless)
 * - 클라이언트가 매 요청마다 JWT 토큰을 Authorization 헤더에 포함
 * - JWTCheckFilter가 토큰을 검증하고 사용자 인증 정보를 SecurityContext에 저장
 * 
 * 실행 흐름:
 * 1. 클라이언트 요청 도착
 * 2. CORS 필터 실행 (Preflight 요청 처리)
 * 3. JWTCheckFilter 실행 (JWT 검증)
 * 4. Spring Security 권한 체크 (authorizeHttpRequests)
 * 5. 컨트롤러 메소드 실행
 */
@Configuration // Spring 설정 클래스임을 명시
@Slf4j // 로깅 기능 활성화
public class SecurityConfig {

    // JWT 토큰 생성/검증을 위한 유틸리티
    // JWTCheckFilter 생성 시 주입됨
    private final JWTUtil jwtUtil;

    /**
     * 생성자 주입 - Spring이 JWTUtil Bean을 자동으로 주입
     * @param jwtUtil - JWTUtil Bean (JWTUtil.java에서 @Component로 등록됨)
     */
    public SecurityConfig(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /**
     * BCrypt 패스워드 인코더 빈 등록
     * 
     * 역할:
     * - 회원가입 시 비밀번호를 암호화하여 DB에 저장
     * - 로그인 시 입력된 비밀번호와 DB의 암호화된 비밀번호 비교
     * 
     * 사용 위치:
     * - MemberService.register(): 회원가입 시 passwordEncoder.encode(pwd)
     * - MemberService.login(): 로그인 시 passwordEncoder.matches(inputPwd, dbPwd)
     * 
     * BCrypt 특징:
     * - 동일한 비밀번호라도 매번 다른 해시값 생성 (Salt 사용)
     * - 일방향 암호화 (복호화 불가능)
     * 
     * @return BCryptPasswordEncoder 객체 - Spring Container에서 관리되어 @Autowired로 주입 가능
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Spring Security 필터 체인 설정
     * 
     * 역할:
     * - Spring Security의 모든 보안 설정을 구성하는 핵심 메소드
     * - JWT 기반 인증, CORS, 세션 정책, URL 접근 권한 등을 설정
     * 
     * 실행 순서:
     * 1. CORS 필터 실행 (Preflight 요청 처리)
     * 2. JWTCheckFilter 실행 (Authorization 헤더에서 JWT 검증)
     * 3. Spring Security 권한 체크 (authorizeHttpRequests)
     * 4. 컨트롤러 메소드 실행
     * 
     * @param http - HttpSecurity 객체 (보안 설정을 위한 빌더)
     * @return SecurityFilterChain - 구성된 보안 필터 체인
     * @throws Exception - 설정 중 오류 발생 시
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        log.info("==================== Security Config ====================");

        /**
         * CORS(Cross-Origin Resource Sharing) 설정
         * 
         * 역할: 프론트엔드(http://localhost:3000)와 백엔드(http://localhost:8080) 간의
         *       크로스 도메인 요청 허용
         * 
         * 설정 내용: corsConfigurationSource() 메소드에서 정의
         * - 허용 Origin: http://localhost:3000, http://127.0.0.1:3000 등
         * - 허용 메소드: GET, POST, PUT, DELETE, OPTIONS
         * - 허용 헤더: Authorization (JWT 토큰 전송용)
         */
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));

        /**
         * 세션 관리 정책: STATELESS
         * 
         * STATELESS 의미:
         * - 서버가 세션을 생성하거나 저장하지 않음
         * - 매 요청마다 JWT 토큰을 검증하여 인증 처리
         * - 서버 확장성(Scalability)이 좋음
         * 
         * JWT 방식의 특징:
         * - 클라이언트가 토큰을 보관 (localStorage)
         * - 서버는 토큰을 검증하기만 함 (저장 안 함)
         * - 로그아웃 = 클라이언트에서 토큰 삭제
         */
        http.sessionManagement(sessionConfig ->
                sessionConfig.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        );

        /**
         * CSRF(Cross-Site Request Forgery) 보호 비활성화
         * 
         * CSRF란: 사용자가 의도하지 않은 요청을 서버에 보내도록 하는 공격
         * 
         * 비활성화 이유:
         * - JWT 방식은 Stateless로 동작 (세션 쿠키 미사용)
         * - CSRF는 세션 기반 인증에서만 필요
         * - JWT는 매 요청마다 Authorization 헤더로 전송되므로 CSRF 공격에 안전
         * 
         * 참고: RESTful API는 CSRF 토큰 없이 동작하는 것이 일반적
         */
        http.csrf(csrf -> csrf.disable());

        /**
         * Form Login 비활성화
         * 
         * Form Login: Spring Security가 기본 제공하는 로그인 폼 페이지
         * 
         * 비활성화 이유:
         * - 프론트엔드에서 커스텀 로그인 페이지 사용 (Login.jsx)
         * - JWT 방식은 API 기반 로그인 (폼 기반 아님)
         * - 로그인 성공 시 JWT 토큰을 반환하는 방식
         */
        http.formLogin(form -> form.disable());

        /**
         * HTTP Basic 인증 비활성화
         * 
         * HTTP Basic: HTTP 헤더에 사용자명/비밀번호를 Base64로 인코딩하여 전송
         * 형식: Authorization: Basic dXNlcjpwYXNzd29yZA==
         * 
         * 비활성화 이유:
         * - JWT 토큰 방식 사용 (Bearer 토큰)
         * - HTTP Basic은 보안성이 낮음 (매 요청마다 비밀번호 전송)
         * - JWT는 한 번만 로그인하고 토큰을 사용
         */
        http.httpBasic(basic -> basic.disable());

        /**
         * JWT 검증 필터 추가
         * 
         * 역할:
         * - 모든 HTTP 요청을 가로채서 JWT 토큰 검증
         * - UsernamePasswordAuthenticationFilter 앞에 배치
         * 
         * 실행 순서:
         * 1. 클라이언트 요청 도착
         * 2. JWTCheckFilter 실행
         *    - Authorization 헤더에서 JWT 토큰 추출
         *    - JWTUtil.validateToken()로 토큰 검증
         *    - 토큰에서 사용자 정보 추출 (userId, role)
         *    - SecurityContext에 인증 정보 저장
         * 3. UsernamePasswordAuthenticationFilter (건너뜀)
         * 4. authorizeHttpRequests 권한 체크
         * 5. 컨트롤러 메소드 실행
         * 
         * new JWTCheckFilter(jwtUtil): JWTUtil을 생성자로 주입
         */
        http.addFilterBefore(new JWTCheckFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class);

        // 권한 / 경로 접근 설정
        http.authorizeHttpRequests(auth -> auth
                // ========== 정적 리소스 ==========
                .requestMatchers("/css/**", "/js/**", "/images/**", "/font/**",
                        "/product_img/**", "/uploads/**", "/favicon.ico").permitAll()

                // ========== 공개 페이지 ==========
                .requestMatchers("/", "/index", "/login", "/signup", "/logout",
                        "/admin/login", "/admin/dashboard",
                        "/user/index", "/user/product/**", "/user/board/**").permitAll()

                // ========== 웹소켓 ==========
                .requestMatchers("/ws/**").permitAll()

                // ========== 인증 API (로그인, 회원가입, 토큰 갱신, 이메일 인증, 비밀번호 재설정) ==========
                .requestMatchers("/api/user/login", "/api/user/register",
                        "/api/user/refresh",  // ⭐ 추가: 토큰 갱신
                        "/api/user/session-info",
                        "/api/user/reset-password",  // ⭐ 비밀번호 재설정
                        "/api/email/**").permitAll()  // ⭐ 이메일 인증 API

                // ========== 소셜 로그인 ==========
                .requestMatchers("/api/auth/kakao/**").permitAll()
                .requestMatchers("/api/auth/naver/**").permitAll()  // 네이버 로그인 추가!
                .requestMatchers("/api/auth/google/**").permitAll()  // 구글 로그인 추가!

                // ========== 공개 상품 API ==========
                .requestMatchers("/api/products/**",  // 상품 목록, 상세 조회
                        "/user/product/api/**",  // 사용자용 상품 API
                        "/api/company/info").permitAll()

                // ========== 게시판 API (Q&A, 리뷰) ==========
                // 조회는 public, 작성/수정/삭제는 인증 필요
                .requestMatchers("/api/qna/**", "/api/reviews/**").permitAll()

                // ========== 공지사항 API ==========
                .requestMatchers("/api/notices/**").permitAll()

                // ========== 관리자 API 중 예외 ==========
                .requestMatchers("/api/admin/products/categories").permitAll()

                // ========== 찜하기 공개 API (찜 개수 조회) - 반드시 인증 API보다 위에 ==========
                .requestMatchers("/api/favorites/count/**").permitAll()


                // ========== 관리자 API - 모두 허용 (프론트엔드에서 별도 인증 처리) ==========
                .requestMatchers("/api/admin/**").permitAll()
                .requestMatchers("/admin/**").permitAll()

                // ========== 인증 필요 사용자 API ==========
                .requestMatchers("/api/user/**",  // 사용자 정보
                        "/api/orders/**",  // 주문
                        "/api/cart/**",  // 장바구니
                        "/api/notifications/**",  // 알림
                        "/api/favorites/**").authenticated()  // 찜하기 (count는 위에서 제외됨)

                // ========== 나머지는 인증 필요 ==========
                .anyRequest().authenticated()
        );

        return http.build();
    }

    /**
     * CORS 설정 (소셜 로그인 + JWT 대응 완성본)
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration config = new CorsConfiguration();

        // ⭐ 인증 포함 요청 허용 (JWT + 소셜 로그인 필수)
        config.setAllowCredentials(true);

        // ⭐ AllowedOriginPatterns 사용해서 CORS 안정성 확보
        config.setAllowedOriginPatterns(Arrays.asList(
                "http://localhost:3000",
                "http://127.0.0.1:3000",
                "http://localhost:8080",
                "http://127.0.0.1:8080"
        ));

        // Methods 허용
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Headers 허용
        config.setAllowedHeaders(List.of("*"));

        // ⭐ JWT 헤더 노출 - 브라우저가 Authorization 헤더 읽을 수 있게
        config.setExposedHeaders(List.of(
                "Authorization",
                "Set-Cookie"
        ));

        // Preflight 캐시 시간
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}

// 그냥 주석
