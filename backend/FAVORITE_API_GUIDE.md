# 찜하기(좋아요) 기능 API 가이드

## 📌 개요
상품에 대한 찜하기(좋아요, 하트) 기능을 제공하는 REST API입니다.

## 🗂️ 구현된 파일 목록

### Entity
- `Favorite.java` - 찜하기 엔티티 (User-Product 관계 매핑)

### Repository
- `FavoriteRepository.java` - 찜하기 데이터 접근 레이어

### Service
- `FavoriteService.java` - 찜하기 비즈니스 로직

### Controller
- `FavoriteRestController.java` - 찜하기 REST API 엔드포인트

### DTO
- `FavoriteDTO.java` - 찜하기 응답 DTO
- `FavoriteRequestDTO.java` - 찜하기 요청 DTO
- `FavoriteResponseDTO.java` - 찜하기 응답 래퍼 DTO

## 🔗 API 엔드포인트

### 1. 찜 목록 조회
```http
GET /api/favorites
Authorization: Bearer {JWT_TOKEN}
```

**응답 예시:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "userId": 1,
      "productId": 5,
      "productName": "LG 올레드 TV",
      "productCode": "PC-001",
      "price": 2500000,
      "salePrice": 2300000,
      "thumbnailImage": "/images/product1.jpg",
      "category": "TV",
      "stock": 10,
      "createdAt": "2024-11-25T10:30:00"
    }
  ],
  "count": 1
}
```

### 2. 찜하기 토글 (추가/삭제)
```http
POST /api/favorites/toggle
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json

{
  "productId": 5
}
```

**응답 예시 (찜하기 추가):**
```json
{
  "success": true,
  "message": "찜하기에 추가되었습니다.",
  "isFavorite": true,
  "data": {
    "id": 1,
    "userId": 1,
    "productId": 5,
    "productName": "LG 올레드 TV",
    "price": 2500000,
    "createdAt": "2024-11-25T10:30:00"
  }
}
```

**응답 예시 (찜하기 취소):**
```json
{
  "success": true,
  "message": "찜하기가 취소되었습니다.",
  "isFavorite": false
}
```

### 3. 찜 여부 확인
```http
GET /api/favorites/check/{productId}
Authorization: Bearer {JWT_TOKEN}
```

**응답 예시:**
```json
{
  "success": true,
  "isFavorite": true
}
```

### 4. 상품별 찜 개수 조회
```http
GET /api/favorites/count/product/{productId}
```

**응답 예시:**
```json
{
  "success": true,
  "count": 15
}
```

### 5. 사용자 찜 개수 조회
```http
GET /api/favorites/count
Authorization: Bearer {JWT_TOKEN}
```

**응답 예시:**
```json
{
  "success": true,
  "count": 5
}
```

### 6. 찜하기 삭제
```http
DELETE /api/favorites/product/{productId}
Authorization: Bearer {JWT_TOKEN}
```

**응답 예시:**
```json
{
  "success": true,
  "message": "찜하기에서 제거되었습니다."
}
```

## 🗄️ 데이터베이스 테이블

### favorite 테이블
```sql
CREATE TABLE favorite (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL,
    UNIQUE KEY unique_user_product (user_id, product_id),
    FOREIGN KEY (user_id) REFERENCES user(id),
    FOREIGN KEY (product_id) REFERENCES product(id)
);
```

## 🎯 주요 기능

### 1. 찜하기 토글
- 한 번 클릭하면 찜하기 추가
- 다시 클릭하면 찜하기 취소
- 중복 찜하기 방지 (UNIQUE 제약조건)

### 2. 찜 목록 조회
- 사용자별 찜한 상품 목록 조회
- 최신순 정렬 (createdAt DESC)
- 상품 정보 포함 (상품명, 가격, 썸네일 등)

### 3. 찜 여부 확인
- 상품 상세 페이지에서 하트 아이콘 상태 표시용
- 로그인하지 않은 경우 false 반환

### 4. 찜 개수 조회
- 상품별 찜 개수 (인기도 표시용)
- 사용자별 찜 개수 (마이페이지 표시용)

## 🔐 인증
모든 API는 JWT 토큰 기반 인증을 사용합니다.
- Header: `Authorization: Bearer {JWT_TOKEN}`
- 토큰이 없거나 유효하지 않으면 401 Unauthorized 반환

## ⚠️ 에러 처리

### 공통 에러 응답
```json
{
  "success": false,
  "message": "에러 메시지"
}
```

### 주요 에러 케이스
- **401 Unauthorized**: 로그인하지 않은 사용자
- **400 Bad Request**: 잘못된 요청 (productId 누락 등)
- **404 Not Found**: 존재하지 않는 상품 또는 사용자
- **500 Internal Server Error**: 서버 오류

## 🧪 테스트 방법

### Postman으로 테스트

1. **로그인하여 JWT 토큰 획득**
```http
POST http://localhost:8080/api/user/login
Content-Type: application/json

