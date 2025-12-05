# 프로젝트 수정 이력

## 2025-11-24: 리뷰 및 QnA 수정/삭제 기능 구현

### 백엔드 수정사항

#### 1. SecurityConfig.java
**파일**: `C:\Java\intellijApp\OnAndHomeBack\src\main\java\com\onandhome\SecurityConfig.java`
- QnA와 Review API 전체 경로 허용 (`/api/qna/**`, `/api/reviews/**`)
- CORS 설정 유지
- JWT 토큰 인증은 컨트롤러 레벨에서 처리

#### 2. QnaRestController.java
**파일**: `C:\Java\intellijApp\OnAndHomeBack\src\main\java\com\onandhome\qna\QnaRestController.java`

**추가된 API:**
- `PUT /api/qna/{id}` - QnA 수정
  - JWT 토큰으로 작성자 확인
  - 작성자만 수정 가능
- `DELETE /api/qna/{id}` - QnA 삭제
  - JWT 토큰으로 작성자 확인
  - 작성자만 삭제 가능

#### 3. ReviewRestController.java
**파일**: `C:\Java\intellijApp\OnAndHomeBack\src\main\java\com\onandhome\review\ReviewRestController.java`

**추가된 API:**
- `PUT /api/reviews/{id}` - 리뷰 수정
  - JWT 토큰으로 작성자 확인
  - 작성자만 수정 가능
- `DELETE /api/reviews/{id}` - 리뷰 삭제
  - JWT 토큰으로 작성자 확인
  - 작성자만 삭제 가능

#### 4. ReviewService.java
**파일**: `C:\Java\intellijApp\OnAndHomeBack\src\main\java\com\onandhome\review\ReviewService.java`

**추가된 메서드:**
- `updateReview(Long id, String content, int rating)` - 리뷰 수정

### 프론트엔드 수정사항

#### 1. ReviewItem 컴포넌트
**파일**: `C:\Java\intellijApp\OnAndHomeFront\src\components\review\ReviewItem.js`
**파일**: `C:\Java\intellijApp\OnAndHomeFront\src\components\review\ReviewItem.css`

**기능:**
- 리뷰 표시
- 작성자만 수정/삭제 버튼 표시
- 인라인 수정 모드
- 평점 변경 가능
- 수정 취소 기능

#### 2. QnaItem 컴포넌트
**파일**: `C:\Java\intellijApp\OnAndHomeFront\src\components\qna\QnaItem.js`
**파일**: `C:\Java\intellijApp\OnAndHomeFront\src\components\qna\QnaItem.css`

**기능:**
- QnA 표시 (제목, 내용, 답변)
- 작성자만 수정/삭제 버튼 표시
- 인라인 수정 모드
- 제목과 내용 분리 편집
- 수정 취소 기능

#### 3. ProductDetail.js
**파일**: `C:\Java\intellijApp\OnAndHomeFront\src\pages\user\ProductDetail.js`

**주요 변경사항:**
- ReviewItem과 QnaItem 컴포넌트 import
- QnA 작성 시 제목 입력 필드 추가 (`qnaTitle` state)
- 리뷰/QnA 수정/삭제 핸들러 추가:
  - `handleEditReview`
  - `handleDeleteReview`
  - `handleEditQna`
  - `handleDeleteQna`
- QnA 작성란에 제목과 내용 입력 필드 분리
- 제목 유효성 검사 추가

#### 4. ProductDetail.css
**파일**: `C:\Java\intellijApp\OnAndHomeFront\src\pages\user\ProductDetail.css`

**추가된 스타일:**
- `.input-title` - QnA 제목 입력 필드 스타일
- 포커스 효과
- placeholder 색상

### 주요 기능

1. **작성자 확인**
   - 백엔드: JWT 토큰으로 작성자 확인
   - 프론트엔드: Redux 사용자 정보로 UI 표시/숨김

2. **수정 기능**
   - 리뷰: 내용, 평점 수정 가능
   - QnA: 제목, 내용 수정 가능
   - 인라인 편집 UI
   - 수정 취소 기능

3. **삭제 기능**
   - 삭제 확인 다이얼로그
   - 삭제 후 목록 새로고침

4. **QnA 작성 개선**
   - 제목 입력 필드 추가
   - 제목과 내용 분리
   - 제목 필수 입력 검증

### API 엔드포인트

**QnA:**
- `GET /api/qna/product/{productId}` - 상품별 QnA 조회
- `POST /api/qna` - QnA 작성
- `PUT /api/qna/{id}` - QnA 수정 (작성자만)
- `DELETE /api/qna/{id}` - QnA 삭제 (작성자만)

**Review:**
- `GET /api/reviews/product/{productId}` - 상품별 리뷰 조회
- `POST /api/reviews` - 리뷰 작성
- `PUT /api/reviews/{id}` - 리뷰 수정 (작성자만)
- `DELETE /api/reviews/{id}` - 리뷰 삭제 (작성자만)

### 보안 사항

- JWT 토큰 기반 인증
- Authorization 헤더로 토큰 전송
- 백엔드에서 작성자 확인 후 수정/삭제 허용
- 프론트엔드에서도 UI 레벨 권한 체크

### 테스트 방법

1. Spring Boot 재시작
2. React 앱 재시작
3. 로그인 후 상품 상세 페이지 접속
4. QnA 작성 시 제목과 내용 입력
5. 자신이 작성한 글에 수정/삭제 버튼 확인
6. 수정/삭제 기능 테스트

---

## 이전 작업 이력

### 2025-11-24: 사용자 정보 조회/수정/탈퇴 기능
- MyInfo.js 페이지 구현
- UserController API 추가
- 회원 탈퇴 후 로그인 상태 해제 로직 구현
