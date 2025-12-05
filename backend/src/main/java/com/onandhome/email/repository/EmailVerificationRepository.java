package com.onandhome.email.repository;

import com.onandhome.email.entity.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {
    
    /**
     * 이메일로 가장 최근 인증 코드 조회
     */
    Optional<EmailVerification> findTopByEmailOrderByCreatedAtDesc(String email);
    
    /**
     * 이메일과 코드로 인증 정보 조회
     */
    Optional<EmailVerification> findByEmailAndCode(String email, String code);
    
    /**
     * 만료된 인증 코드 삭제
     */
    void deleteByExpiresAtBefore(LocalDateTime dateTime);
}
