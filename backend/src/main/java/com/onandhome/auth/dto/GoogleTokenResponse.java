package com.onandhome.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 구글 OAuth 토큰 응답 DTO
 *
 * 역할: 구글 OAuth 서버로부터 받는 액세스 토큰 및 관련 정보를 담는 객체
 *
 * 데이터 흐름:
 * [구글 OAuth 서버]
 * → POST https://oauth2.googleapis.com/token (인증 코드 교환)
 * → [GoogleAuthService.getAccessToken()]
 * → RestTemplate.postForObject()로 응답 받기
 * → [이 객체에 자동 매핑]
 * → GoogleAuthService에서 accessToken 추출하여 사용자 정보 조회에 사용
 */

@Data
public class GoogleTokenResponse {
    // 액세스 토큰 - 구글 API 호출 시 인증에 사용
    // GoogleAuthService.getUserInfo() 메서드에서 사용자 정보 조회 시 필요
    @JsonProperty("access_token")
    private String accessToken;

    // 토큰 만료 시간 (초 단위) - 일반적으로 3600초 (1시간)
    @JsonProperty("expires_in")
    private Integer expiresIn;

    // 토큰 접근 권한 범위 - 예: "openid email profile"
    @JsonProperty("scope")
    private String scope;

    // 토큰 타입 - 일반적으로 "Bearer"
    // API 호출 시 "Authorization: Bearer {accessToken}" 형식으로 사용
    @JsonProperty("token_type")
    private String tokenType;

    // ID 토큰 (JWT 형식) - 사용자 인증 정보가 포함된 JSON Web Token
    @JsonProperty("id_token")
    private String idToken;

    // 리프레시 토큰 (선택적) - 액세스 토큰 갱신 시 사용
    @JsonProperty("refresh_token")
    private String refreshToken;
}
