# 🔍 알림 시스템 디버깅 가이드

## 1단계: 브라우저에서 직접 확인

### 개발자 도구 열기
1. `F12` 키를 누르거나 우클릭 > 검사
2. **Console** 탭으로 이동

### API 직접 호출 테스트
```javascript
// 1. 인증 토큰 확인
console.log('Access Token:', localStorage.getItem('accessToken'));

// 2. 알림 개수 API 호출
fetch('http://localhost:8080/api/notifications/unread-count', {
  method: 'GET',
  headers: {
    'Authorization': 'Bearer ' + localStorage.getItem('accessToken'),
    'Content-Type': 'application/json'
  }
})
.then(response => response.json())
.then(data => {
  console.log('📊 읽지 않은 알림 개수:', data);
})
.catch(error => console.error('❌ 에러:', error));

// 3. 전체 알림 목록 API 호출
fetch('http://localhost:8080/api/notifications', {
  method: 'GET',
  headers: {
    'Authorization': 'Bearer ' + localStorage.getItem('accessToken'),
    'Content-Type': 'application/json'
  }
})
.then(response => response.json())
.then(data => {
  console.log('📋 알림 목록:', data);
  if (data.success) {
    console.log('✅ 알림 개수:', data.notifications.length);
    console.log('📝 알림 내용:', data.notifications);
  }
})
.catch(error => console.error('❌ 에러:', error));

// 4. Redux Store 확인
console.log('🗄️ Redux State:', window.store?.getState());
console.log('🔔 Notification State:', window.store?.getState()?.notification);

// 5. 사용자 정보 확인
console.log('👤 User State:', window.store?.getState()?.user);
```

---

## 2단계: MySQL에서 데이터 확인

```sql
-- 1. notifications 테이블이 존재하는지 확인
SHOW TABLES LIKE 'notifications';

-- 2. 테이블 구조 확인
DESCRIBE notifications;

-- 3. 본인의 user_id 확인
SELECT user_id, id, username FROM user WHERE active = 1;

-- 4. 본인의 알림 확인 (user_id를 본인 것으로 변경)
SELECT 
    n.*,
    u.user_id
FROM notifications n
JOIN user u ON n.user_id = u.id
WHERE u.user_id = 'user1'  -- 여기를 본인의 user_id로 변경
ORDER BY n.created_at DESC;

-- 5. 전체 알림 확인
SELECT 
    n.id,
    u.user_id,
    n.title,
    n.content,
    n.type,
    n.is_read,
    n.created_at
FROM notifications n
JOIN user u ON n.user_id = u.id
ORDER BY n.created_at DESC
LIMIT 20;

-- 6. 알림이 없다면 테스트 알림 생성
INSERT INTO notifications (user_id, title, content, type, is_read, created_at)
SELECT 
    id,
    '🎉 테스트 알림',
    '이것은 테스트 알림입니다!',
    'SYSTEM',
    FALSE,
    NOW()
FROM user 
WHERE user_id = 'user1'  -- 본인의 user_id로 변경
LIMIT 1;

-- 7. 여러 개의 테스트 알림 생성
INSERT INTO notifications (user_id, title, content, type, reference_id, is_read, created_at)
SELECT 
    u.id,
    t.title,
    t.content,
    t.type,
    t.ref_id,
    FALSE,
    t.created
FROM user u
CROSS JOIN (
    SELECT '📦 주문 완료' as title, '주문이 정상적으로 완료되었습니다!' as content, 'ORDER' as type, 1 as ref_id, NOW() - INTERVAL 10 MINUTE as created
    UNION ALL
    SELECT '⭐ 리뷰 답글', '작성하신 리뷰에 답글이 등록되었습니다.', 'REVIEW', 10, NOW() - INTERVAL 1 HOUR
    UNION ALL
    SELECT '❓ Q&A 답변', '문의하신 내용에 답변이 등록되었습니다.', 'QNA', 5, NOW() - INTERVAL 3 HOUR
    UNION ALL
    SELECT '📢 새 공지사항', '중요한 공지사항을 확인해주세요!', 'NOTICE', 3, NOW() - INTERVAL 5 MINUTE
) as t
WHERE u.user_id = 'user1'  -- 본인의 user_id로 변경
LIMIT 4;
```

