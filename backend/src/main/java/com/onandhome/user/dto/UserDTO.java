package com.onandhome.user.dto;

import com.onandhome.user.entity.User;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class UserDTO {

    // 사용자 고유 ID (PK)
    private Long id;

    // 로그인용 아이디
    private String userId;

    // 암호화된 비밀번호
    private String password;

    // 이메일
    private String email;

    // 이름
    private String username;

    // 전화번호
    private String phone;

    // 성별
    private String gender;

    // 생년월일
    private String birthDate;

    // 주소
    private String address;

    // 사용자 권한 (0=관리자, 1=회원)
    private Integer role;

    // 계정 활성 여부 (true=활성, false=탈퇴)
    private Boolean active;

    // 가입일
    private LocalDateTime createdAt;

    // 정보 수정일
    private LocalDateTime updatedAt;

    // 마케팅 수신 동의 여부
    private Boolean marketingConsent;

    // 개인정보 이용 동의 여부
    private Boolean privacyConsent;

    // Entity → DTO 변환 메서드
    // DB에서 가져온 User 엔티티를 화면에서 사용할 수 있는 UserDTO로 변환
    public static UserDTO fromEntity(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .userId(user.getUserId())
                .password(user.getPassword())
                .email(user.getEmail())
                .username(user.getUsername())
                .phone(user.getPhone())
                .gender(user.getGender())
                .birthDate(user.getBirthDate())
                .address(user.getAddress())
                .role(user.getRole())
                .active(user.getActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .marketingConsent(user.getMarketingConsent())
                .privacyConsent(user.getPrivacyConsent())
                .build();
    }

    // DTO → Entity 변환 메서드
    // 입력 폼에서 받은 데이터를 실제 DB 저장용 User 엔티티로 변환
    public User toEntity() {
        return User.builder()
                .id(this.id)
                .userId(this.userId)
                .password(this.password)
                .email(this.email)
                .username(this.username)
                .phone(this.phone)
                .gender(this.gender)
                .birthDate(this.birthDate)
                .address(this.address)
                .role(this.role)
                .active(this.active)
                .marketingConsent(this.marketingConsent)
                .privacyConsent(this.privacyConsent)
                .build();
    }
}
