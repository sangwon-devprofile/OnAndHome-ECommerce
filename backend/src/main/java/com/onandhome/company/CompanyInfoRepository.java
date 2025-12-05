package com.onandhome.company;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 회사 정보 Repository
 */
@Repository
public interface CompanyInfoRepository extends JpaRepository<CompanyInfo, Long> {
    
    /**
     * 첫 번째 회사 정보 조회 (일반적으로 하나만 있음)
     */
    Optional<CompanyInfo> findFirstByOrderByIdAsc();
}
