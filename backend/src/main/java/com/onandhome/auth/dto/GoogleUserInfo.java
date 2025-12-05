package com.onandhome.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 구글 사용자 정보 DTO
 *
 * 역할: 구글 API로부터 받은 사용자 정보를 담는 객체
 *
 * 데이터 흐름:
 * [GoogleAuthService.getAccessToken()]
 * → accessToken 획득
 * → [GoogleAuthService.getUserInfo(accessToken)]
 * → GET https://www.googleapis.com/oauth2/v2/userinfo
 * → Authorization: Bearer {accessToken} 헤더 포함
 * → [이 객체에 자동 매핑]
 * → GoogleAuthService.processGoogleLogin()에서 정보 추출
 * → 이메일로 기존 회원 조회 또는 신규 회원 생성
 */

@Data
public class GoogleUserInfo {
    // 구글 고유 ID - 구글 계정의 고유 식별자 (변경되지 않는 값)
    private String id;
    
    // 이메일 주소 - 회원가입/로그인 시 주요 식별자로 사용
    // User 테이블의 email 필드와 매칭
    private String email;

    // 이메일 인증 여부 - true: 구글에서 이메일 인증 완료
    @JsonProperty("verified_email")
    private Boolean verifiedEmail;

    // 사용자 이름 (전체) - 성 + 이름 조합, 예: "홍길동"
    // User 테이블의 name 필드에 저장
    private String name;

    // 이름 (Given Name) - 영문 이름의 이름 부분, 예: "Gildong"
    @JsonProperty("given_name")
    private String givenName;

    // 성 (Family Name) - 영문 이름의 성 부분, 예: "Hong"
    @JsonProperty("family_name")
    private String familyName;

    // 프로필 사진 URL - 구글 계정의 프로필 이미지 주소
    private String picture;
    
    // 로케일 (언어/지역 설정) - 예: "ko" (한국어), "en" (영어)
    private String locale;
}
