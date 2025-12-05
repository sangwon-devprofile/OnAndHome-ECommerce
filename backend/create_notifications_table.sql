-- 알림 테이블 생성
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

-- 인덱스 생성 (성능 향상)
CREATE INDEX idx_notification_user_id ON notifications(user_id);
CREATE INDEX idx_notification_is_read ON notifications(is_read);
CREATE INDEX idx_notification_created_at ON notifications(created_at DESC);

-- 테스트용 알림 데이터 삽입 (userId가 'user1'인 사용자가 있다고 가정)
-- 먼저 user 테이블에서 실제 사용자의 id를 확인하고 아래 @user_id 변수를 사용합니다

-- 사용자 ID 조회 및 테스트 알림 생성 예시
-- 실제 사용 시에는 아래 쿼리의 'user1'을 실제 사용자 userId로 변경하세요

SET @user_id = (SELECT id FROM user WHERE user_id = 'user1' LIMIT 1);

-- @user_id가 있을 경우에만 테스트 알림 생성
INSERT INTO notifications (user_id, title, content, type, reference_id, is_read, created_at)
SELECT 
    @user_id,
    '주문 완료',
    '주문하신 상품이 정상적으로 처리되었습니다. 빠른 시일 내에 배송될 예정입니다.',
    'ORDER',
    1,
    FALSE,
    NOW() - INTERVAL 1 HOUR
WHERE @user_id IS NOT NULL;

INSERT INTO notifications (user_id, title, content, type, reference_id, is_read, created_at)
SELECT 
    @user_id,
    '리뷰 답글',
    '작성하신 리뷰에 판매자가 답글을 남겼습니다.',
    'REVIEW',
    10,
    FALSE,
    NOW() - INTERVAL 3 HOUR
WHERE @user_id IS NOT NULL;

INSERT INTO notifications (user_id, title, content, type, reference_id, is_read, created_at)
SELECT 
    @user_id,
    'Q&A 답변 등록',
    '문의하신 내용에 답변이 등록되었습니다.',
    'QNA',
    5,
    TRUE,
    NOW() - INTERVAL 1 DAY
WHERE @user_id IS NOT NULL;

INSERT INTO notifications (user_id, title, content, type, reference_id, is_read, created_at)
SELECT 
    @user_id,
    '새로운 공지사항',
    '중요한 공지사항이 등록되었습니다. 확인해주세요.',
    'NOTICE',
    3,
    FALSE,
    NOW() - INTERVAL 30 MINUTE
WHERE @user_id IS NOT NULL;

INSERT INTO notifications (user_id, title, content, type, reference_id, is_read, created_at)
SELECT 
    @user_id,
    '배송 시작',
    '주문하신 상품이 배송을 시작했습니다.',
    'ORDER',
    2,
    TRUE,
    NOW() - INTERVAL 2 DAY
WHERE @user_id IS NOT NULL;

-- 알림 테이블 확인
SELECT * FROM notifications ORDER BY created_at DESC;
