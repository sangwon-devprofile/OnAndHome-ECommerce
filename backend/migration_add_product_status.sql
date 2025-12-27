-- =============================================
-- 상품 테이블 status 컬럼 추가 및 초기화
-- =============================================

-- 1. status 컬럼 추가 (기존 데이터가 있을 경우 기본값 '판매중' 설정)
ALTER TABLE product ADD COLUMN status VARCHAR(20) DEFAULT '판매중';

-- 2. 기존 데이터의 status를 '판매중'으로 업데이트 (안전장치)
UPDATE product SET status = '판매중' WHERE status IS NULL;

-- 3. (옵션) 재고가 0인 상품은 자동으로 '품절' 처리하고 싶다면 실행
-- UPDATE product SET status = '품절' WHERE stock = 0;

