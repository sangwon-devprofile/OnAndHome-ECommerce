package com.onandhome.company;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 회사 정보 Service
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CompanyInfoService {
    
    private final CompanyInfoRepository companyInfoRepository;
    
    /**
     * 회사 정보 조회 (첫 번째 레코드)
     */
    @Transactional(readOnly = true)
    public Optional<CompanyInfo> getCompanyInfo() {
        try {
            return companyInfoRepository.findFirstByOrderByIdAsc();
        } catch (Exception e) {
            log.error("회사 정보 조회 오류", e);
            return Optional.empty();
        }
    }
    
    /**
     * 회사 정보 저장/수정
     */
    @Transactional
    public CompanyInfo save(CompanyInfo companyInfo) {
        return companyInfoRepository.save(companyInfo);
    }
}
