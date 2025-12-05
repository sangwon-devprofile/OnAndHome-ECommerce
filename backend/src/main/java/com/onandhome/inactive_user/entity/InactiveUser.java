package com.onandhome.inactive_user.entity;

import com.onandhome.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * 탈퇴 회원 엔티티
 * 관리자가 회원을 삭제하면 User 테이블에서 이 테이블로 데이터가 이동됨
 */
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "inactive_user")
public class InactiveUser {

    // 기본 키 (자동 증가)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 원래 user의 로그인 아이디
    @Column(name = "user_id", nullable = false)
    private String userId;

    // 암호화된 비밀번호
    @Column(nullable = false)
    private String password;

    // 이메일
    @Column
    private String email;

    // 사용자 이름
    @Column
    private String username;

    // 전화번호
    @Column
    private String phone;

    // 성별
    @Column(length = 10)
    private String gender;

    // 생년월일
    @Column(name = "birth_date")
    private String birthDate;

    // 주소
    @Column
    private String address;

    // 원래 가입일 (User 테이블에서 복사)
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // 탈퇴일 (삭제 시점)
    @Column(name = "deleted_at", nullable = false)
    private LocalDateTime deletedAt;

    /**
     * User 엔티티로부터 InactiveUser 생성
     * 탈퇴 처리 시 사용
     */
    public static InactiveUser fromUser(User user) {
        return InactiveUser.builder()
                .userId(user.getUserId())
                .password(user.getPassword())
                .email(user.getEmail())
                .username(user.getUsername())
                .phone(user.getPhone())
                .gender(user.getGender())
                .birthDate(user.getBirthDate())
                .address(user.getAddress())
                .createdAt(user.getCreatedAt())
                .deletedAt(LocalDateTime.now())
                .build();
    }
}

