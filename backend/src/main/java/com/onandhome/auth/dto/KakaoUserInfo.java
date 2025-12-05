package com.onandhome.auth.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties; // JSON에서 맵핑되지 않은 필드는 무시하도록 하는 애너테이션 임포트
import com.fasterxml.jackson.annotation.JsonProperty; // JSON 키와 자바 필드를 매핑할 때 사용
import lombok.Data; // Lombok: getter/setter/toString 등 자동 생성

@Data // Lombok이 getter/setter, toString, equals 등을 자동 생성해 줌
@JsonIgnoreProperties(ignoreUnknown = true) // JSON에 내가 선언하지 않은 필드가 있어도 무시함 (파싱 실패 방지)
public class KakaoUserInfo {

    @JsonProperty("id") // 카카오가 내려주는 JSON의 "id" 값을 이 필드에 매핑
    private Long id; // 카카오 유저의 고유 ID (숫자 형태)

    @JsonProperty("connected_at") // 카카오가 제공하는 "connected_at" 필드 매핑
    private String connectedAt; // 카카오 계정이 연결된 시간(ISO 날짜 문자열)

    @JsonProperty("kakao_account") // JSON 안의 "kakao_account" 객체 전체를 KakaoAccount 클래스로 매핑
    private KakaoAccount kakaoAccount; // 이메일, 닉네임 등 상세 정보가 들어 있음

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true) // 선언되지 않은 JSON 필드는 무시 (카카오가 종종 필드 추가하기 때문)
    public static class KakaoAccount {

        @JsonProperty("profile_nickname_needs_agreement") // 닉네임 제공에 동의 필요 여부
        private Boolean profileNicknameNeedsAgreement; // true면 닉네임 제공하려면 동의 받아야 함

        @JsonProperty("profile") // "profile" JSON 객체 전체를 Profile 클래스로 매핑
        private Profile profile; // 닉네임, 프로필 이미지 등

        @JsonProperty("has_email") // 이메일 필드가 존재하는지 여부 (카카오 계정 설정에 따라 다름)
        private Boolean hasEmail;

        @JsonProperty("email_needs_agreement") // 이메일 제공 동의 필요 여부
        private Boolean emailNeedsAgreement;

        @JsonProperty("is_email_valid") // 카카오에서 이메일이 유효한지 확인한 값
        private Boolean isEmailValid;

        @JsonProperty("is_email_verified") // 이메일이 실제로 인증된 계정인지 여부
        private Boolean isEmailVerified;

        @JsonProperty("email") // 이메일 문자열 매핑
        private String email; // 유저 이메일 값(없을 수도 있음)

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true) // 혹시 profile 내에 다른 필드가 있어도 무시
        public static class Profile {

            @JsonProperty("nickname") // "nickname" 필드를 매핑
            private String nickname; // 카카오 프로필 닉네임

            @JsonProperty("thumbnail_image_url") // 썸네일 이미지 주소 매핑
            private String thumbnailImageUrl; // 작은 크기의 프로필 이미지 URL

            @JsonProperty("profile_image_url") // 일반 프로필 이미지 주소 매핑
            private String profileImageUrl; // 큰 프로필 이미지 URL
        }
    }
}
/*
요약
1. KakaoUserInfo는 카카오에서 보내주는 JSON을 자바 DTO로 그대로 담기 위한 클래스
2. @JsonProperty는 JSON의 키 이름을 자바 필드에 연결하는 역할
3. @JsonIgnoreProperties(ignoreUnknown = true)는 JSON에 내가 안 만든 필드가 있어도 무시하고 파싱 실패를 막아줌
4. 내부 클래스로 KakaoAccount, Profile을 둬서 JSON 구조를 똑같이 표현한 구조
5. Lombok의 @Data로 getter/setter를 자동 생성해서 코드가 간결하게 유지됨
 */