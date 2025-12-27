-- =============================================
-- OnAndHome 카테고리별 외부 이미지 URL 업데이트 쿼리
-- =============================================
-- 
-- 목적: 상품 카테고리에 맞는 고화질 외부 이미지(Unsplash) 링크로 업데이트
--
-- =============================================

-- 1. TV
UPDATE product SET thumbnail_image = 'https://images.unsplash.com/photo-1593359677879-a4bb92f829d1?q=80&w=800', detail_image = 'https://images.unsplash.com/photo-1593359677879-a4bb92f829d1?q=80&w=800' WHERE category = 'TV';

-- 2. 오디오
UPDATE product SET thumbnail_image = 'https://images.unsplash.com/photo-1545454675-3531b543be5d?q=80&w=800', detail_image = 'https://images.unsplash.com/photo-1545454675-3531b543be5d?q=80&w=800' WHERE category = '오디오';

-- 3. 냉장고
UPDATE product SET thumbnail_image = 'https://images.unsplash.com/photo-1584622650111-993a426fbf0a?q=80&w=800', detail_image = 'https://images.unsplash.com/photo-1584622650111-993a426fbf0a?q=80&w=800' WHERE category = '냉장고';

-- 4. 전자레인지
UPDATE product SET thumbnail_image = 'https://images.unsplash.com/photo-1585659823160-359b6dca5238?q=80&w=800', detail_image = 'https://images.unsplash.com/photo-1585659823160-359b6dca5238?q=80&w=800' WHERE category = '전자레인지';

-- 5. 식기세척기
UPDATE product SET thumbnail_image = 'https://images.unsplash.com/photo-1584622781564-1d9876a13d00?q=80&w=800', detail_image = 'https://images.unsplash.com/photo-1584622781564-1d9876a13d00?q=80&w=800' WHERE category = '식기세척기';

-- 6. 세탁기
UPDATE product SET thumbnail_image = 'https://images.unsplash.com/photo-1626806819282-2c1dc01a5e0c?q=80&w=800', detail_image = 'https://images.unsplash.com/photo-1626806819282-2c1dc01a5e0c?q=80&w=800' WHERE category = '세탁기';

-- 7. 청소기
UPDATE product SET thumbnail_image = 'https://images.unsplash.com/photo-1558317374-067fb5f30001?q=80&w=800', detail_image = 'https://images.unsplash.com/photo-1558317374-067fb5f30001?q=80&w=800' WHERE category = '청소기';

-- 8. 에어컨
UPDATE product SET thumbnail_image = 'https://images.unsplash.com/photo-1631049307264-da0ec9d70304?q=80&w=800', detail_image = 'https://images.unsplash.com/photo-1631049307264-da0ec9d70304?q=80&w=800' WHERE category = '에어컨';

-- 9. 공기청정기
UPDATE product SET thumbnail_image = 'https://images.unsplash.com/photo-1626541571597-9005938d9f48?q=80&w=800', detail_image = 'https://images.unsplash.com/photo-1626541571597-9005938d9f48?q=80&w=800' WHERE category = '공기청정기';

-- 10. 정수기
UPDATE product SET thumbnail_image = 'https://images.unsplash.com/photo-1589139266091-64906f2f9f10?q=80&w=800', detail_image = 'https://images.unsplash.com/photo-1589139266091-64906f2f9f10?q=80&w=800' WHERE category = '정수기';

-- 11. 안마의자
UPDATE product SET thumbnail_image = 'https://images.unsplash.com/photo-1598300042247-d088f8ab3a91?q=80&w=800', detail_image = 'https://images.unsplash.com/photo-1598300042247-d088f8ab3a91?q=80&w=800' WHERE category = '안마의자';

-- 12. PC
UPDATE product SET thumbnail_image = 'https://images.unsplash.com/photo-1547082299-de196ea013d6?q=80&w=800', detail_image = 'https://images.unsplash.com/photo-1547082299-de196ea013d6?q=80&w=800' WHERE category = 'PC';
