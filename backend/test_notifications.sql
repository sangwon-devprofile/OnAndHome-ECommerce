-- 빠른 테스트를 위한 알림 생성 스크립트

-- 1. 자신의 userId 확인
SELECT user_id, id, username FROM user WHERE active = 1;

-- 2. 위에서 확인한 user_id로 테스트 알림 생성
-- 예: 'user1' 대신 본인의 user_id를 입력하세요

-- 방법 1: 직접 user_id 입력
INSERT INTO notifications (user_id, title, content, type, reference_id, is_read, created_at)
SELECT 
    id,
    '🎉 테스트 알림',
    '이것은 테스트 알림입니다. 종 아이콘이 보이나요?',
    'SYSTEM',
    NULL,
    FALSE,
    NOW()
FROM user 
WHERE user_id = 'user1'  -- 여기에 본인의 user_id 입력
LIMIT 1;

-- 방법 2: 여러 개의 테스트 알림 한번에 생성
INSERT INTO notifications (user_id, title, content, type, reference_id, is_read, created_at)
SELECT 
    id,
    title,
    content,
    type,
    ref_id,
    is_read,
    created
FROM user,
(
    SELECT '🛒 주문 완료' as title, '주문이 정상적으로 완료되었습니다!' as content, 'ORDER' as type, 1 as ref_id, FALSE as is_read, NOW() - INTERVAL 10 MINUTE as created
    UNION ALL
    SELECT '💬 Q&A 답변', '문의하신 내용에 답변이 등록되었습니다.', 'QNA', 5, FALSE, NOW() - INTERVAL 1 HOUR
    UNION ALL
    SELECT '⭐ 리뷰 답글', '작성하신 리뷰에 답글이 등록되었습니다.', 'REVIEW', 10, FALSE, NOW() - INTERVAL 3 HOUR
    UNION ALL
    SELECT '📢 새 공지사항', '중요한 공지사항을 확인해주세요!', 'NOTICE', 3, FALSE, NOW() - INTERVAL 5 MINUTE
) as notifications
WHERE user.user_id = 'user1'  -- 여기에 본인의 user_id 입력
LIMIT 4;

-- 3. 생성된 알림 확인
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

-- 4. 읽지 않은 알림 개수 확인
SELECT 
    u.user_id,
    COUNT(*) as unread_count
FROM notifications n
JOIN user u ON n.user_id = u.id
WHERE n.is_read = FALSE
GROUP BY u.user_id;

-- 5. 모든 알림 삭제 (테스트 후 정리하고 싶을 때)
-- DELETE FROM notifications WHERE user_id = (SELECT id FROM user WHERE user_id = 'user1');
