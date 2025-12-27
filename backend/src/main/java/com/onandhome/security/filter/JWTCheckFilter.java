package com.onandhome.security.filter;

import org.springframework.lang.NonNull;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

// Spring Security의 인증 토큰 클래스
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
// 권한 정보를 담는 클래스 (ROLE_USER, ROLE_ADMIN 등)
import org.springframework.security.core.authority.SimpleGrantedAuthority;
// 현재 인증된 사용자 정보를 저장하는 컨텍스트
import org.springframework.security.core.context.SecurityContextHolder;
// 요청당 한 번만 실행되는 필터의 기본 클래스
import org.springframework.web.filter.OncePerRequestFilter;
import com.onandhome.user.dto.UserDTO; // 사용자 정보를 담는 DTO
import com.onandhome.util.JWTUtil; // JWT 토큰 검증 유틸리티

import com.google.gson.Gson; // JSON 변환 라이브러리

import jakarta.servlet.FilterChain; // 필터 체인 (다음 필터로 요청 전달)
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest; // HTTP 요청 객체
import jakarta.servlet.http.HttpServletResponse; // HTTP 응답 객체
import lombok.extern.slf4j.Slf4j; // 로깅

/**
 * JWT 토큰 검증 필터
 * Spring Security Filter Chain에 등록되어 모든 HTTP 요청을 가로채서 JWT 검증 수행
 * 
 * 실행 순서:
 * 1. 클라이언트 요청 → 2. shouldNotFilter() 체크 → 3. doFilterInternal() 실행
 * 
 * 등록 위치: CustomSecurityConfig.filterChain() 메소드
 * 실행 시점: UsernamePasswordAuthenticationFilter 이전에 실행
 */
@Slf4j
public class JWTCheckFilter extends OncePerRequestFilter {

    // JWT 토큰 검증을 위한 유틸리티 (JWTUtil.validateToken() 호출)
    private final JWTUtil jwtUtil;

