package com.onandhome.email.repository;

import com.onandhome.email.entity.AccountDeletion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface AccountDeletionRepository extends JpaRepository<AccountDeletion, Long> {
    
    Optional<AccountDeletion> findByEmailAndCode(String email, String code);
    
    Optional<AccountDeletion> findTopByEmailOrderByCreatedAtDesc(String email);
    
    void deleteByExpiresAtBefore(LocalDateTime dateTime);
}
