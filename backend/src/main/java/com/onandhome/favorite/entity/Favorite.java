package com.onandhome.favorite.entity; //경로

//역할 : 사용자가 찜을 했을시 저장되는 Database Entity
//주요 필드 구성
//id (Long type) : 찜 고유ID 자동생성
//user (User Entity) : 찜한 사용자 (다대일관계)
//product (Product Entity) : 찜한 상품 (다대일관계)
//createdAt (LocalDateTime) : 찜한 일시
//
//특징
//@Table의 uniqueConstraints: 동일 사용자가 같은 상품을 중복 찜하는 것을 방지
//FetchType.LAZY: 성능 최적화를 위해 지연 로딩 적용
//@PrePersist: 엔티티 저장 전 자동으로 createdAt 설정

import com.onandhome.admin.adminProduct.entity.Product;
import com.onandhome.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter //각 필드값 가져오기
@Setter //각 필드값 설정or변경
@NoArgsConstructor //파라미터 없는 빈 생성자(새로운 행 틀을 만들어 DB값 입력)
@AllArgsConstructor // 모든 파라미터 받는 생성자 (Builder쓰기위해 필요)
@Builder            // 객체를 만들 때 가독성 높게 필드 값을 설정할 수 있는 빌더 패턴 코드를 자동생성
@Table(name = "favorite", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "product_id"})
})
public class Favorite {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

