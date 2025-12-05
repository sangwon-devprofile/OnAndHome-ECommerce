-- 알림 테이블이 존재하는지 확인
SHOW TABLES LIKE 'notifications';

-- 알림 테이블 구조 확인
DESCRIBE notifications;

-- 현재 있는 알림 확인
SELECT * FROM notifications;

-- 사용자 정보 확인 (본인의 userId 확인)
SELECT id, user_id, username, active FROM user WHERE active = 1;

-- 테스트 알림 생성 (본인의 user_id를 'user1'로 가정)
-- 아래에서 'user1'을 본인의 실제 user_id로 변경하세요
INSERT INTO notifications (user_id, title, content, type, is_read, created_at)
SELECT 
    u.id,
    '🎉 테스트 알림',
    '알림 시스템이 정상 작동하는지 테스트입니다.',
    'SYSTEM',
    FALSE,
    NOW()
FROM user u
WHERE u.user_id = 'user1'  -- 여기를 본인의 user_id로 변경
LIMIT 1;

-- 알림이 생성되었는지 확인
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
LIMIT 10;
