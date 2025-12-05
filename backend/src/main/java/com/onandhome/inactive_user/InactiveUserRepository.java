package com.onandhome.inactive_user;

import com.onandhome.inactive_user.entity.InactiveUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 탈퇴 회원 Repository
 */
@Repository
public interface InactiveUserRepository extends JpaRepository<InactiveUser, Long> {

    /**
     * 원래 userId로 탈퇴 회원 조회
     */
    Optional<InactiveUser> findByUserId(String userId);

    /**
     * 이메일로 탈퇴 회원 조회
     */
    Optional<InactiveUser> findByEmail(String email);

    /**
     * userId 존재 여부 확인
     */
    boolean existsByUserId(String userId);

    /**
     * 이메일 존재 여부 확인
     */
    boolean existsByEmail(String email);

    /**
     * 탈퇴일 기준 내림차순 정렬 (최근 탈퇴 순)
     */
    List<InactiveUser> findAllByOrderByDeletedAtDesc();

    /**
     * 이름으로 검색
     */
    List<InactiveUser> findByUsernameContaining(String username);

    /**
     * userId로 검색
     */
    List<InactiveUser> findByUserIdContaining(String userId);
}

