-- orders 테이블의 status ENUM에 PAYMENT_PENDING 추가
-- 무통장 입금 결제 대기 상태를 처리하기 위한 스키마 수정

ALTER TABLE orders 
MODIFY COLUMN status ENUM(
    'PAYMENT_PENDING',  -- 결제 대기 (무통장 입금)
    'ORDERED',          -- 주문 완료
    'DELIVERING',       -- 배송 중
    'DELIVERED',        -- 배송 완료
    'CANCELED'          -- 취소됨
) NOT NULL;

-- 확인
SHOW COLUMNS FROM orders WHERE Field = 'status';
