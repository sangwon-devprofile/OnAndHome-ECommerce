# OnAndHome 프로젝트 - Spring Boot & React 마이그레이션 작업 완료

## 작업 개요
Spring Boot + Thymeleaf로 구성된 기존 프로젝트를 Spring Boot (백엔드) + React (프론트엔드)로 분리하는 작업을 진행했습니다.

## 작업 완료 항목

### 1. API 엔드포인트 수정
기존 API 엔드포인트를 Spring Boot 백엔드의 실제 구현에 맞게 수정했습니다.

#### 1.1 인증 API (authApi.js)
- **로그인**: `/api/user/login` (기존 `/api/auth/login`에서 변경)
- **회원가입**: `/api/user/register` (기존 `/api/auth/signup`에서 변경)
- **토큰 갱신**: `/api/user/refresh` (기존 `/api/auth/refresh`에서 변경)
- **세션 정보**: `/api/user/session-info`
- **사용자 조회**: `/api/user/username/{userId}`, `/api/user/{id}`

#### 1.2 상품 API (productApi.js)
- **전체 상품 조회**: `/api/products/list`
- **상품 상세**: `/api/products/{id}`
- **상품 검색**: `/api/products/search?keyword=검색어`
- **상품 생성/수정/삭제**: 관리자 전용 엔드포인트

#### 1.3 장바구니 API (cartApi.js)
- **상품 추가**: `POST /api/cart/add`
- **목록 조회**: `GET /api/cart`
- **개수 조회**: `GET /api/cart/count`
- **수량 수정**: `PUT /api/cart/{cartItemId}`
- **아이템 삭제**: `DELETE /api/cart/{cartItemId}`
- **전체 비우기**: `DELETE /api/cart/clear/all`

#### 1.4 주문 API (orderApi.js)
- **주문 생성**: `POST /api/orders/create`
- **사용자 주문 목록**: `GET /api/orders/user/{userId}`
- **주문 상세**: `GET /api/orders/{orderId}`
- **결제 처리**: `POST /api/orders/{orderId}/pay`
- **주문 취소**: `POST /api/orders/{orderId}/cancel`

### 2. Axios 설정 수정 (axiosConfig.js)
- JWT 토큰 자동 헤더 추가 기능 유지
- 토큰 갱신 로직 Spring Boot API에 맞게 수정
  - Authorization 헤더에 refresh token 전달 방식으로 변경
- 401 에러 시 자동 토큰 갱신 및 재시도 로직 구현
- 토큰 만료 시 로그인 페이지로 리다이렉트

### 3. React 컴포넌트 작성

#### 3.1 Login.js
- Spring Boot API 엔드포인트에 맞게 수정
- JWT 토큰 저장 로직 구현
- 에러 핸들링 강화
- 로그인 성공 시 역할(role)에 따른 리다이렉트
  - role === 0: 관리자 → `/admin/dashboard`
  - role === 1: 일반 사용자 → `/`
- 로딩 상태 표시
- CSS 스타일링 (Login.css)

#### 3.2 Signup.js
- Spring Boot API 엔드포인트에 맞게 수정
- 클라이언트 측 입력 검증
  - 아이디: 4-20자
  - 비밀번호: 8자 이상
  - 비밀번호 확인: 일치 여부
  - 이메일: 형식 검증
  - 휴대폰: 형식 검증 (선택)
- 실시간 에러 메시지 표시
- 회원가입 성공 시 로그인 페이지로 자동 이동 (2초 후)
- CSS 스타일링 (Signup.css)

### 4. JWT 인증 흐름

```
1. 로그인 시
   - 사용자가 userId/password 입력
   - POST /api/user/login 호출
   - accessToken (10분), refreshToken (1일) 수신
   - localStorage에 토큰 저장
   - 역할에 따라 페이지 리다이렉트

2. API 요청 시
   - axios interceptor가 자동으로 Authorization 헤더 추가
   - Bearer {accessToken} 형식으로 전송

3. 토큰 만료 시
   - 401 에러 발생
   - axios interceptor가 자동으로 refresh 요청
   - POST /api/user/refresh (Authorization: Bearer {refreshToken})
   - 새로운 accessToken 수신 및 저장
   - 원래 요청 재시도

4. refresh token 만료 시
   - 로그인 페이지로 리다이렉트
   - localStorage 토큰 제거
```

### 5. 보안 설정 (Spring Boot)
- CORS 설정: 모든 오리진 허용 (개발 환경)
- JWT 기반 인증
- 공개 API 엔드포인트:
  - `/api/user/login`
  - `/api/user/register`
  - `/api/user/session-info`
  - `/api/products/**`
- 인증 필요 API:
  - `/api/cart/**`
  - `/api/orders/**`
- 관리자 전용:
  - `/admin/**`

## 프로젝트 구조

