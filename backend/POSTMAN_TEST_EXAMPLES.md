# Postman JSON 테스트 컬렉션

## 📌 사용 방법

이 파일을 Postman에 import하면 모든 테스트를 한번에 할 수 있습니다.

### Import 방법:
1. Postman 열기
2. 좌측 "Collections" → "+ Create Collection"
3. 이름: "OnAndHome Auth"
4. 각 요청 추가

---

## 1️⃣ User 회원가입 (방법 1: 웹 페이지)

### GET /admin/signup (페이지 접속)
```
URL: http://localhost:8080/admin/signup
Method: GET
```

**결과:** signup.html 페이지 렌더링

---

### POST /api/user/register (JSON API 호출)
```
URL: http://localhost:8080/api/user/register
Method: POST
Headers: Content-Type: application/json

Body (raw JSON):
{
  "userId": "user001",
  "password": "User1234",
  "email": "user001@example.com",
  "username": "일반사용자1",
  "phone": "010-1111-1111",
  "gender": "M",
  "birthDate": "1990-01-01",
  "address": "서울시 강남구"
}
```

**예상 응답 (201 Created):**
```json
{
  "success": true,
  "message": "회원가입 성공",
  "data": {
    "id": 1,
    "userId": "user001",
    "email": "user001@example.com",
    "username": "일반사용자1",
    "phone": "010-1111-1111",
    "gender": "M",
    "birthDate": "1990-01-01",
    "address": "서울시 강남구",
    "role": "USER",
    "active": true,
    "createdAt": "2024-10-20T15:30:00",
    "updatedAt": "2024-10-20T15:30:00"
  }
}
```

---

## 2️⃣ User 로그인 (방법 1: 웹 페이지)

### GET /admin/login (페이지 접속)
```
URL: http://localhost:8080/admin/login
Method: GET
```

**결과:** login.html 페이지 렌더링 + "회원가입 후 로그인할 수 있습니다" 메시지

---

### POST /admin/login (Form 방식 - 웹 페이지)
```
URL: http://localhost:8080/admin/login
Method: POST
Content-Type: application/x-www-form-urlencoded

Body:
userId=user001&password=User1234
```

**예상 결과:** /admin/dashboard로 리다이렉트

---

### POST /api/user/login (JSON API)
```
URL: http://localhost:8080/api/user/login
Method: POST
Headers: Content-Type: application/json

Body (raw JSON):
{
  "userId": "user001",
  "password": "User1234"
}
```

**예상 응답 (200 OK):**
```json
{
  "success": true,
  "message": "로그인 성공",
  "data": {
    "id": 1,
    "userId": "user001",
    "email": "user001@example.com",
    "username": "일반사용자1",
    "phone": "010-1111-1111",
    "gender": "M",
    "birthDate": "1990-01-01",
    "address": "서울시 강남구",
    "role": "USER",
    "active": true,
    "createdAt": "2024-10-20T15:30:00",
    "updatedAt": "2024-10-20T15:30:00"
  }
}
```

---

## 3️⃣ Admin 회원가입 (동일한 방식)

### POST /api/user/register
```
URL: http://localhost:8080/api/user/register
Method: POST
Headers: Content-Type: application/json

Body (raw JSON):
{
  "userId": "admin001",
  "password": "Admin1234",
  "email": "admin001@example.com",
  "username": "관리자1",
  "phone": "010-9999-9999",
  "gender": "M",
  "birthDate": "1985-05-15",
  "address": "서울시 서초구"
}
```

**예상 응답 (201 Created):**
```json
{
  "success": true,
  "message": "회원가입 성공",
  "data": {
    "id": 2,
    "userId": "admin001",
    "email": "admin001@example.com",
    "username": "관리자1",
    "phone": "010-9999-9999",
    "gender": "M",
    "birthDate": "1985-05-15",
    "address": "서울시 서초구",
    "role": "USER",  ← 주의: 기본값은 USER (나중에 수정 필요)
    "active": true,
    "createdAt": "2024-10-20T15:31:00",
    "updatedAt": "2024-10-20T15:31:00"
  }
}
```

---

## 4️⃣ Admin 로그인 (동일한 방식)

### POST /api/user/login
```
URL: http://localhost:8080/api/user/login
Method: POST
Headers: Content-Type: application/json

Body (raw JSON):
{
  "userId": "admin001",
  "password": "Admin1234"
}
```

**예상 응답 (200 OK):**
```json
{
  "success": true,
  "message": "로그인 성공",
  "data": {
    "id": 2,
    "userId": "admin001",
    "email": "admin001@example.com",
    "username": "관리자1",
    "phone": "010-9999-9999",
    "gender": "M",
    "birthDate": "1985-05-15",
    "address": "서울시 서초구",
    "role": "USER",
    "active": true,
    "createdAt": "2024-10-20T15:31:00",
    "updatedAt": "2024-10-20T15:31:00"
  }
}
```

---

## ✅ 핵심: User와 Admin은 동일한 방식

| 작업 | User | Admin | 차이 |
|------|------|-------|------|
| 회원가입 | `/api/user/register` | `/api/user/register` | ❌ 없음 |
| 로그인 | `/api/user/login` | `/api/user/login` | ❌ 없음 |
| 사용하는 Service | UserService.register() | UserService.register() | ❌ 없음 |
| 데이터베이스 | users 테이블 | users 테이블 | ❌ 없음 |
| 역할 구분 | `role = "USER"` | `role = "ADMIN"` (DB 수정 필요) | ⚠️ role 필드만 다름 |

---

## 🔄 전체 테스트 시나리오

### 시나리오: User와 Admin 모두 가입 및 로그인

```
1️⃣ User 회원가입
   POST /api/user/register
   userId: user001, password: User1234
   → 201 Created, role: USER

2️⃣ Admin 회원가입
   POST /api/user/register
   userId: admin001, password: Admin1234
   → 201 Created, role: USER (주의: 기본값)

3️⃣ User 로그인
   POST /api/user/login
   userId: user001, password: User1234
   → 200 OK, role: USER

4️⃣ Admin 로그인
   POST /api/user/login
   userId: admin001, password: Admin1234
   → 200 OK, role: USER (아직 권한 제어 없음)

5️⃣ User 조회
   GET /api/user/1
   → 200 OK, user001 정보 반환

6️⃣ Admin 조회
   GET /api/user/2
   → 200 OK, admin001 정보 반환
```

---

## ⚠️ 현재 상태 정리

### ✅ 완성된 것
- 회원가입 (role 기본값: USER)
- 로그인 (User/Admin 구분 없음)
- 사용자 조회
- User와 Admin 동일한 로직

### ❌ 아직 구현되지 않은 것
- Admin 전용 기능 보호
- User 전용 기능 보호
- role 기반 권한 제어
- Admin이 회원가입할 때 role을 ADMIN으로 설정

---

## 🎯 Admin 계정을 실제 admin으로 만드는 방법

### 방법 1: MySQL에서 직접 수정
```sql
UPDATE users 
SET role = 'ADMIN' 
WHERE user_id = 'admin001';
```

### 방법 2: 나중에 API 추가
```java
@PutMapping("/{id}/role")
public ResponseEntity<?> updateRole(
    @PathVariable Long id,
    @RequestParam String newRole) {
    // role 업데이트 로직
}
```

---

## 💡 결론

**User와 Admin의 회원가입/로그인은 완벽하게 동일합니다!**

- 같은 API 사용
- 같은 데이터베이스 테이블
- 같은 비즈니스 로직
- `role` 필드로만 구분

**차이는 앞으로 구현될 권한 제어(Authorization)에서 나타날 것입니다.**
