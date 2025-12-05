package com.onandhome.user.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "user")
public class User {

    // 기본 키 (자동 증가)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 로그인용 아이디 (중복 불가, 필수)
    @Column(unique = true, nullable = false)
    private String userId;

    // 암호화된 비밀번호 (필수)
    @Column(nullable = false)
    private String password;

    // 이메일 (중복 가능, 비필수)
    @Column(unique = true)
    private String email;

    // 사용자 이름
    @Column
    private String username;

    // 전화번호
    @Column
    private String phone;

    // 성별
    @Column
    private String gender;

    // 생년월일
    @Column
    private String birthDate;

    // 주소
    @Column
    private String address;

    // 소셜 로그인 제공자 (KAKAO, GOOGLE, NAVER 등)
    @Column
    private String provider;

    // 소셜 제공자의 사용자 식별 ID
    @Column
    private String providerId;

    // 사용자 역할 (0=관리자, 1=일반 사용자) 기본값 1
    @Column(nullable = false, columnDefinition = "INT DEFAULT 1")
    private Integer role;

    // 계정 활성 여부 (true=활성, false=탈퇴)
    @Column(nullable = false, columnDefinition = "TINYINT(1) DEFAULT 1")
    private Boolean active;

    // 생성 시각 (최초 저장 시 자동 설정)
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 업데이트 시각 (수정 시 자동 업데이트)
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 마케팅 정보 수신 동의 여부 (기본 false)
    @Column(name = "marketing_consent", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean marketingConsent;

    // 개인정보 이용 동의 여부 (필수, 기본 true)
    @Column(name = "privacy_consent", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 1")
    private Boolean privacyConsent;

    // 엔티티가 처음 저장될 때 실행됨: 기본값 지정 + 생성시간 설정
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        if (role == null) {
            role = 1; // 기본 사용자
        }
        if (active == null) {
            active = true; // 기본 활성 상태
        }
        if (marketingConsent == null) {
            marketingConsent = false; // 기본 비동의
        }
        if (privacyConsent == null) {
            privacyConsent = true; // 필수 동의
        }
    }

    // 엔티티가 수정될 때 실행됨: updatedAt만 갱신
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
