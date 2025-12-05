package com.onandhome.util;

// JWT 토큰 생성 및 검증을 위한 jjwt 라이브러리 import
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys; // 암호화 키 생성 유틸리티
import lombok.extern.slf4j.Slf4j; // 로깅을 위한 Lombok 어노테이션
import org.springframework.stereotype.Component; // Spring Bean으로 등록하기 위한 어노테이션

import java.time.ZonedDateTime; // 시간대를 포함한 날짜/시간 처리
import java.util.*;
import javax.crypto.SecretKey; // JWT 서명에 사용될 비밀키 타입

@Slf4j // 로깅 기능 활성화 (log.info(), log.error() 사용 가능)
@Component // Spring Container에 Bean으로 등록 (다른 클래스에서 @Autowired로 주입 가능)
public class JWTUtil {

    // JWT 서명에 사용되는 비밀키 (최소 256bit/32byte 이상 필요)
    // 실제 운영 환경에서는 application.properties나 환경변수로 관리 필요
    private static final String key = "1234567890123456789012345678901234567890";

    /**
     * JWT 토큰 생성 메소드
     * 호출 위치: MemberController.loginPost(), socialLogin() 등 로그인 처리 후
     * @param valueMap - 토큰 Payload에 저장할 사용자 정보 (userId, role 등)
     * @param min - Access Token 만료 시간 (분 단위, 일반적으로 60분)
     * @return JWT 토큰 문자열 (프론트엔드로 전송되어 localStorage에 저장됨)
     */
    public String generateToken(Map<String, Object> valueMap, int min) {
        SecretKey secretKey = null; // HMAC-SHA256 암호화에 사용될 비밀키 객체
        
        try {
            // String 타입의 key를 byte[]로 변환 후 SecretKey 객체 생성
            // UTF-8 인코딩으로 문자열을 바이트 배열로 변환
            secretKey = Keys.hmacShaKeyFor(key.getBytes("UTF-8"));
        } catch(Exception e) {
            // 키 생성 실패 시 RuntimeException 발생
            throw new RuntimeException(e.getMessage());
        }

        // JWT 토큰 생성 과정 (jjwt 라이브러리 사용)
        String jwtStr = Jwts.builder()
                .header().add("typ", "JWT").and() // Header: 토큰 타입을 JWT로 지정
                .claims(valueMap) // Payload: 사용자 정보(userId, role 등) 저장
                .issuedAt(Date.from(ZonedDateTime.now().toInstant())) // Payload: 토큰 발급 시간 (현재 시간)
                .expiration(Date.from(ZonedDateTime.now().plusMinutes(min).toInstant())) // Payload: 만료 시간 (현재시간 + min분)
                .signWith(secretKey) // Signature: 비밀키로 서명 (HMAC-SHA256 알고리즘 사용)
                .compact(); // 최종적으로 Header.Payload.Signature 형태의 문자열로 압축

        // 생성된 JWT 문자열 반환 -> 컨트롤러로 전달 -> HTTP 응답의 Authorization 헤더에 포함
        return jwtStr;
    }

    /**
     * JWT 토큰 검증 및 파싱 메소드
     * 호출 위치: JWTCheckFilter.doFilterInternal() - 모든 인증이 필요한 요청에서 실행
     * @param token - 클라이언트가 보낸 JWT 토큰 (HTTP 요청 헤더의 Authorization: Bearer <token>)
     * @return 토큰에 포함된 클레임(사용자 정보) Map - userId, role 등이 포함됨
     * @throws CustomJWTException - 토큰이 유효하지 않을 경우 발생
     */
    public Map<String, Object> validateToken(String token) {
        Map<String, Object> claim = null; // 토큰에서 추출한 사용자 정보를 저장할 Map
        
        try {
            // 토큰 생성 시 사용한 동일한 비밀키로 SecretKey 객체 생성
            SecretKey secretKey = Keys.hmacShaKeyFor(key.getBytes("UTF-8"));

            // JWT 파싱 및 검증 과정
            claim = Jwts.parser()
                    .verifyWith(secretKey) // 비밀키로 서명 검증 (토큰이 위조되지 않았는지 확인)
                    .build() // Parser 객체 생성
                    .parseSignedClaims(token) // 토큰 문자열을 파싱하여 Claims 객체로 변환
                    .getPayload(); // Payload 부분(사용자 정보)을 Map으로 추출
                    
        } catch(MalformedJwtException malformedJwtException) {
            // JWT 형식이 잘못된 경우 (Header.Payload.Signature 구조가 아님)
            // -> JWTCheckFilter에서 catch하여 401 에러 응답
            throw new CustomJWTException("MalFormed");
        } catch(ExpiredJwtException expiredJwtException) {
            // JWT 만료 시간이 지난 경우
            // -> 프론트엔드에서 Refresh Token으로 재발급 요청 필요
            throw new CustomJWTException("Expired");
        } catch(JwtException jwtException) {
            // 기타 JWT 관련 에러 (서명 불일치 등)
            throw new CustomJWTException("JWTError");
        } catch(Exception e) {
            // 예상치 못한 에러
            throw new CustomJWTException("Error");
        }
        // 검증 성공 시 사용자 정보 Map 반환 -> JWTCheckFilter에서 SecurityContext에 저장
        return claim;
    }
}
