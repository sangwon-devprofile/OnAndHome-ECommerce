package com.onandhome.user;

import com.onandhome.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // userId로 사용자 조회
    Optional<User> findByUserId(String userId);

    // email로 사용자 조회
    Optional<User> findByEmail(String email);

    // userId 존재 여부 확인
    boolean existsByUserId(String userId);

    // email 존재 여부 확인
    boolean existsByEmail(String email);

    // active=true 인 사용자 중 userId로 조회
    Optional<User> findByUserIdAndActiveTrue(String userId);

    // 오늘 가입한 회원 수 조회 (createdAt이 오늘 0시 이후인 사용자)
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :startOfDay")
    long countTodayNewUsers(@Param("startOfDay") LocalDateTime startOfDay);

    // 비활성(탈퇴) 회원 수 조회
    @Query("SELECT COUNT(u) FROM User u WHERE u.active = false")
    long countInactiveUsers();

    // 소셜 로그인: provider + providerId로 조회

    /**
     * 탈퇴 회원 조회 (active = false)
     */
    List<User> findByActiveFalse();

    /**
     * 소셜 로그인 제공자와 ID로 사용자 조회
     */
    Optional<User> findByProviderAndProviderId(String provider, String providerId);

    // 소셜 로그인: provider + providerId 존재 여부 확인
    boolean existsByProviderAndProviderId(String provider, String providerId);

    // 마케팅 동의 + 활성 사용자 조회
    @Query("SELECT u FROM User u WHERE u.marketingConsent = true AND u.active = true")
    List<User> findByMarketingConsentTrue();

    // 마케팅 동의 + 활성 사용자 중 특정 role 제외
    @Query("SELECT u FROM User u WHERE u.marketingConsent = true AND u.active = true AND u.role <> :excludeRole")
    List<User> findByMarketingConsentTrueAndRoleNot(@Param("excludeRole") Integer excludeRole);

    // 활성 사용자 전체 조회
    @Query("SELECT u FROM User u WHERE u.active = true")
    List<User> findByActiveTrue();

    // 활성 사용자 중 특정 role 제외
    @Query("SELECT u FROM User u WHERE u.active = true AND u.role <> :excludeRole")
    List<User> findByActiveTrueAndRoleNot(@Param("excludeRole") Integer excludeRole);

    // 역할(role) 기반 조회 (0=관리자, 1=일반 사용자)
    List<User> findByRole(Integer role);
}
