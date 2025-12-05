-- ✅ orders 테이블에 hidden 컬럼 추가
-- 사용자가 주문 내역을 숨길 수 있는 기능

ALTER TABLE orders 
ADD COLUMN hidden BOOLEAN NOT NULL DEFAULT FALSE COMMENT '사용자가 숨긴 주문';

-- 인덱스 추가 (성능 향상)
CREATE INDEX idx_orders_hidden ON orders(hidden);
CREATE INDEX idx_orders_user_hidden ON orders(user_id, hidden);

-- 확인
DESC orders;

-- 기존 주문 모두 hidden = false로 설정 (이미 DEFAULT FALSE지만 확인차)
UPDATE orders SET hidden = FALSE WHERE hidden IS NULL;
