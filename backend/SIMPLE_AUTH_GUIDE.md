# 🏠 온앤홈 - 회원가입/로그인 사용 가이드

## ✅ 완성된 기능

### 1️⃣ **회원가입 (회원가입만으로 시작)**
- 더 이상 초기 테스트 계정이 없습니다
- 직접 회원가입하여 계정을 만들어야 합니다
- 만든 계정으로 로그인 가능

### 2️⃣ **로그인 (회원가입한 정보로)**
- 회원가입 시 등록한 ID와 Password로 로그인
- 평문 비밀번호 비교 (간단한 로직)
- 로그인 성공 시 세션에 사용자 정보 저장

---

## 🚀 빠른 시작

### 1단계: 애플리케이션 시작
```bash
gradle bootRun
```

### 2단계: 로그인 페이지 접속
```
http://localhost:8080/admin/login
```

**화면에 보이는 것:**
- 로그인 입력 폼
- "회원가입" 버튼
- "아직 계정이 없으신가요?" 메시지

### 3단계: 회원가입 클릭
로그인 페이지의 **"회원가입"** 버튼을 클릭합니다.

### 4단계: 회원정보 입력
회원가입 페이지에서 다음 정보를 입력합니다:

**필수 항목:**
- 아이디 (4-20글자, 영문/숫자)
- 비밀번호 (8글자 이상)
- 비밀번호 확인 (비밀번호 재입력)
- 이메일 (유효한 이메일)
- 이름

**선택 항목:**
- 휴대폰
- 성별
- 생년월일
- 주소

### 5단계: 회원가입 완료
"회원가입" 버튼을 클릭하면:
1. 정보 검증
2. 아이디 중복 검사
3. 이메일 중복 검사
4. DB 저장
5. 자동으로 로그인 페이지로 이동

### 6단계: 로그인
로그인 페이지에서 방금 가입한 ID와 Password를 입력하여 로그인합니다.

---

## 📝 예시

### 회원가입 예시
```
아이디: john123
비밀번호: mypassword123
이메일: john@example.com
이름: John Doe
휴대폰: 010-1234-5678
성별: 남성
생년월일: 1990-01-15
주소: 서울시 강남구 테헤란로 123
```

### 로그인 (위 계정으로)
```
ID: john123
Password: mypassword123
```

---

## ⚙️ 수정된 파일 목록

### 1. DataInitializer.java
- ❌ 초기 테스트 계정 생성 제거
- ✅ 회원가입으로만 사용자 생성

### 2. login.html
- ✅ 정보 메시지 추가 ("회원가입 후 로그인할 수 있습니다")
- ✅ 회원가입 버튼 추가
- ✅ 회원가입 완료 메시지 처리

### 3. signup.html
- ✅ 회원가입 후 `?signup=true` 파라미터와 함께 로그인 페이지로 리다이렉트

### 4. UserService.java
- ✅ 평문 비밀번호 비교
- ✅ 아이디/이메일 중복 검사

### 5. UserController.java
- ✅ 회원가입 API 구현
- ✅ 로그인 API 구현 (세션 저장)

---

## 🔄 플로우 다이어그램

```
시작
  ↓
로그인 페이지 (http://localhost:8080/admin/login)
  ↓
회원가입? → YES → 회원가입 페이지 (http://localhost:8080/admin/signup)
  ↓                ↓
  NO              정보 입력
  ↓                ↓
ID/PW 입력        검증 (아이디, 비밀번호, 이메일 중복 검사)
  ↓                ↓
검증               DB 저장
  ↓                ↓
성공?              성공!
  ↓                ↓
로그인 상태 진입   로그인 페이지로 리다이렉트
  ↓                ↓
대시보드           로그인 페이지 (?signup=true)
                   ↓
                  ID/PW 입력하여 로그인
                   ↓
                  로그인 상태 진입
```

---

## 📡 API 요청 예시

### 회원가입 요청
```bash
curl -X POST http://localhost:8080/api/user/register \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "john123",
    "password": "mypassword123",
    "email": "john@example.com",
    "username": "John Doe",
    "phone": "010-1234-5678",
    "gender": "M",
    "birthDate": "1990-01-15",
    "address": "서울시 강남구"
  }'
```

### 회원가입 성공 응답
```json
{
  "success": true,
  "message": "회원가입 성공",
  "data": {
    "id": 1,
    "userId": "john123",
    "email": "john@example.com",
    "username": "John Doe",
    "phone": "010-1234-5678",
    "gender": "M",
    "birthDate": "1990-01-15",
    "address": "서울시 강남구",
    "role": "USER",
    "active": true,
    "createdAt": "2024-10-20T15:30:00",
    "updatedAt": "2024-10-20T15:30:00"
  }
}
```

### 로그인 요청
```bash
curl -X POST http://localhost:8080/api/user/login \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "john123",
    "password": "mypassword123"
  }'
```

### 로그인 성공 응답
```json
{
  "success": true,
  "message": "로그인 성공",
  "data": {
    "id": 1,
    "userId": "john123",
    "email": "john@example.com",
    "username": "John Doe",
    "phone": "010-1234-5678",
    "gender": "M",
    "birthDate": "1990-01-15",
    "address": "서울시 강남구",
    "role": "USER",
    "active": true,
    "createdAt": "2024-10-20T15:30:00",
    "updatedAt": "2024-10-20T15:30:00"
  }
}
```

---

## ⚠️ 주의사항

### 테스트 계정이 없습니다!
- ❌ admin/admin123
- ❌ 초기 데이터 없음
- ✅ 반드시 회원가입 먼저 하세요

### 데이터 초기화 방법
데이터베이스를 초기화하려면:

**MySQL에서:**
```sql
DELETE FROM users;
ALTER TABLE users AUTO_INCREMENT = 1;
```

**application.properties에서:** (주의: 모든 데이터 삭제)
```properties
spring.jpa.hibernate.ddl-auto=create  -- 데이터베이스 재생성
```

---

## 🔐 보안 수준

이 구현은 **개발/학습 목적**입니다.

**현재 구현:**
- ✅ 기본적인 회원가입/로그인
- ✅ 아이디/이메일 중복 검사
- ✅ 평문 비밀번호 비교

**프로덕션에서 필요한 것:**
- ❌ 비밀번호 암호화 (BCrypt, Argon2)
- ❌ Spring Security
- ❌ JWT 토큰
- ❌ HTTPS
- ❌ CSRF 보호
- ❌ 레이트 리미팅
- ❌ 입력값 검증 강화

---

## 📞 문제 해결

### Q: 회원가입 페이지가 안 열려요
**A:** URL 확인: `http://localhost:8080/admin/signup`

### Q: 회원가입은 되는데 로그인이 안 돼요
**A:** 
1. ID와 Password가 정확한지 확인
2. 대소문자 구분
3. 아이디는 중복이 없어야 함

### Q: 데이터베이스 연결 오류
**A:** application.properties 확인:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/onandhome
spring.datasource.username=admin
spring.datasource.password=1111
```

### Q: 아이디가 계속 중복이라고 나와요
**A:** 이미 가입된 아이디입니다. 다른 아이디 사용

---

**작성일**: 2024-10-20
**최종 수정**: 2024-10-20
