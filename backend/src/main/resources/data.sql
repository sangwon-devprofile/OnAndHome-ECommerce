-- 회사 정보 테이블 초기 데이터 삽입
-- 이미 데이터가 있으면 무시 (중복 방지)

INSERT INTO company_info (id, company_name, ceo, fax, email, address, business_number, mail_order_number, privacy_officer, phone, created_at, updated_at)
SELECT 1, '(주)하이미디어', '이상혁', '02-1544-7778', 'faker@naver.com', '서울 서초구 서초동 123-456', '123-456789', '카456-7894', '최우제', '1544-7777', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM company_info WHERE id = 1);
