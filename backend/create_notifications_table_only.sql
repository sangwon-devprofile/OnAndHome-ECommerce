-- notifications 테이블 생성 (테스트 데이터 제외)

-- 기존 테이블이 있다면 삭제 (선택사항)
-- DROP TABLE IF EXISTS notifications;

-- notifications 테이블 생성
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

-- 테이블 생성 확인
SELECT 'notifications 테이블 생성 완료!' as status;
DESCRIBE notifications;
