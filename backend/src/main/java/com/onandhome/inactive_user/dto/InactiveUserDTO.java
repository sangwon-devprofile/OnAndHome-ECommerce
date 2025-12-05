package com.onandhome.inactive_user.dto;

import com.onandhome.inactive_user.entity.InactiveUser;
import lombok.*;
import java.time.LocalDateTime;

/**
 * 탈퇴 회원 DTO
 * 프론트엔드와 데이터 전송용
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class InactiveUserDTO {

    // 탈퇴 회원 고유 ID (PK)
    private Long id;

    // 원래 로그인 아이디
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

    // 원래 가입일
    private LocalDateTime createdAt;

    // 탈퇴일
    private LocalDateTime deletedAt;

    /**
     * Entity → DTO 변환 메서드
     */
    public static InactiveUserDTO fromEntity(InactiveUser inactiveUser) {
        return InactiveUserDTO.builder()
                .id(inactiveUser.getId())
                .userId(inactiveUser.getUserId())
                .password(inactiveUser.getPassword())
                .email(inactiveUser.getEmail())
                .username(inactiveUser.getUsername())
                .phone(inactiveUser.getPhone())
                .gender(inactiveUser.getGender())
                .birthDate(inactiveUser.getBirthDate())
                .address(inactiveUser.getAddress())
                .createdAt(inactiveUser.getCreatedAt())
                .deletedAt(inactiveUser.getDeletedAt())
                .build();
    }

    /**
     * DTO → Entity 변환 메서드
     */
    public InactiveUser toEntity() {
        return InactiveUser.builder()
                .id(this.id)
                .userId(this.userId)
                .password(this.password)
                .email(this.email)
                .username(this.username)
                .phone(this.phone)
                .gender(this.gender)
                .birthDate(this.birthDate)
                .address(this.address)
                .createdAt(this.createdAt)
                .deletedAt(this.deletedAt)
                .build();
    }
}