```
C:\onandhomefront\
├── src\
│   ├── api\
│   │   ├── authApi.js          ✅ 수정 완료
│   │   ├── axiosConfig.js      ✅ 수정 완료
│   │   ├── cartApi.js          ✅ 수정 완료
│   │   ├── orderApi.js         ✅ 수정 완료
│   │   ├── productApi.js       ✅ 수정 완료
│   │   ├── qnaApi.js
│   │   ├── reviewApi.js
│   │   ├── noticeApi.js
│   │   └── userApi.js
│   ├── pages\
│   │   └── user\
│   │       ├── Login.js        ✅ 작성 완료
│   │       ├── Login.css       ✅ 작성 완료
│   │       ├── Signup.js       ✅ 작성 완료
│   │       ├── Signup.css      ✅ 작성 완료
│   │       ├── Cart.js
│   │       ├── Home.js
│   │       ├── MyInfo.js
│   │       ├── MyOrders.js
│   │       ├── MyPage.js
│   │       ├── Order.js
│   │       ├── ProductDetail.js
│   │       └── ProductList.js
│   ├── components\
│   └── store\
└── package.json
```

## 다음 단계 작업

### 우선순위 높음
1. **Header/Footer 컴포넌트 작성**
   - 기존 Thymeleaf fragments 참고
   - 로그인 상태에 따른 네비게이션 변경
   - 장바구니 아이콘 (개수 표시)

2. **Home 페이지**
   - `/user/index.html` 참고
   - 상품 목록 표시
   - 슬라이더/배너

3. **ProductList & ProductDetail 페이지**
   - `/user/product/category.html`
   - `/user/product/detail.html` 참고

4. **Cart 페이지**
   - `/user/cart_new.html` 참고
   - 장바구니 목록 표시
   - 수량 변경/삭제 기능

5. **MyPage 페이지**
   - `/user/my_page.html` 참고
   - 사용자 정보 표시
   - 주문 내역 링크

6. **Order & MyOrders 페이지**
   - `/user/order.html`
   - `/user/my_order.html` 참고

### 우선순위 중간
7. **QnA API 업데이트**
   - qnaApi.js 수정
   - Spring Boot QnaRestController 확인 후 수정

8. **Review API 업데이트**
   - reviewApi.js 수정
   - Spring Boot ReviewRestController 확인 후 수정

9. **Notice API 업데이트**
   - noticeApi.js 수정
   - Spring Boot NoticeService 확인 후 수정

### 우선순위 낮음
10. **Admin 페이지들**
    - 관리자 대시보드
    - 상품 관리
    - 주문 관리
    - 게시판 관리 (공지사항, QnA, 리뷰)

11. **라우팅 설정** (App.js)
    - React Router 설정
    - 인증 라우트 가드 구현

12. **Redux 상태 관리** (선택사항)
    - 사용자 정보
    - 장바구니 상태

## 테스트 가이드

### 로그인 테스트
1. Spring Boot 서버 실행 (8080 포트)
2. React 개발 서버 실행 (3000 포트)
3. http://localhost:3000/login 접속
4. 테스트 계정으로 로그인 시도
5. JWT 토큰이 localStorage에 저장되는지 확인
6. 역할에 따라 올바른 페이지로 리다이렉트되는지 확인

### 회원가입 테스트
1. http://localhost:3000/signup 접속
2. 폼 입력 및 검증 확인
3. 회원가입 완료 후 로그인 페이지로 이동 확인
4. 생성된 계정으로 로그인 가능한지 확인

### API 테스트
1. 개발자 도구 > Network 탭에서 API 요청 확인
2. Authorization 헤더가 자동으로 추가되는지 확인
3. 401 에러 시 자동 토큰 갱신이 동작하는지 확인

## 주의사항

1. **CORS 설정**
   - 개발 환경에서는 모든 오리진 허용
   - 프로덕션 환경에서는 특정 도메인만 허용하도록 수정 필요

2. **토큰 저장**
   - 현재 localStorage 사용
   - 더 높은 보안이 필요한 경우 HttpOnly 쿠키 사용 고려

3. **에러 핸들링**
   - 네트워크 에러, 서버 에러 등 다양한 상황 대응
   - 사용자에게 친화적인 에러 메시지 표시

4. **환경 변수**
   - `.env` 파일에 API URL 설정
   - 개발/프로덕션 환경에 따라 다른 URL 사용

## 환경 설정

### React 환경 변수 (.env)
```
REACT_APP_API_URL=http://localhost:8080
```

### Spring Boot 환경 설정 (application.properties)
```
server.port=8080
spring.datasource.url=jdbc:mysql://localhost:3306/onandhome
spring.datasource.username=root
spring.datasource.password=your_password

# JWT 설정
jwt.secret=your-secret-key-here-minimum-32-characters-long
jwt.expiration=600000
```

## 추가 개선 사항

1. **React Query 도입** (선택사항)
   - 서버 상태 관리 개선
   - 캐싱, 자동 재요청 등

2. **에러 바운더리**
   - 예상치 못한 에러 처리
   - 사용자 친화적인 에러 페이지

3. **로딩 스피너**
   - 전역 로딩 상태 관리
   - 사용자 경험 개선

4. **폼 검증 라이브러리**
   - React Hook Form 또는 Formik
   - 복잡한 폼 관리 개선

5. **UI 컴포넌트 라이브러리**
   - Material-UI 또는 Ant Design
   - 일관된 디자인 시스템

## 문의사항
작업 진행 중 문제가 발생하거나 추가 지원이 필요한 경우 알려주세요.