    /**
     * 생성자 - CustomSecurityConfig에서 JWTUtil을 주입받아 초기화
     * @param jwtUtil - Spring Container에서 관리하는 JWTUtil Bean
     */
    public JWTCheckFilter(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /**
     * JWT 검증을 건너뛸 경로를 판단하는 메소드
     * doFilterInternal() 실행 전에 먼저 호출됨
     * 
     * @param request - HTTP 요청 객체 (URI, Method, Header 정보 포함)
     * @return true: JWT 검증 건너뛰기 (공개 API), false: JWT 검증 수행 (인증 필요)
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {

        // 요청 URI와 HTTP 메소드 추출 (예: GET /api/products/1)
        String path = request.getRequestURI();
        String method = request.getMethod();

        log.info("=== JWT 필터 체크 ===");
        log.info("요청 경로: {} {}", method, path);

        // ✅ OPTIONS 요청(Preflight) 제외
        // CORS 정책 때문에 브라우저가 실제 요청 전에 보내는 사전 요청
        // OPTIONS 요청에는 토큰이 없으므로 검증 제외
        if(request.getMethod().equals("OPTIONS")) {
            log.info("OPTIONS 요청 JWT 체크 제외");
            return true; // JWT 검증 건너뛰기
        }

        // ✅ 정적 리소스는 JWT 체크 완전 제외
        // CSS, JS, 이미지 파일 등은 인증 불필요
        if(path.startsWith("/css/") ||
                path.startsWith("/js/") ||
                path.startsWith("/images/") ||
                path.startsWith("/font/") ||
                path.startsWith("/product_img/") ||
                path.startsWith("/uploads/") ||
                path.equals("/favicon.ico")) {
            log.info("정적 리소스 JWT 체크 제외: {}", path);
            return true; // JWT 검증 건너뛰기
        }

        // ✅ 인증 관련 API는 JWT 검증 제외
        // 로그인, 회원가입, 토큰 갱신 등은 토큰 없이 접근 가능해야 함
        if(path.startsWith("/api/user/login") ||      // 일반 로그인
                path.startsWith("/api/user/register") ||   // 회원가입
                path.startsWith("/api/user/refresh") ||    // Access Token 갱신 (Refresh Token 사용)
                path.startsWith("/api/user/session-info") || // 세션 정보 확인
                path.startsWith("/api/user/reset-password") || // 비밀번호 재설정
                path.startsWith("/api/auth/kakao/") ||     // 카카오 소셜 로그인
                path.startsWith("/api/auth/naver/") ||     // 네이버 소셜 로그인
                path.startsWith("/api/auth/google/") ||    // 구글 소셜 로그인
                path.startsWith("/api/email/")) {          // 이메일 인증 API
            log.info("인증 API JWT 체크 제외: {}", path);
            return true; // JWT 검증 건너뛰기
        }

        // ✅ 공개 페이지는 JWT 검증 제외
        // 메인 페이지, 로그인 페이지 등 누구나 접근 가능한 페이지
        if(path.equals("/") ||
                path.equals("/index") ||
                path.startsWith("/login") ||
                path.startsWith("/signup") ||
                path.startsWith("/logout") ||
                path.startsWith("/admin/login") ||
                path.startsWith("/user/index") ||
                path.startsWith("/user/product/") ||
                path.startsWith("/user/board/") ||
                path.startsWith("/admin/dashboard")) {
            log.info("공개 페이지 JWT 체크 제외: {}", path);
            return true; // JWT 검증 건너뛰기
        }

        // ✅ 공개 API (상품, 회사 정보 조회)
        // 비로그인 사용자도 상품 목록과 상세 정보를 볼 수 있어야 함
        if(path.startsWith("/api/products/") ||
                path.startsWith("/user/product/api/") ||
                path.startsWith("/api/company/info")) {
            log.info("공개 상품 API JWT 체크 제외: {}", path);
            return true; // JWT 검증 건너뛰기
        }

        // ✅ Q&A, 리뷰, 공지사항 읽기 API
        // GET 요청(조회)만 공개, POST/PUT/DELETE(작성/수정/삭제)는 JWT 검증 필요
        if(path.startsWith("/api/qna/") || 
           path.startsWith("/api/reviews/") || 
           path.startsWith("/api/notices/")) {
            // GET 요청만 JWT 검증 제외 (목록, 상세 조회)
            if(method.equals("GET")) {
                log.info("게시판 조회 API JWT 체크 제외: {}", path);
                return true; // JWT 검증 건너뛰기
            }
            // POST/PUT/DELETE는 JWT 검증 수행 (아래로 진행)
        }

        // ✅ 찜 개수 조회 API는 공개
        // 비로그인 사용자도 상품의 찜 개수를 볼 수 있어야 함 (인기 상품 정렬용)
        if (path.startsWith("/api/favorites/count")) {
            log.info("찜 개수 조회 API JWT 체크 제외: {}", path);
            return true; // JWT 검증 건너뛰기
        }

        // ✅ 관리자 카테고리 조회 API는 JWT 체크 제외
        // 상품 등록 시 카테고리 목록을 가져오기 위한 API
        if (path.equals("/api/admin/products/categories")) {
            log.info("관리자 카테고리 API JWT 체크 제외: {}", path);
            return true; // JWT 검증 건너뛰기
        }

        // ✅ 웹소켓 관련 모든 요청은 JWT 체크에서 제외 (노이즈 및 401 방지)
        if (path.startsWith("/ws/")) {
            log.info("웹소켓 관련 요청 JWT 체크 제외: {}", path);
            return true;
        }

        // ⭐ 나머지는 모두 JWT 체크 필수!
        // 인증이 필요한 API들:
        // - /api/user/** (사용자 정보 조회/수정)
        // - /api/orders/** (주문 생성/조회)
        // - /api/cart/** (장바구니 추가/삭제)
        // - /api/notifications/** (알림 조회)
        // - /api/favorites/** (찜하기 추가/삭제)
        // - /api/admin/** (관리자 기능)
        log.info("JWT 체크 대상 경로: {}", path);
        return false; // JWT 검증 수행 (doFilterInternal 실행)
    }

    /**
     * JWT 토큰 검증 및 인증 처리 메소드
     * shouldNotFilter()에서 false를 반환한 경우에만 실행됨
     * 
     * 처리 과정:
     * 1. Authorization 헤더에서 JWT 토큰 추출
     * 2. JWTUtil.validateToken()으로 토큰 검증 및 파싱
     * 3. 토큰에서 사용자 정보 추출 (id, userId, role 등)
     * 4. Spring Security Context에 인증 정보 저장
     * 5. 다음 필터로 요청 전달
     * 
     * @param request - HTTP 요청 객체 (Authorization 헤더 포함)
     * @param response - HTTP 응답 객체 (에러 발생 시 401 응답 작성)
     * @param filterChain - 다음 필터로 요청을 전달하기 위한 체인
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain) throws ServletException, IOException {

        // 요청 경로 추출
        String path = request.getRequestURI();

        // HTTP 요청 헤더에서 Authorization 값 가져오기
        // 형식: "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
        String authHeaderStr = request.getHeader("Authorization");
        String accessToken = null;

        try {
            // 1. Authorization 헤더 확인
            if (authHeaderStr != null && authHeaderStr.startsWith("Bearer ")) {
                accessToken = authHeaderStr.substring(7);
                log.info("헤더에서 토큰 추출 성공");
            } 
            
            // 2. 헤더에 없으면 쿼리 파라미터 확인 (웹소켓 등)
            if (accessToken == null || accessToken.isEmpty()) {
                accessToken = request.getParameter("token");
                if (accessToken != null) {
                    log.info("파라미터에서 토큰 추출 성공");
                }
            }

            // 토큰이 없으면 예외 발생
            if (accessToken == null || accessToken.isEmpty()) {
                log.warn("인증 토큰이 누락되었습니다: {}", path);
                throw new Exception("Authorization header or token parameter is missing");
            }

            // JWTUtil.validateToken()으로 토큰 검증 및 Payload(사용자 정보) 추출
            Map<String, Object> claims = jwtUtil.validateToken(accessToken);

            // 클레임에서 사용자 정보 추출
            Long id = Long.valueOf(claims.get("id").toString()); // 사용자 고유 ID (DB Primary Key)
            String userId = (String) claims.get("userId"); // 로그인 ID (이메일 등)
            Integer role = (Integer) claims.get("role"); // 권한 (0: 관리자, 1: 일반 사용자)
            
            // 마케팅 동의 여부 (없으면 기본값 false)
            Boolean marketingConsent = claims.get("marketingConsent") != null 
                    ? (Boolean) claims.get("marketingConsent") 
                    : false;

            // UserDTO 객체 생성 및 사용자 정보 설정
            // 이 객체는 Spring Security Context에 저장되어 컨트롤러에서 사용됨
            UserDTO userDTO = new UserDTO();
            userDTO.setId(id);
            userDTO.setUserId(userId);
            userDTO.setRole(role);

            // 인증 성공 로그 출력
            log.info("JWT 인증 성공 - userId: {}, role: {}, marketingConsent: {}", userId, role, marketingConsent);

            // Request 객체에 사용자 정보 저장 (컨트롤러에서 getAttribute()로 꺼내 사용)
            // 예: Long userId = (Long) request.getAttribute("userId");
            request.setAttribute("userId", id);
            request.setAttribute("marketingConsent", marketingConsent);

            // Spring Security 인증 토큰 생성
            // UsernamePasswordAuthenticationToken: 사용자 인증 정보를 담는 객체
            // - principal: 인증된 사용자 정보 (UserDTO)
            // - credentials: 비밀번호 (JWT 방식에서는 null)
            // - authorities: 권한 목록 (ROLE_ADMIN 또는 ROLE_USER)
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(
                            userDTO, // 인증된 사용자 정보
                            null,    // 비밀번호는 JWT 검증으로 대체되므로 null
                            // role이 0이면 ROLE_ADMIN, 1이면 ROLE_USER 권한 부여
                            java.util.List.of(new SimpleGrantedAuthority(role == 0 ? "ROLE_ADMIN" : "ROLE_USER"))
                    );

            // SecurityContextHolder에 인증 정보 저장
            // 이후 모든 컨트롤러에서 SecurityContextHolder.getContext().getAuthentication()으로 접근 가능
            // 또는 @AuthenticationPrincipal 어노테이션으로 UserDTO 주입 가능
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);

            // 다음 필터로 요청 전달 (필터 체인 계속 진행)
            // 인증 완료 후 실제 컨트롤러 메소드 실행
            filterChain.doFilter(request, response);

        } catch(Exception e) {
            // JWT 검증 실패 시 처리 (토큰 없음, 만료, 위조 등)
            log.warn("JWT 인증 실패 - {}: {}", request.getRequestURI(), e.getMessage());

            // JSON 형태의 에러 응답 생성
            Gson gson = new Gson();
            // {"error": "ERROR_ACCESS_TOKEN", "message": "토큰이 만료되었습니다"}
            String msg = gson.toJson(Map.of("error", "ERROR_ACCESS_TOKEN", "message", e.getMessage()));

            // HTTP 응답 설정
            response.setContentType("application/json; charset=UTF-8"); // JSON 응답 타입
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 Unauthorized 상태 코드
            
            // 응답 본문에 에러 메시지 작성
            PrintWriter printWriter = response.getWriter();
            printWriter.println(msg); // 에러 JSON 출력
            printWriter.close(); // 출력 스트림 닫기
            
            // 여기서 필터 체인 중단 (filterChain.doFilter() 호출하지 않음)
            // 컨트롤러로 요청이 전달되지 않고 401 에러 응답으로 종료
            
            // 프론트엔드에서 이 응답을 받으면:
            // - authSlice.js의 API 인터셉터에서 401 에러 감지
            // - Refresh Token으로 새 Access Token 발급 시도
            // - 실패 시 로그인 페이지로 리다이렉트
        }
    }
}

// 그냥 주석
