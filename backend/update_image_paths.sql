-- =============================================
-- OnAndHome 상품 이미지 경로 업데이트 쿼리
-- =============================================
-- 
-- 목적: thumbnail_image와 detail_image 컬럼을 
--      파일명(tv_01)에서 전체 경로(/product_img/tv_01.jpg)로 업데이트
--
-- 실행 전 주의사항:
-- 1. 백업을 먼저 수행하세요
-- 2. 테스트 환경에서 먼저 실행하세요
-- =============================================

-- 1. 현재 상태 확인 (실행 전)
SELECT id, name, thumbnail_image, detail_image 
FROM product 
LIMIT 10;

-- 2. thumbnail_image 업데이트
UPDATE product 
SET thumbnail_image = CONCAT('/product_img/', thumbnail_image, '.jpg')
WHERE thumbnail_image IS NOT NULL 
  AND thumbnail_image != ''
  AND thumbnail_image NOT LIKE '/product_img/%'
  AND thumbnail_image NOT LIKE '/uploads/%';

-- 3. detail_image 업데이트
UPDATE product 
SET detail_image = CONCAT('/product_img/', detail_image, '.jpg')
WHERE detail_image IS NOT NULL 
  AND detail_image != ''
  AND detail_image NOT LIKE '/product_img/%'
  AND detail_image NOT LIKE '/uploads/%';

-- 4. 업데이트 결과 확인
SELECT id, name, thumbnail_image, detail_image 
FROM product 
LIMIT 10;

-- 5. 모든 상품의 이미지 경로 확인
SELECT 
    id,
    name,
    thumbnail_image,
    detail_image,
    CASE 
        WHEN thumbnail_image LIKE '/product_img/%' THEN '✓ 정상'
        WHEN thumbnail_image LIKE '/uploads/%' THEN '✓ 정상'
        WHEN thumbnail_image IS NULL THEN '⚠ NULL'
        ELSE '✗ 비정상'
    END as thumbnail_status,
    CASE 
        WHEN detail_image LIKE '/product_img/%' THEN '✓ 정상'
        WHEN detail_image LIKE '/uploads/%' THEN '✓ 정상'
        WHEN detail_image IS NULL THEN '⚠ NULL'
        ELSE '✗ 비정상'
    END as detail_status
FROM product
ORDER BY id;

-- =============================================
-- 롤백이 필요한 경우 (원래 상태로 되돌리기)
-- =============================================
/*
UPDATE product 
SET thumbnail_image = REPLACE(REPLACE(thumbnail_image, '/product_img/', ''), '.jpg', '')
WHERE thumbnail_image LIKE '/product_img/%';

UPDATE product 
SET detail_image = REPLACE(REPLACE(detail_image, '/product_img/', ''), '.jpg', '')
WHERE detail_image LIKE '/product_img/%';
*/
