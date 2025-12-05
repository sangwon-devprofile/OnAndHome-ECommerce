# JWT 인증 시스템 전체 흐름 문서

## 📋 목차
1. [JWT 인증 시스템 개요](#1-jwt-인증-시스템-개요)
2. [주요 파일 구성](#2-주요-파일-구성)
3. [로그인 처리 전체 흐름](#3-로그인-처리-전체-흐름)
4. [JWT 토큰 생성 및 검증](#4-jwt-토큰-생성-및-검증)
5. [API 요청 시 인증 처리](#5-api-요청-시-인증-처리)
6. [토큰 갱신 (Refresh Token)](#6-토큰-갱신-refresh-token)
7. [관리자 권한 검증](#7-관리자-권한-검증)
8. [로그아웃 처리](#8-로그아웃-처리)
9. [전체 데이터 흐름도](#9-전체-데이터-흐름도)
10. [보안 고려사항](#10-보안-고려사항)

---

## 1. JWT 인증 시스템 개요

### 1.1 시스템 구조
- **인증 방식**: JWT (JSON Web Token) 기반 Stateless 인증
- **토큰 종류**: Access Token (60분) + Refresh Token (7일)
- **권한 관리**: Role 기반 (0=관리자, 1=일반사용자)
- **저장소**: 프론트엔드 localStorage + Redux store

### 1.2 JWT 토큰 구조
```
eyJhbGciOiJIUzI1NiJ9.eyJpZCI6MSwidXNlcklkIjoidXNlcjEyMyIsInJvbGUiOjEsImV4cCI6MTcwMDAwMDAwMH0.signature
    ↑ Header         ↑ Payload (Claims)                                                   ↑ Signature
```

**Claims (페이로드) 포함 정보:**
- `id`: 사용자 PK (Long)
- `userId`: 로그인 아이디 (String)
- `role`: 사용자 권한 (Integer) - 0=관리자, 1=일반사용자
- `marketingConsent`: 광고 수신 동의 (Boolean)
- `exp`: 만료 시간 (Unix Timestamp)

---

## 2. 주요 파일 구성

### 2.1 백엔드 (Spring Boot)

#### 📁 **JWTUtil.java**
**위치**: `C:\himedia\project\OnAndHome\src\main\java\com\onandhome\util\JWTUtil.java`

**역할**: JWT 토큰 생성 및 검증

**주요 메소드**:
- `generateToken(Map<String, Object> claims, long expirationMinutes)`: JWT 토큰 생성
  - 클레임과 만료 시간을 받아 HMAC SHA256으로 서명된 JWT 생성
  - 반환: "eyJhbGciOiJIUzI1NiJ9..." 형태의 JWT 문자열

- `validateToken(String token)`: JWT 토큰 검증
  - 서명 검증 및 만료 시간 확인
  - 반환: Claims (Map<String, Object>) - 토큰이 유효하면 클레임 반환
  - 예외: 토큰이 만료되거나 서명이 잘못되면 예외 발생

**사용하는 곳**:
- UserController.login() - 로그인 시 토큰 생성
- UserController.refresh() - 토큰 갱신
- JwtAuthenticationFilter - 모든 요청에서 토큰 검증

---

#### 📁 **UserController.java**
**위치**: `C:\himedia\project\OnAndHome\src\main\java\com\onandhome\user\UserController.java`

**역할**: 사용자 인증 및 회원 관리 API

**주요 메소드**:

1. **`login(@RequestBody LoginRequest)`** - POST /api/user/login
   - 사용자 인증 후 JWT 토큰 발급
   - 처리 과정:
     1. UserService.login()으로 userId/password 검증 (BCrypt)
     2. JWT 클레임 생성 (id, userId, role, marketingConsent)
     3. Access Token (60분) + Refresh Token (7일) 생성
     4. 응답: {success, accessToken, refreshToken, user}
   - 호출 위치: Login.js, AdminLogin.js

2. **`refresh(@RequestHeader("Authorization"))`** - POST /api/user/refresh
   - Refresh Token으로 새로운 Access Token 발급
   - 처리 과정:
     1. Refresh Token 검증 (JWTUtil.validateToken)
     2. 클레임 추출
     3. 새로운 Access Token 생성 (60분)
     4. 응답: {success, accessToken}
   - 호출 위치: axios interceptor (401 에러 발생 시 자동)

3. **`getSessionInfo(@RequestHeader("Authorization"))`** - GET /api/user/session-info
   - JWT 토큰으로 현재 로그인 사용자 정보 조회
   - 처리 과정:
     1. Access Token 검증
     2. 클레임에서 userId 추출
     3. DB에서 사용자 정보 조회
     4. 응답: {loggedIn: true, user, isAdmin: role===0}

---

#### 📁 **JwtAuthenticationFilter.java**
**위치**: `C:\himedia\project\OnAndHome\src\main\java\com\onandhome\config\JwtAuthenticationFilter.java`

**역할**: 모든 HTTP 요청에서 JWT 토큰 자동 검증

**처리 흐름**:
```java
doFilterInternal(request, response, filterChain) {
    1. Authorization 헤더에서 JWT 토큰 추출
       - "Bearer eyJhbGciOiJIUzI1NiJ9..." → "eyJhbGciOiJIUzI1NiJ9..."
    
    2. JWTUtil.validateToken(token) 호출하여 검증
       - 서명 검증 + 만료 시간 확인
    
    3. 클레임에서 userId, role 추출
    
    4. Spring Security Context에 인증 정보 저장
       - UsernamePasswordAuthenticationToken 생성
       - authorities: "ROLE_ADMIN" (role=0) or "ROLE_USER" (role=1)
    
    5. filterChain.doFilter() - 다음 필터로 전달
}
```

**예외 처리**:
- 토큰이 없거나 유효하지 않으면 → 인증 없이 다음 필터로 전달 (401 에러는 Controller에서 발생)

---

#### 📁 **UserService.java**
**위치**: `C:\himedia\project\OnAndHome\src\main\java\com\onandhome\user\UserService.java`

**역할**: 사용자 비즈니스 로직 처리

**주요 메소드**:
- `login(String userId, String password)`: 사용자 인증
  - UserRepository.findByUserId() → DB에서 사용자 조회
  - passwordEncoder.matches() → BCrypt 비밀번호 검증
  - 반환: Optional<UserDTO> (인증 성공 시 사용자 정보)

---

### 2.2 프론트엔드 (React)

#### 📁 **Login.js**
**위치**: `C:\himedia\project\OnAndHomeFront\src\pages\user\Login.js`

**역할**: 일반 사용자 로그인 페이지

**주요 함수**:
```javascript
handleSubmit(e) {
    1. authApi.login(formData) 호출
       - POST http://localhost:8080/api/user/login
       - Body: {userId: "user123", password: "Password1!"}
    
    2. 응답 받기: {success, accessToken, refreshToken, user}
    
    3. Redux store 업데이트
       - dispatch(login({accessToken, refreshToken, user}))
       - localStorage에도 자동 저장 (userSlice에서 처리)
    
    4. 메인 페이지로 이동
       - navigate('/') - role 구분 없이 모두 메인 페이지
}
```

---

#### 📁 **AdminLogin.js**
**위치**: `C:\himedia\project\OnAndHomeFront\src\pages\admin\AdminLogin.js`

**역할**: 관리자 전용 로그인 페이지

**주요 함수**:
```javascript
handleSubmit(e) {
    1. axios.post('/api/user/login', {userId, password}) 호출
       - Login.js와 동일한 API 사용
    
    2. 응답 받기: {success, accessToken, refreshToken, user}
    
    3. ★ 관리자 권한 검증 (핵심!)
       - if (user.role !== 0) {
           에러: "관리자 권한이 없습니다"
           return; // 로그인 차단
         }
    
    4. 토큰 저장
       - localStorage.setItem('accessToken', accessToken)
       - localStorage.setItem('refreshToken', refreshToken)
       - localStorage.setItem('userInfo', JSON.stringify(user))
    
    5. Redux store 업데이트
       - dispatch(loginSuccess({user, accessToken}))
    
    6. 관리자 대시보드로 이동
       - navigate('/admin/dashboard')
}
```

**일반 로그인과의 차이점**:
- Login.js: role 검증 없음 → 메인 페이지
- AdminLogin.js: **user.role === 0 체크** → 관리자 대시보드

---

#### 📁 **userSlice.js**
**위치**: `C:\himedia\project\OnAndHomeFront\src\store\slices\userSlice.js`

**역할**: Redux 상태 관리 (로그인 상태, 사용자 정보, 토큰)

**주요 리듀서**:

1. **login(state, action)**:
   ```javascript
   - payload: {accessToken, refreshToken, user}
   - state.isAuthenticated = true
   - state.user = user
   - state.accessToken = accessToken
   
   - localStorage에 저장:
     * localStorage.setItem('accessToken', accessToken)
     * localStorage.setItem('refreshToken', refreshToken)
     * localStorage.setItem('userInfo', JSON.stringify(user))
   ```

2. **logout(state)**:
   ```javascript
   - state.isAuthenticated = false
   - state.user = null
   - state.accessToken = null
   
   - localStorage에서 삭제:
     * localStorage.removeItem('accessToken')
     * localStorage.removeItem('refreshToken')
     * localStorage.removeItem('userInfo')
   ```

3. **updateAccessToken(state, action)**:
   ```javascript
   - payload: {accessToken}
   - state.accessToken = accessToken
   - localStorage.setItem('accessToken', accessToken)
   ```

**초기 상태 복원**:
```javascript
// 페이지 새로고침 시 localStorage에서 복원
const initialState = {
    isAuthenticated: !!localStorage.getItem('accessToken'),
    user: JSON.parse(localStorage.getItem('userInfo') || 'null'),
    accessToken: localStorage.getItem('accessToken'),
};
```

---

#### 📁 **axiosInstance.js**
**위치**: `C:\himedia\project\OnAndHomeFront\src\api\axiosInstance.js`

**역할**: axios 인터셉터를 통한 자동 토큰 처리

**Request Interceptor** (요청 전):
```javascript
axios.interceptors.request.use((config) => {
    const token = localStorage.getItem('accessToken');
    
    if (token) {
        // 모든 요청에 Authorization 헤더 자동 추가
        config.headers.Authorization = `Bearer ${token}`;
    }
    
    return config;
});
```

**Response Interceptor** (응답 후):
```javascript
axios.interceptors.response.use(
    (response) => response, // 성공 시 그대로 반환
    
    async (error) => {
        // 401 Unauthorized 에러 발생 시
        if (error.response?.status === 401) {
            const refreshToken = localStorage.getItem('refreshToken');
            
            if (refreshToken) {
                try {
                    // 1. Refresh Token으로 새로운 Access Token 발급
                    const response = await axios.post('/api/user/refresh', {}, {
                        headers: { Authorization: `Bearer ${refreshToken}` }
                    });
                    
                    const newAccessToken = response.data.accessToken;
                    
                    // 2. 새 Access Token 저장
                    localStorage.setItem('accessToken', newAccessToken);
                    store.dispatch(updateAccessToken({accessToken: newAccessToken}));
                    
                    // 3. 원래 요청 재시도
                    error.config.headers.Authorization = `Bearer ${newAccessToken}`;
                    return axios(error.config);
                    
                } catch (refreshError) {
                    // Refresh Token도 만료되면 로그아웃
                    store.dispatch(logout());
                    window.location.href = '/login';
                }
            }
        }
        
        return Promise.reject(error);
    }
);
```

---

## 3. 로그인 처리 전체 흐름

### 3.1 일반 사용자 로그인

```
[사용자 입력]
└─ userId: "user123"
└─ password: "Password1!"

[프론트엔드 - Login.js]
1. handleSubmit() 실행
   └─ authApi.login({userId, password})
      └─ POST http://localhost:8080/api/user/login
         └─ Body: {userId: "user123", password: "Password1!"}

[백엔드 - UserController.java]
2. login(@RequestBody LoginRequest) 실행
   └─ UserService.login(userId, password) 호출
      ├─ UserRepository.findByUserId(userId)
      │  └─ SQL: SELECT * FROM user WHERE user_id = 'user123'
      │  └─ DB에서 User 엔티티 조회
      │
      ├─ passwordEncoder.matches(password, user.getPassword())
      │  └─ BCrypt 검증: "Password1!" vs "$2a$10$..."
      │  └─ 일치하면 true, 불일치하면 false
      │
      └─ 반환: Optional<UserDTO>
         └─ {id: 1, userId: "user123", role: 1, username: "홍길동", ...}

3. JWT 클레임 생성
   └─ Map<String, Object> claims = {
       "id": 1,
       "userId": "user123",
       "role": 1,
       "marketingConsent": false
   }

4. JWT 토큰 생성
   ├─ Access Token: jwtUtil.generateToken(claims, 60)
   │  └─ "eyJhbGciOiJIUzI1NiJ9.eyJpZCI6MSwidXNlcklkIjoidXNlcjEyMyIsInJvbGUiOjEsImV4cCI6MTcwMDAwNjAwMH0.signature"
   │
   └─ Refresh Token: jwtUtil.generateToken(claims, 60 * 24 * 7)
      └─ "eyJhbGciOiJIUzI1NiJ9.eyJpZCI6MSwidXNlcklkIjoidXNlcjEyMyIsInJvbGUiOjEsImV4cCI6MTcwMDYwNDgwMH0.signature"

5. 응답 반환 (HTTP 200 OK)
   └─ {
       "success": true,
       "message": "로그인 성공",
       "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
       "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
       "user": {
           "id": 1,
           "userId": "user123",
           "role": 1,
           "username": "홍길동",
           "email": "user@example.com"
       }
   }

[프론트엔드 - Login.js]
6. 응답 처리
   ├─ dispatch(login({accessToken, refreshToken, user}))
   │  └─ Redux store 업데이트
   │  └─ localStorage.setItem('accessToken', accessToken)
   │  └─ localStorage.setItem('refreshToken', refreshToken)
   │  └─ localStorage.setItem('userInfo', JSON.stringify(user))
   │
   └─ navigate('/')
      └─ 메인 페이지로 이동
```

---

### 3.2 관리자 로그인

```
[프론트엔드 - AdminLogin.js]
1. handleSubmit() 실행
   └─ axios.post('/api/user/login', {userId, password})
      └─ 동일한 API 사용 (UserController.login)

[백엔드]
2. 일반 로그인과 동일한 처리
   └─ JWT 토큰 생성 (role이 0 또는 1일 수 있음)
   └─ 응답: {success, accessToken, refreshToken, user}

[프론트엔드 - AdminLogin.js]
3. ★ 관리자 권한 검증 (핵심 차이점!)
   └─ if (user.role !== 0) {
       setError("관리자 권한이 없습니다");
       return; // 로그인 차단
   }

4. role=0인 경우에만 진행
   ├─ localStorage.setItem('accessToken', accessToken)
   ├─ localStorage.setItem('refreshToken', refreshToken)
   ├─ localStorage.setItem('userInfo', JSON.stringify(user))
   ├─ dispatch(loginSuccess({user, accessToken}))
   │
   └─ navigate('/admin/dashboard')
      └─ 관리자 대시보드로 이동

5. role=1인 경우
   └─ "관리자 권한이 없습니다" 에러 표시
   └─ 로그인 차단
```

**차이점 요약**:
| 구분 | Login.js (일반 로그인) | AdminLogin.js (관리자 로그인) |
|------|----------------------|---------------------------|
| API | POST /api/user/login | POST /api/user/login (동일) |
| role 검증 | 없음 (모두 허용) | **user.role === 0 체크** |
| 이동 경로 | `/` (메인 페이지) | `/admin/dashboard` |
| 실패 시 | 에러 메시지 | "관리자 권한이 없습니다" |

---

## 4. JWT 토큰 생성 및 검증

### 4.1 토큰 생성 (JWTUtil.generateToken)

**메소드 시그니처**:
```java
public String generateToken(Map<String, Object> claims, long expirationMinutes)
```

**처리 과정**:
```java
1. 현재 시간 기준으로 만료 시간 계산
   - Date now = new Date();
   - Date expiration = new Date(now.getTime() + expirationMinutes * 60 * 1000);

2. JWT 빌더로 토큰 생성
   - Jwts.builder()
       .setClaims(claims)              // 클레임 설정
       .setIssuedAt(now)               // 발급 시간
       .setExpiration(expiration)       // 만료 시간
       .signWith(secretKey, SignatureAlgorithm.HS256)  // HMAC SHA256 서명
       .compact();                      // 문자열로 변환

3. 생성된 JWT 문자열 반환
   - "eyJhbGciOiJIUzI1NiJ9.eyJpZCI6MSwidXNlcklkIjoidXNlcjEyMyIsInJvbGUiOjEsImV4cCI6MTcwMDAwNjAwMH0.signature"
```

**JWT 구조 분해**:
```
Header (Base64):
{
  "alg": "HS256",
  "typ": "JWT"
}

Payload (Base64):
{
  "id": 1,
  "userId": "user123",
  "role": 1,
  "marketingConsent": false,
  "iat": 1700000000,  // 발급 시간
  "exp": 1700003600   // 만료 시간 (60분 후)
}

Signature (HMAC SHA256):
HMACSHA256(
  base64UrlEncode(header) + "." + base64UrlEncode(payload),
  secretKey
)
```

---

### 4.2 토큰 검증 (JWTUtil.validateToken)

**메소드 시그니처**:
```java
public Map<String, Object> validateToken(String token)
```

**처리 과정**:
```java
1. JWT 파싱 및 서명 검증
   - Jws<Claims> jws = Jwts.parserBuilder()
       .setSigningKey(secretKey)
       .build()
       .parseClaimsJws(token);

2. 검증 항목:
   ├─ 서명 검증: secretKey로 서명이 올바른지 확인
   │  - 위조된 토큰이면 예외 발생
   │
   ├─ 만료 시간 검증: 현재 시간 vs exp 비교
   │  - 만료되었으면 ExpiredJwtException 발생
   │
   └─ 형식 검증: JWT 형식이 올바른지 확인
      - 형식이 잘못되었으면 MalformedJwtException 발생

3. 클레임 추출 및 반환
   - Claims claims = jws.getBody();
   - Map<String, Object> claimsMap = new HashMap<>(claims);
   - return claimsMap;
   
   예시:
   {
     "id": 1,
     "userId": "user123",
     "role": 1,
     "marketingConsent": false,
     "iat": 1700000000,
     "exp": 1700003600
   }
```

**예외 처리**:
- `ExpiredJwtException`: 토큰 만료
- `SignatureException`: 서명 불일치 (위조된 토큰)
- `MalformedJwtException`: 잘못된 JWT 형식

---

## 5. API 요청 시 인증 처리

### 5.1 전체 흐름

```
[프론트엔드]
사용자가 API 요청 (예: 장바구니 조회)
└─ axios.get('/api/cart')

[axiosInstance.js - Request Interceptor]
요청 전 자동 처리:
├─ const token = localStorage.getItem('accessToken');
└─ config.headers.Authorization = `Bearer ${token}`;

HTTP 요청:
GET /api/cart
Headers: {
  Authorization: "Bearer eyJhbGciOiJIUzI1NiJ9..."
}

[백엔드 - JwtAuthenticationFilter]
모든 요청에 대해 자동 실행:
1. Authorization 헤더에서 토큰 추출
   - String authHeader = request.getHeader("Authorization");
   - String token = authHeader.substring(7); // "Bearer " 제거

2. JWTUtil.validateToken(token) 호출
   - 서명 검증
   - 만료 시간 확인
   - 클레임 추출: {id: 1, userId: "user123", role: 1}

3. Spring Security Context에 인증 정보 저장
   - UserDetails userDetails = new User(userId, "", authorities);
   - Authentication auth = new UsernamePasswordAuthenticationToken(...);
   - SecurityContextHolder.getContext().setAuthentication(auth);

4. 다음 필터로 전달
   - filterChain.doFilter(request, response);

[백엔드 - CartController]
컨트롤러에서 인증 정보 사용:
@GetMapping("/api/cart")
public ResponseEntity<?> getCart(@RequestHeader("Authorization") String authHeader) {
    // JwtAuthenticationFilter가 이미 검증했으므로
    // 토큰이 유효하다고 가정하고 처리
    
    String token = authHeader.substring(7);
    Map<String, Object> claims = jwtUtil.validateToken(token);
    Long userId = (Long) claims.get("id");
    
    // userId로 장바구니 조회
    List<CartItem> items = cartService.getCartItems(userId);
    return ResponseEntity.ok(items);
}

[프론트엔드]
응답 받기:
└─ const response = await axios.get('/api/cart');
   └─ response.data: [{productId: 1, quantity: 2}, ...]
```

---

### 5.2 토큰 만료 시 처리

```
[프론트엔드]
사용자가 API 요청
└─ axios.get('/api/cart')
   └─ Authorization: "Bearer <만료된 Access Token>"

[백엔드 - JwtAuthenticationFilter]
1. JWTUtil.validateToken(token) 호출
   └─ 토큰 만료 → ExpiredJwtException 발생

2. 예외 처리
   └─ 필터에서 예외를 잡지 않고 Controller로 전달
      └─ Controller에서 401 Unauthorized 반환

[프론트엔드 - axiosInstance.js Response Interceptor]
1. 401 에러 감지
   - if (error.response?.status === 401)

2. Refresh Token으로 새 Access Token 발급 시도
   ├─ const refreshToken = localStorage.getItem('refreshToken');
   │
   └─ POST /api/user/refresh
      └─ Headers: {Authorization: "Bearer <Refresh Token>"}

[백엔드 - UserController.refresh()]
3. Refresh Token 검증
   ├─ JWTUtil.validateToken(refreshToken)
   │  └─ 클레임 추출: {id: 1, userId: "user123", role: 1}
   │
   └─ 새 Access Token 생성
      └─ String newAccessToken = jwtUtil.generateToken(claims, 60);

4. 응답 반환
   └─ {success: true, accessToken: "eyJhbGciOiJIUzI1NiJ9..."}

[프론트엔드 - axiosInstance.js]
5. 새 Access Token 저장
   ├─ localStorage.setItem('accessToken', newAccessToken);
   └─ store.dispatch(updateAccessToken({accessToken: newAccessToken}));

6. 원래 요청 재시도
   ├─ error.config.headers.Authorization = `Bearer ${newAccessToken}`;
   └─ return axios(error.config); // 원래 요청 재실행
      └─ GET /api/cart (새 토큰으로 다시 요청)

7. 성공 응답 받기
   └─ [{productId: 1, quantity: 2}, ...]
```

---

## 6. 토큰 갱신 (Refresh Token)

### 6.1 토큰 갱신 API

**엔드포인트**: POST /api/user/refresh

**요청**:
```http
POST /api/user/refresh
Headers: {
  Authorization: "Bearer <Refresh Token>"
}
```

**백엔드 처리 (UserController.refresh)**:
```java
@PostMapping("/refresh")
public ResponseEntity<Map<String, Object>> refresh(
        @RequestHeader("Authorization") String authHeader) {
    
    Map<String, Object> response = new HashMap<>();
    
    try {
        // 1. Refresh Token 추출
        String refreshToken = authHeader.substring(7);
        
        // 2. Refresh Token 검증 및 클레임 추출
        Map<String, Object> claims = jwtUtil.validateToken(refreshToken);
        
        // 3. 새로운 Access Token 생성 (동일한 클레임 사용)
        String newAccessToken = jwtUtil.generateToken(claims, 60);
        
        // 4. 응답 반환
        response.put("success", true);
        response.put("accessToken", newAccessToken);
        
        return ResponseEntity.ok(response);
        
    } catch (ExpiredJwtException e) {
        // Refresh Token도 만료됨 → 재로그인 필요
        response.put("success", false);
        response.put("message", "Refresh Token이 만료되었습니다.");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
}
```

**응답**:
```json
{
  "success": true,
  "accessToken": "eyJhbGciOiJIUzI1NiJ9.새로운토큰클레임.새로운서명"
}
```

---

### 6.2 자동 토큰 갱신 (Axios Interceptor)

**위치**: axiosInstance.js

**동작 원리**:
```javascript
axios.interceptors.response.use(
    (response) => response, // 성공 시 그대로 반환
    
    async (error) => {
        const originalRequest = error.config;
        
        // 1. 401 에러이고, 재시도하지 않은 요청인 경우
        if (error.response?.status === 401 && !originalRequest._retry) {
            originalRequest._retry = true; // 무한 루프 방지
            
            const refreshToken = localStorage.getItem('refreshToken');
            
            if (refreshToken) {
                try {
                    // 2. Refresh Token으로 새 Access Token 발급
                    const response = await axios.post('/api/user/refresh', {}, {
                        headers: { Authorization: `Bearer ${refreshToken}` }
                    });
                    
                    const newAccessToken = response.data.accessToken;
                    
                    // 3. 새 Access Token 저장
                    localStorage.setItem('accessToken', newAccessToken);
                    store.dispatch(updateAccessToken({ accessToken: newAccessToken }));
                    
                    // 4. 원래 요청에 새 토큰 적용 후 재시도
                    originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
                    return axios(originalRequest);
                    
                } catch (refreshError) {
                    // 5. Refresh Token도 만료되면 로그아웃
                    store.dispatch(logout());
                    window.location.href = '/login';
                    return Promise.reject(refreshError);
                }
            }
        }
        
        return Promise.reject(error);
    }
);
```

**시나리오 예시**:
```
시간: 10:00 - 사용자 로그인
└─ Access Token 발급 (만료: 11:00)
└─ Refresh Token 발급 (만료: 7일 후)

시간: 10:59 - 사용자가 장바구니 조회
└─ GET /api/cart
   └─ Authorization: Bearer <Access Token>
   └─ 성공 (토큰 유효)

시간: 11:01 - 사용자가 주문 생성 시도
└─ POST /api/orders
   └─ Authorization: Bearer <만료된 Access Token>
   └─ 401 Unauthorized (Access Token 만료!)

[Axios Interceptor 자동 처리]
1. 401 에러 감지
2. POST /api/user/refresh (Refresh Token 사용)
3. 새 Access Token 받기 (만료: 12:01)
4. POST /api/orders (새 토큰으로 재시도)
5. 성공!

사용자는 로그인 만료를 전혀 느끼지 못함!
```

---

## 7. 관리자 권한 검증

### 7.1 Role 값 의미

```java
// User 엔티티 (DB)
public class User {
    @Column(nullable = false)
    private Integer role; // 0=관리자, 1=일반사용자
}
```

**role 값**:
- **0**: 관리자 (ROLE_ADMIN)
  - 관리자 페이지 접근 가능
  - 상품 관리, 주문 관리, 사용자 관리 등 모든 권한
  
- **1**: 일반 사용자 (ROLE_USER)
  - 쇼핑몰 기능만 사용 가능
  - 관리자 페이지 접근 불가

---

### 7.2 관리자 권한 부여 방법

**회원가입 시 (UserController.register)**:
```java
@PostMapping("/register")
public ResponseEntity<?> register(@RequestBody UserDTO userDTO) {
    // ★ 보안상 중요: 모든 회원가입은 role=1 (일반사용자)로 강제 설정
    userDTO.setRole(1);
    
    UserDTO registeredUser = userService.register(userDTO);
    return ResponseEntity.ok(registeredUser);
}
```

**관리자 권한 부여**:
- DB에서 직접 role을 0으로 변경해야 함
```sql
-- 예시: admin@example.com을 관리자로 승격
UPDATE user 
SET role = 0 
WHERE user_id = 'admin@example.com';
```

---

### 7.3 관리자 로그인 검증 (AdminLogin.js)

```javascript
const handleSubmit = async (e) => {
    // 1. 일반 로그인 API 호출 (동일한 엔드포인트)
    const response = await axios.post('/api/user/login', {
        userId: formData.username,
        password: formData.password
    });
    
    const { accessToken, refreshToken, user } = response.data;
    
    // 2. ★ 관리자 권한 검증 (핵심!)
    if (user.role !== 0) {
        // role이 1이면 (일반 사용자면) 로그인 차단
        setError('관리자 권한이 없습니다.');
        return;
    }
    
    // 3. role=0인 경우에만 진행
    localStorage.setItem('accessToken', accessToken);
    localStorage.setItem('refreshToken', refreshToken);
    localStorage.setItem('userInfo', JSON.stringify(user));
    
    // 4. 관리자 대시보드로 이동
    navigate('/admin/dashboard');
};
```

**검증 위치**:
- **프론트엔드**: AdminLogin.js에서 `user.role === 0` 체크
- **백엔드**: 별도의 role 검증 없음 (프론트엔드에서 처리)

---

## 8. 로그아웃 처리

### 8.1 로그아웃 흐름

```
[프론트엔드 - Header.js or MyPage.js]
사용자가 "로그아웃" 버튼 클릭
└─ handleLogout() 실행

1. Redux store 로그아웃 액션 dispatch
   - dispatch(logout())

[Redux - userSlice.js]
2. logout 리듀서 실행
   ├─ state.isAuthenticated = false
   ├─ state.user = null
   ├─ state.accessToken = null
   │
   └─ localStorage 정리
      ├─ localStorage.removeItem('accessToken')
      ├─ localStorage.removeItem('refreshToken')
      └─ localStorage.removeItem('userInfo')

[프론트엔드]
3. 로그인 페이지로 리다이렉트
   - navigate('/login')
   또는
   - window.location.href = '/login'
```

---

## 9. 전체 데이터 흐름도

### 9.1 로그인 → API 요청 → 로그아웃

```
┌─────────────────────────────────────────────────────────────────────┐
│                         1. 로그인                                    │
└─────────────────────────────────────────────────────────────────────┘

[프론트엔드]                    [백엔드]                    [DB]
Login.js                        UserController              MySQL
   │                                 │                         │
   ├─ POST /api/user/login ────────>│                         │
   │  {userId, password}             │                         │
   │                                 ├─ UserService.login()   │
   │                                 ├─────> SELECT user ─────>│
   │                                 │<────── User Entity ─────┤
   │                                 │                         │
   │                                 ├─ BCrypt 검증            │
   │                                 ├─ JWT 생성              │
   │                                 │  (Access + Refresh)     │
   │<─ {accessToken, refreshToken} ─┤                         │
   │   {user}                        │                         │
   │                                 │                         │
   ├─ localStorage.setItem()         │                         │
   ├─ dispatch(login())              │                         │
   └─ navigate('/')                  │                         │

┌─────────────────────────────────────────────────────────────────────┐
│                     2. API 요청 (인증 필요)                          │
└─────────────────────────────────────────────────────────────────────┘

[프론트엔드]                    [백엔드]
Cart.js                         JwtAuthenticationFilter     CartController
   │                                 │                         │
   ├─ axios.get('/api/cart') ──────>│                         │
   │  Header: Authorization          │                         │
   │  Bearer eyJhbGc...               │                         │
   │                                 │                         │
   │                                 ├─ 토큰 추출              │
   │                                 ├─ JWTUtil.validateToken()│
   │                                 ├─ 서명 검증 + 만료 확인   │
   │                                 ├─ 클레임 추출            │
   │                                 ├─ Security Context 저장  │
   │                                 │                         │
   │                                 └───────────────────────>│
   │                                                           ├─ userId로 장바구니 조회
   │<─ [{productId: 1, quantity: 2}] ─────────────────────────┤
   │                                                           │

┌─────────────────────────────────────────────────────────────────────┐
│                 3. Access Token 만료 → 자동 갱신                     │
└─────────────────────────────────────────────────────────────────────┘

[프론트엔드]                    [백엔드]
axiosInstance                   UserController
   │                                 │
   ├─ API 요청 (만료된 토큰) ──────>│
   │<─ 401 Unauthorized ─────────────┤
   │                                 │
   ├─ Interceptor 감지              │
   ├─ POST /api/user/refresh ──────>│
   │  Header: Bearer <RefreshToken>  │
   │                                 ├─ JWTUtil.validateToken()
   │                                 ├─ 새 Access Token 생성
   │<─ {accessToken: "new..."} ──────┤
   │                                 │
   ├─ localStorage.setItem()         │
   ├─ 원래 요청 재시도 (새 토큰) ──>│
   │<─ 성공 응답 ────────────────────┤

┌─────────────────────────────────────────────────────────────────────┐
│                         4. 로그아웃                                  │
└─────────────────────────────────────────────────────────────────────┘

[프론트엔드]
Header.js
   │
   ├─ handleLogout() 클릭
   ├─ dispatch(logout())
   │    └─ localStorage.removeItem('accessToken')
   │    └─ localStorage.removeItem('refreshToken')
   │    └─ localStorage.removeItem('userInfo')
   │    └─ state.isAuthenticated = false
   │    └─ state.user = null
   │
   └─ navigate('/login')
```

---

## 10. 보안 고려사항

### 10.1 주요 보안 메커니즘

1. **BCrypt 비밀번호 해시**: 일방향 암호화로 원본 비밀번호 복구 불가
2. **JWT 서명 검증**: HMAC SHA256으로 토큰 위조 방지
3. **이중 토큰 전략**: Access Token (짧은 수명) + Refresh Token (긴 수명)
4. **Role 기반 접근 제어**: 프론트엔드에서 관리자 권한 검증
5. **HTTPS 사용 (프로덕션)**: 토큰 전송 시 암호화

### 10.2 권장 개선사항

1. **HttpOnly Cookie**: localStorage 대신 HttpOnly Cookie 사용 (XSS 방어)
2. **백엔드 role 검증**: 프론트엔드뿐만 아니라 백엔드에서도 권한 검증
3. **토큰 만료 시간 단축**: Access Token 15~30분으로 단축
4. **로그인 시도 제한**: 무차별 대입 공격 방지
5. **CSRF 토큰**: Cookie 사용 시 CSRF 공격 방어

---

## 📌 요약

### 핵심 포인트

1. **JWT 구조**: Header + Payload(Claims) + Signature
   - Claims에 id, userId, role, marketingConsent 포함

2. **이중 토큰 전략**:
   - Access Token (60분): API 요청 인증용
   - Refresh Token (7일): Access Token 갱신용

3. **자동 토큰 갱신**:
   - axios interceptor가 401 에러 감지 시 자동으로 Refresh Token 사용

4. **관리자 권한 검증**:
   - role=0 (관리자), role=1 (일반사용자)
   - AdminLogin.js에서 `user.role === 0` 체크

5. **보안 계층**:
   - BCrypt 비밀번호 해시
   - JWT 서명 검증
   - HTTPS 암호화 (프로덕션)
   - Role 기반 접근 제어

### 주요 파일 역할

| 파일 | 역할 |
|------|------|
| **JWTUtil.java** | JWT 생성 및 검증 |
| **UserController.java** | 로그인, 토큰 갱신 API |
| **JwtAuthenticationFilter.java** | 모든 요청에서 JWT 자동 검증 |
| **Login.js** | 일반 사용자 로그인 |
| **AdminLogin.js** | 관리자 로그인 (role=0 검증) |
| **userSlice.js** | Redux 상태 관리 |
| **axiosInstance.js** | 자동 토큰 추가 및 갱신 |

---

**문서 작성일**: 2025년 11월 30일  
**프로젝트**: OnAndHome E-commerce Platform
