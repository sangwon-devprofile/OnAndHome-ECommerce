# OnAndHome 상품 이미지 표시 문제 해결 가이드

## 문제 원인
- DB에 이미지 파일명만 저장됨 (예: `tv_01`)
- 실제 경로가 없음 (예: `/product_img/tv_01.jpg` 필요)
- 실제 이미지 파일도 없음

## 해결 순서

### 1단계: 이미지 파일 생성
실제 상품 이미지가 있다면 `C:\OnAndHome\src\main\resources\static\product_img\` 폴더에 복사하세요.

**실제 이미지가 없는 경우**, 샘플 이미지로 테스트:
```bash
# Windows에서 실행
C:\OnAndHome\create_sample_images.bat
```

이 스크립트는 다음 이미지 파일들을 자동 생성합니다:
- tv_01.jpg ~ tv_06.jpg (TV 썸네일)
- tv_01_d.jpg ~ tv_06_d.jpg (TV 상세이미지)
- kit_01.jpg ~ kit_18.jpg (주방가전 썸네일)
- kit_01_d.jpg ~ kit_18_d.jpg (주방가전 상세이미지)
- life_01.jpg ~ life_12.jpg (생활가전 썸네일)
- life_01_d.jpg ~ life_12_d.jpg (생활가전 상세이미지)
- air_01.jpg ~ air_12.jpg (에어컨/공청기 썸네일)
- air_01_d.jpg ~ air_12_d.jpg (에어컨/공청기 상세이미지)
- au_01.jpg ~ au_06.jpg (오디오 썸네일)
- au_01_d.jpg ~ au_06_d.jpg (오디오 상세이미지)
- etc_01.jpg ~ etc_18.jpg (기타 썸네일)
- etc_01_d.jpg ~ etc_18_d.jpg (기타 상세이미지)

총 144개의 이미지 파일이 생성됩니다.

### 2단계: DB 업데이트
MySQL에 접속해서 SQL 쿼리 실행:

```sql
-- 1. 현재 상태 확인
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

-- 4. 결과 확인
SELECT id, name, thumbnail_image, detail_image 
FROM product 
LIMIT 10;
```

**또는** `C:\OnAndHome\update_image_paths.sql` 파일을 MySQL Workbench에서 실행하세요.

### 3단계: 애플리케이션 재시작
IntelliJ에서 Spring Boot 애플리케이션을 재시작합니다.

### 4단계: 확인
브라우저에서 다음을 확인:
1. 캐시 완전 삭제 (Ctrl+Shift+Delete)
2. 페이지 새로고침 (Ctrl+Shift+R)
3. 개발자 도구 Console에서 에러 확인

## 예상 결과

### Before (현재)
```
DB: thumbnail_image = 'tv_01'
파일: 없음
결과: 이미지 안 보임 ❌
```

### After (수정 후)
```
DB: thumbnail_image = '/product_img/tv_01.jpg'
파일: C:\OnAndHome\src\main\resources\static\product_img\tv_01.jpg
결과: 이미지 표시됨 ✅
```

## 롤백 (문제 발생 시)
```sql
UPDATE product 
SET thumbnail_image = REPLACE(REPLACE(thumbnail_image, '/product_img/', ''), '.jpg', '')
WHERE thumbnail_image LIKE '/product_img/%';

UPDATE product 
SET detail_image = REPLACE(REPLACE(detail_image, '/product_img/', ''), '.jpg', '')
WHERE detail_image LIKE '/product_img/%';
```

## 참고사항
- 실제 상품 이미지를 사용하려면 1단계에서 실제 파일을 복사하세요
- 샘플 이미지는 모두 동일하게 보입니다 (테스트용)
- 이미지 경로는 `/product_img/` 또는 `/uploads/` 두 가지 방식을 지원합니다
