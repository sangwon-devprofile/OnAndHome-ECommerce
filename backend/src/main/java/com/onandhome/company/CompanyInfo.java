package com.onandhome.company;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 회사 정보 엔티티
 */
@Entity
@Table(name = "company_info")
@Getter
@Setter
public class CompanyInfo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String companyName;  // 회사명
    
    @Column(nullable = false)
    private String ceo;  // 대표
    
    @Column(nullable = false)
    private String fax;  // 팩스
    
    @Column(nullable = false)
    private String email;  // 이메일
    
    @Column(nullable = false)
    private String address;  // 주소
    
    @Column(nullable = false)
    private String businessNumber;  // 사업자등록번호
    
    @Column(nullable = false)
    private String mailOrderNumber;  // 통신판매업신고
    
    @Column(nullable = false)
    private String privacyOfficer;  // 개인정보 책임자
    
    @Column(nullable = false)
    private String phone;  // 고객센터 전화번호
    
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