---

## 3단계: 백엔드 로그 확인

백엔드 콘솔에서 다음 로그를 확인하세요:

```
알림 조회 성공: userId=user1
알림 개수: 5
```

에러가 있다면:
```
알림 조회 실패: [에러 메시지]
```

---

## 4단계: Network 탭에서 API 호출 확인

1. **개발자 도구** > **Network** 탭 열기
2. 알림 페이지 새로고침 (`Ctrl + R`)
3. `notifications` 검색
4. 응답 확인:

### 정상 응답 예시:
```json
{
  "success": true,
  "notifications": [
    {
      "id": 1,
      "title": "🎉 테스트 알림",
      "content": "이것은 테스트 알림입니다!",
      "type": "SYSTEM",
      "isRead": false,
      "createdAt": "2024-01-15T10:30:00"
    }
  ]
}
```

### 에러 응답 예시:
```json
{
  "success": false,
  "message": "사용자를 찾을 수 없습니다."
}
```

---

## 5단계: 자주 발생하는 문제와 해결방법

### 문제 1: "notifications 테이블이 존재하지 않습니다"
**해결**: 데이터베이스에 테이블 생성
```sql
CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    content VARCHAR(1000) NOT NULL,
    type VARCHAR(50) NOT NULL,
    reference_id BIGINT,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL,
    read_at DATETIME,
    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
);
```

### 문제 2: "유효하지 않은 토큰입니다"
**해결**: 다시 로그인
```javascript
// 브라우저 콘솔에서 실행
localStorage.removeItem('accessToken');
localStorage.removeItem('refreshToken');
localStorage.removeItem('userInfo');
// 그리고 다시 로그인
```

### 문제 3: "사용자를 찾을 수 없습니다"
**해결**: userId가 올바른지 확인
```sql
-- 토큰의 userId와 데이터베이스의 user_id가 일치하는지 확인
SELECT * FROM user WHERE user_id = '토큰에서_추출한_userId';
```

### 문제 4: 알림은 있는데 화면에 안 보임
**해결**: Redux state 확인
```javascript
// 브라우저 콘솔에서 실행
console.log(window.store.getState().notification);
// notifications 배열이 비어있다면 API 호출 문제
// notifications 배열에 데이터가 있다면 렌더링 문제
```

### 문제 5: CORS 에러
**해결**: 백엔드 CORS 설정 확인
```java
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
```

---

## 6단계: 종합 체크리스트

- [ ] MySQL에 notifications 테이블이 존재하는가?
- [ ] 테이블에 본인의 알림 데이터가 있는가?
- [ ] accessToken이 localStorage에 저장되어 있는가?
- [ ] API 호출이 200 OK를 반환하는가?
- [ ] API 응답에 success: true가 있는가?
- [ ] API 응답에 notifications 배열이 있는가?
- [ ] Redux store에 알림 데이터가 저장되는가?
- [ ] 브라우저 콘솔에 에러가 없는가?
- [ ] 백엔드 서버가 실행 중인가?
- [ ] 프론트엔드 서버가 실행 중인가?

---

## 빠른 수정 방법

### 방법 1: 캐시 완전 삭제
```javascript
// 브라우저 콘솔에서 실행
localStorage.clear();
sessionStorage.clear();
// 그리고 페이지 새로고침 (Ctrl + Shift + R)
```

### 방법 2: 서버 완전 재시작
```bash
# 백엔드
Ctrl + C (서버 중지)
./gradlew clean bootRun

# 프론트엔드
Ctrl + C (서버 중지)
npm start
```

### 방법 3: 테스트 알림 강제 생성
위의 SQL로 테스트 알림을 생성한 후, 
알림 페이지를 새로고침하세요.

---

## 결과 확인

모든 설정이 정상이라면:
1. 브라우저 콘솔에서 "🔔 알림 로딩 시작..." 메시지 확인
2. "📦 API 응답: {success: true, notifications: [...]}" 확인
3. "✅ 알림 개수: X" 확인
4. 화면에 알림 카드가 표시됨

문제가 계속된다면 위의 모든 로그를 캡처해서 제공해주세요!
