# 🔔 알림 기능 테스트 가이드

## ⚠️ 종 아이콘이 안 보이는 경우

### 1단계: 프론트엔드 재시작
```bash
# 프론트엔드 서버 중지 (Ctrl + C)
# 그리고 다시 시작
npm start
```

### 2단계: 브라우저 완전 새로고침
- **Windows**: `Ctrl + Shift + R`
- **Mac**: `Cmd + Shift + R`

### 3단계: 개발자 도구 콘솔 확인
1. `F12` 키를 눌러 개발자 도구 열기
2. Console 탭에서 `🔔 NotificationBell rendered, unreadCount: 0` 메시지 확인
3. 에러가 있다면 에러 메시지 확인

## 📝 데이터베이스 테이블 생성

### 1. MySQL 접속
```bash
mysql -u admin -p
use onandhome;
```

### 2. 테이블 생성
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

-- 인덱스 생성
CREATE INDEX idx_notification_user_id ON notifications(user_id);
CREATE INDEX idx_notification_is_read ON notifications(is_read);
CREATE INDEX idx_notification_created_at ON notifications(created_at DESC);
```

## 🧪 테스트 알림 생성

### 방법 1: 간단한 테스트 알림 1개 생성
```sql
-- 먼저 본인의 user_id 확인
SELECT user_id, id, username FROM user WHERE active = 1;

-- 테스트 알림 생성 (user_id를 본인의 것으로 변경)
INSERT INTO notifications (user_id, title, content, type, reference_id, is_read, created_at)
SELECT 
    id,
    '🎉 테스트 알림',
    '이것은 테스트 알림입니다. 종 아이콘에 빨간 숫자가 보이나요?',
    'SYSTEM',
    NULL,
    FALSE,
    NOW()
FROM user 
WHERE user_id = '여기에_본인의_user_id_입력'
LIMIT 1;
```

### 방법 2: 여러 개 테스트 알림 한번에 생성
```sql
-- test_notifications.sql 파일 사용
source C:/OnAndHomeBack/test_notifications.sql;
```

### 방법 3: 프론트엔드에서 확인
1. 로그인
2. 브라우저 개발자 도구 Console에서 실행:
```javascript
// Redux store 확인
console.log('Redux State:', window.__REDUX_DEVTOOLS_EXTENSION__.());

// 알림 개수 API 직접 호출
fetch('http://localhost:8080/api/notifications/unread-count', {
  headers: {
    'Authorization': 'Bearer ' + localStorage.getItem('accessToken')
  }
})
.then(r => r.json())
.then(data => console.log('Unread count:', data));
```

## 🔍 트러블슈팅

### 종 아이콘이 여전히 안 보이는 경우

#### 1. Redux Store 확인
```javascript
// 브라우저 콘솔에서 실행
console.log('Notification State:', 
  window.store.getState().notification
);
```

#### 2. 컴포넌트 렌더링 확인
- 개발자 도구 Console에서 `🔔 NotificationBell rendered` 메시지가 있는지 확인
- 없다면 컴포넌트가 렌더링되지 않은 것

#### 3. CSS 확인
```javascript
// 브라우저 콘솔에서 실행
document.querySelector('.notification-bell-wrapper')
// null이면 컴포넌트가 렌더링되지 않음
// 있으면 CSS 문제일 가능성
```

#### 4. 인증 상태 확인
```javascript
// 브라우저 콘솔에서 실행
console.log('Is Authenticated:', 
  window.store.getState().user.isAuthenticated
);
```

### 알림 개수가 0으로 표시되는 경우

#### 1. API 응답 확인
```bash
# 브라우저 개발자 도구 Network 탭에서 확인
# /api/notifications/unread-count 요청 찾기
```

#### 2. 데이터베이스 직접 확인
```sql
-- 본인의 알림 확인
SELECT n.*, u.user_id 
FROM notifications n
JOIN user u ON n.user_id = u.id
WHERE u.user_id = '본인의_user_id'
ORDER BY n.created_at DESC;

-- 읽지 않은 알림 개수
SELECT COUNT(*) 
FROM notifications n
JOIN user u ON n.user_id = u.id
WHERE u.user_id = '본인의_user_id' 
AND n.is_read = FALSE;
```

## ✅ 정상 작동 확인 체크리스트

- [ ] 로그인 후 헤더 우측에 🔔 종 아이콘이 보임
- [ ] 읽지 않은 알림이 있으면 빨간 숫자 뱃지가 표시됨
- [ ] 종 아이콘에 마우스를 올리면 살짝 커지는 애니메이션
- [ ] 종 아이콘 클릭 시 `/notifications` 페이지로 이동
- [ ] 알림 목록에서 최신 알림이 가장 위에 표시됨
- [ ] 읽지 않은 알림은 배경색이 다르고 좌측에 빨간 선이 있음
- [ ] 알림 클릭 시 해당 페이지로 이동하고 읽음 처리됨
- [ ] "모두 읽음" 버튼으로 전체 읽음 처리 가능
- [ ] 개별 알림 삭제 가능 (X 버튼)

## 🎨 종 아이콘 커스터마이징

종 모양이 마음에 들지 않는다면:

### 1. 다른 이모지 사용
```javascript
// NotificationBell.js
<span className="bell-icon">🔔</span>  // 현재
<span className="bell-icon">📬</span>  // 우편함
<span className="bell-icon">💌</span>  // 편지
<span className="bell-icon">🎁</span>  // 선물
```

### 2. SVG 아이콘 사용
```javascript
<svg className="bell-icon" viewBox="0 0 24 24" fill="currentColor">
  <path d="M12 22c1.1 0 2-.9 2-2h-4c0 1.1.9 2 2 2zm6-6v-5c0-3.07-1.63-5.64-4.5-6.32V4c0-.83-.67-1.5-1.5-1.5s-1.5.67-1.5 1.5v.68C7.64 5.36 6 7.92 6 11v5l-2 2v1h16v-1l-2-2zm-2 1H8v-6c0-2.48 1.51-4.5 4-4.5s4 2.02 4 4.5v6z"/>
</svg>
```

## 📞 도움이 필요하신가요?

위 방법으로도 해결되지 않는다면:
1. 브라우저 콘솔의 전체 에러 메시지
2. Network 탭의 API 응답
3. Redux DevTools의 state 스크린샷

이 정보들을 제공해주시면 더 정확한 해결책을 드릴 수 있습니다!