{
  "userId": "testuser",
  "password": "password123"
}
```

2. **찜하기 토글 테스트**
```http
POST http://localhost:8080/api/favorites/toggle
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "productId": 1
}
```

3. **찜 목록 조회 테스트**
```http
GET http://localhost:8080/api/favorites
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

## 🎨 프론트엔드 연동 예시

### JavaScript (Fetch API)
```javascript
// 찜하기 토글
async function toggleFavorite(productId) {
  const token = localStorage.getItem('token');
  
  const response = await fetch('/api/favorites/toggle', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify({ productId })
  });
  
  const result = await response.json();
  
  if (result.success) {
    // 하트 아이콘 상태 업데이트
    updateHeartIcon(result.isFavorite);
    alert(result.message);
  } else {
    alert(result.message);
  }
}

// 찜 여부 확인
async function checkFavorite(productId) {
  const token = localStorage.getItem('token');
  
  const response = await fetch(`/api/favorites/check/${productId}`, {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
  
  const result = await response.json();
  updateHeartIcon(result.isFavorite);
}

// 하트 아이콘 업데이트
function updateHeartIcon(isFavorite) {
  const heartIcon = document.getElementById('favorite-heart');
  if (isFavorite) {
    heartIcon.classList.add('active'); // 채워진 하트
  } else {
    heartIcon.classList.remove('active'); // 빈 하트
  }
}
```

### React 예시
```jsx
import { useState, useEffect } from 'react';

function FavoriteButton({ productId }) {
  const [isFavorite, setIsFavorite] = useState(false);
  
  useEffect(() => {
    checkFavorite();
  }, [productId]);
  
  const checkFavorite = async () => {
    const token = localStorage.getItem('token');
    const response = await fetch(`/api/favorites/check/${productId}`, {
      headers: { 'Authorization': `Bearer ${token}` }
    });
    const result = await response.json();
    setIsFavorite(result.isFavorite);
  };
  
  const toggleFavorite = async () => {
    const token = localStorage.getItem('token');
    const response = await fetch('/api/favorites/toggle', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify({ productId })
    });
    const result = await response.json();
    
    if (result.success) {
      setIsFavorite(result.isFavorite);
    }
  };
  
  return (
    <button onClick={toggleFavorite} className="favorite-btn">
      {isFavorite ? '❤️' : '🤍'}
    </button>
  );
}
```

## 📝 참고 사항

1. **중복 찜하기 방지**: DB 레벨에서 UNIQUE 제약조건으로 방지
2. **토글 방식**: 프론트엔드에서 하트 클릭 시 `/toggle` 엔드포인트만 호출하면 자동으로 추가/삭제 처리
3. **성능 최적화**: `FetchType.LAZY` 사용으로 N+1 문제 방지
4. **트랜잭션 관리**: `@Transactional` 어노테이션으로 데이터 일관성 보장

## 🚀 다음 단계 권장사항

프론트엔드 개발자에게 다음 작업을 요청하세요:

1. **상품 상세 페이지**
   - 하트 아이콘 추가
   - `/api/favorites/check/{productId}` 호출하여 초기 상태 설정
   - 클릭 시 `/api/favorites/toggle` 호출

2. **상품 목록 페이지**
   - 각 상품 카드에 하트 아이콘 추가
   - 상품별 찜 개수 표시

3. **마이페이지**
   - "내 찜 목록" 탭 추가
   - `/api/favorites` 호출하여 찜한 상품 목록 표시
   - 찜 취소 버튼 추가

4. **헤더/네비게이션**
   - 찜 개수 배지 표시 (`/api/favorites/count`)

## 🐛 트러블슈팅

### Q: "사용자를 찾을 수 없습니다" 에러
A: JWT 토큰이 유효한지 확인하세요. 로그인 후 받은 토큰을 사용해야 합니다.

### Q: "이미 찜한 상품입니다" 에러
A: 정상 동작입니다. `/toggle` 엔드포인트를 사용하면 이 에러가 발생하지 않습니다.

### Q: CORS 에러 발생
A: `SecurityConfig.java`에서 CORS 설정을 확인하세요.

---

**작성일**: 2024-11-25  
**작성자**: AI Assistant  
**버전**: 1.0.0

