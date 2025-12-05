package com.onandhome.auth.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties; // JSON에서 선언되지 않은 필드는 무시하도록 설정
import com.fasterxml.jackson.annotation.JsonProperty; // JSON 키와 자바 필드를 연결할 때 사용
import lombok.Data; // Lombok: getter/setter/toString 등을 자동으로 생성해 줌

@Data // getter/setter, toString, equals 등을 자동 생성
@JsonIgnoreProperties(ignoreUnknown = true) // JSON에 내가 선언하지 않은 필드가 있어도 파싱 에러 없이 무시함
public class KakaoTokenResponse {

    @JsonProperty("access_token") // JSON의 "access_token" 값을 매핑
    private String accessToken; // 카카오 인증 후 발급되는 액세스 토큰 (API 요청에 사용)

    @JsonProperty("token_type") // JSON의 "token_type" 매핑
    private String tokenType; // 일반적으로 "bearer" 값이 들어옴

    @JsonProperty("refresh_token") // JSON의 "refresh_token" 매핑
    private String refreshToken; // 액세스 토큰 만료 시 재발급받는 용도의 토큰

    @JsonProperty("expires_in") // JSON의 "expires_in" 매핑
    private Integer expiresIn; // 액세스 토큰 만료 시간 (초 단위)

    @JsonProperty("scope") // JSON의 "scope" 매핑
    private String scope; // 유효한 권한 범위(예: profile, account_email 등)

    @JsonProperty("refresh_token_expires_in") // JSON의 "refresh_token_expires_in" 매핑
    private Integer refreshTokenExpiresIn; // 리프레시 토큰 만료 시간 (초 단위)
}

/*
요약
1. 이 DTO는 카카오 OAuth 서버가 보내주는 토큰 응답(JSON)을 그대로 담는 역할
2. 주요 필드는 access_token, refresh_token, 만료 시간(expires_in) 등이 있음
3. @JsonProperty로 JSON 키를 자바 필드와 정확하게 매핑하고 있음
4. @JsonIgnoreProperties 덕분에 JSON에 새로운 필드가 생겨도 파싱이 깨지지 않음
5. Lombok의 @Data로 getter/setter가 자동 생성돼서 코드가 깔끔해짐
 */