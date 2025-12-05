# On&Home React Frontend - UI 설정 가이드

## 문제 해결 완료 ✅

모든 컴파일 에러가 수정되었고, 기본 UI가 표시되도록 설정되었습니다.

## 완료된 작업

### 1. 컴포넌트 생성
- ✅ 모든 사용자 페이지 생성 (게시판 포함)
- ✅ 모든 관리자 페이지 생성
- ✅ Header, Footer 컴포넌트 완성
- ✅ Layout 컴포넌트 구성

### 2. CSS 스타일링
- ✅ App.css - 글로벌 스타일 및 레이아웃
- ✅ Header.css - 헤더 스타일
- ✅ Footer.css - 푸터 스타일
- ✅ Home.css - 홈 페이지 스타일
- ✅ Login.css - 로그인 페이지 스타일
- ✅ Signup.css - 회원가입 페이지 스타일

### 3. Redux Store 수정
- ✅ userSlice 연결
- ✅ cartSlice, productSlice 유지

### 4. API 수정
- ✅ API export 이름 통일 (대문자/소문자 모두 지원)
- ✅ companyAPI 임시 구현

## 정적 리소스 복사 필요

기존 Spring Boot 프로젝트의 정적 리소스를 React public 폴더로 복사해야 합니다.

### Windows 명령 프롬프트에서 실행:

```cmd
:: 이미지 파일 복사
xcopy "C:\OnAndHome\src\main\resources\static\images" "C:\onandhomefront\public\images" /E /I /Y

:: 상품 이미지 복사
xcopy "C:\OnAndHome\src\main\resources\static\product_img" "C:\onandhomefront\public\product_img" /E /I /Y

:: 폰트 파일 복사 (선택)
xcopy "C:\OnAndHome\src\main\resources\static\font" "C:\onandhomefront\public\font" /E /I /Y
```

## 실행 방법

### 1. Spring Boot 백엔드 실행
```bash
cd C:\OnAndHome
./gradlew bootRun
# 또는 IntelliJ에서 실행
```

### 2. React 프론트엔드 실행
```bash
cd C:\onandhomefront
npm start
```

### 3. 브라우저에서 확인
- http://localhost:3000 접속
- 헤더, 푸터, 홈 화면이 표시되어야 함

## 현재 화면 구성

### 홈 화면 (/)
- ✅ 헤더: 로고, 네비게이션, 로그인/회원가입 버튼
- ✅ 메인 배너 (이미지가 있으면 표시)
- ✅ 추천 상품 목록 (최대 8개)
- ✅ 푸터: 회사 정보, 연락처

### 로그인 상태
- ✅ 사용자 이름 표시
- ✅ 마이페이지 버튼
- ✅ 로그아웃 버튼
- ✅ 장바구니 아이콘 (개수 표시)
- ✅ 관리자는 관리자 버튼 추가 표시

## 테스트 시나리오

### 1. 기본 화면 확인
1. React 앱 실행
2. 헤더가 표시되는지 확인
3. 푸터가 표시되는지 확인
4. "등록된 상품이 없습니다" 메시지 또는 상품 목록 확인

### 2. 네비게이션 테스트
1. 로그인 버튼 클릭 → 로그인 페이지로 이동
2. 회원가입 버튼 클릭 → 회원가입 페이지로 이동
3. 메뉴 클릭 → 각 페이지로 이동 (개발 중 메시지 표시)

### 3. 로그인 테스트
1. 로그인 페이지에서 테스트 계정으로 로그인
2. 홈 화면으로 리다이렉트
3. 헤더에 사용자 이름 표시 확인
4. 마이페이지, 로그아웃 버튼 표시 확인

## 문제 해결

### 화면이 여전히 빈 페이지인 경우

1. **개발자 도구 콘솔 확인**
   - F12 키를 눌러 콘솔 탭 확인
   - 에러 메시지가 있는지 확인

2. **Spring Boot 서버 확인**
   - Spring Boot가 8080 포트에서 실행 중인지 확인
   - CORS 설정이 되어 있는지 확인

3. **React 재시작**
   ```bash
   # Ctrl+C로 중지 후
   npm start
   ```

4. **브라우저 캐시 삭제**
   - Ctrl+Shift+R (강력 새로고침)
   - 또는 시크릿 모드로 접속

### API 연결 오류

- `http://localhost:8080`에서 Spring Boot가 실행 중인지 확인
- SecurityConfig에서 CORS 설정 확인
- 네트워크 탭에서 API 요청 상태 확인

### 이미지가 표시되지 않는 경우

1. 정적 리소스가 제대로 복사되었는지 확인
2. public 폴더 구조 확인:
   ```
   public/
   ├── images/
   ├── product_img/
   └── font/
   ```

## 다음 단계

### 우선순위 높음
1. ProductList 페이지 구현
2. ProductDetail 페이지 구현
3. Cart 페이지 구현
4. MyPage 페이지 구현

### 우선순위 중간
5. 게시판 페이지들 구현 (공지사항, Q&A, 리뷰)
6. 주문 관련 페이지 구현

### 우선순위 낮음
7. 관리자 페이지들 구현

## 참고사항

- 모든 페이지는 기본 레이아웃(헤더+푸터)이 적용됩니다
- 로그인이 필요한 페이지는 자동으로 로그인 페이지로 리다이렉트됩니다
- JWT 토큰은 localStorage에 저장됩니다
- axios interceptor가 자동으로 토큰을 헤더에 추가합니다

## 문의

문제가 계속되면:
1. 콘솔 에러 메시지 확인
2. 네트워크 탭에서 API 요청 확인
3. Spring Boot 로그 확인
