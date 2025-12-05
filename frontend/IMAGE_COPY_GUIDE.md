# 🎨 이미지 복사 및 프로젝트 실행 가이드

## 📋 준비 사항

React 프로젝트에서 이미지를 사용하려면 먼저 OnAndHome 프로젝트의 이미지들을 복사해야 합니다.

---

## 🚀 방법 1: 자동 복사 (가장 쉬운 방법)

### 터미널에서 실행:

```bash
cd C:\onandhomefront
npm run copy-images
```

이 명령어가 자동으로 모든 이미지를 복사합니다.

---

## 💻 방법 2: Windows 명령 프롬프트 사용

**명령 프롬프트(CMD)를 관리자 권한으로 열고** 다음 명령을 실행:

```cmd
robocopy "C:\OnAndHome\src\main\resources\static\images" "C:\onandhomefront\public\images" /E
robocopy "C:\OnAndHome\src\main\resources\static\product_img" "C:\onandhomefront\public\product_img" /E
```

---

## 🖱️ 방법 3: 수동 복사 (Windows 탐색기)

### 이미지 폴더 복사:
1. `C:\OnAndHome\src\main\resources\static\images` 폴더를 엽니다
2. **Ctrl+A**를 눌러 모든 파일을 선택합니다
3. **Ctrl+C**를 눌러 복사합니다
4. `C:\onandhomefront\public\images` 폴더로 이동합니다
5. **Ctrl+V**를 눌러 붙여넣습니다

### 제품 이미지 폴더 복사:
1. `C:\OnAndHome\src\main\resources\static\product_img` 폴더를 엽니다
2. **Ctrl+A**를 눌러 모든 파일을 선택합니다
3. **Ctrl+C**를 눌러 복사합니다
4. `C:\onandhomefront\public\product_img` 폴더로 이동합니다
5. **Ctrl+V**를 눌러 붙여넣습니다

---

## ✅ 복사 완료 확인

복사가 완료되면 다음 파일들이 있는지 확인하세요:

### 📁 C:\onandhomefront\public\images\ 
- `logo.png` - 사이트 로고
- `fb.png`, `blg.png`, `ins.png`, `ut.png` - SNS 아이콘들
- `mypage.png`, `cart.png` - 헤더 아이콘들
- 기타 UI 아이콘들

### 📁 C:\onandhomefront\public\product_img\
- `slide_01.jpg` ~ `slide_04.jpg` - 메인 슬라이드 이미지
- `item_01_01.jpg` ~ `item_01_04.jpg` - HOT 아이템 섹션 이미지
- `item_04_01.jpg` ~ `item_04_04.jpg` - 하단 배너 이미지
- `top_left.jpg` - 헤더 프로필 이미지
- 기타 상품 썸네일 이미지들

---

## 🎯 React 앱 실행

이미지 복사가 완료되면 React 앱을 실행하세요:

```bash
cd C:\onandhomefront
npm start
```

브라우저가 자동으로 열리고 `http://localhost:3000`에서 앱이 실행됩니다.

---

## 🔧 문제 해결

### ❌ 이미지가 표시되지 않는 경우:

1. **브라우저 캐시 삭제**
   - Chrome: `Ctrl + Shift + Delete`
   - 캐시된 이미지 및 파일 삭제

2. **React 앱 재시작**
   ```bash
   # 터미널에서 Ctrl+C를 눌러 앱 중지
   npm start
   ```

3. **이미지 경로 확인**
   - `C:\onandhomefront\public\images\logo.png` 파일이 존재하는지 확인
   - `C:\onandhomefront\public\product_img\slide_01.jpg` 파일이 존재하는지 확인

### ❌ CSS 에러가 발생하는 경우:

```
Module not found: Error: Can't resolve '/images/logo.png'
```

이 에러는 이미지 파일이 없어서 발생합니다. 위의 복사 방법을 다시 확인하세요.

---

## 📝 참고 사항

- 이미지 파일은 **반드시 `public` 폴더**에 있어야 합니다
- `src` 폴더가 아닌 `public` 폴더에 저장해야 런타임에 접근 가능합니다
- 모든 이미지 경로는 `/images/파일명` 또는 `/product_img/파일명` 형식으로 사용됩니다

---

## 🎉 완료!

이미지가 정상적으로 복사되고 React 앱이 실행되면:
- ✅ 메인 페이지의 슬라이드 배너가 표시됩니다
- ✅ HOT 아이템 섹션의 이미지들이 표시됩니다
- ✅ 헤더의 로고와 아이콘들이 표시됩니다
- ✅ 푸터의 추천 상품 이미지들이 표시됩니다

문제가 계속되면 위의 **문제 해결** 섹션을 참고하세요! 🚀
