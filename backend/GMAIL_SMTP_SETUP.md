# Gmail SMTP 설정 가이드

## Gmail 앱 비밀번호 생성 방법

### 1. Google 계정 2단계 인증 활성화
1. https://myaccount.google.com/security 접속
2. "2단계 인증" 클릭
3. 화면 지시에 따라 2단계 인증 설정

### 2. 앱 비밀번호 생성
1. https://myaccount.google.com/apppasswords 접속
2. "앱 선택" → "메일" 선택
3. "기기 선택" → "Windows 컴퓨터" 선택
4. "생성" 클릭
5. 16자리 앱 비밀번호 복사 (공백 제거)

### 3. application.properties 설정
```properties
# Gmail SMTP 설정
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com        # 본인의 Gmail 주소
spring.mail.password=abcd efgh ijkl mnop        # 생성한 16자리 앱 비밀번호 (공백 포함 가능)
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true
spring.mail.properties.mail.smtp.connectiontimeout=5000
spring.mail.properties.mail.smtp.timeout=5000
spring.mail.properties.mail.smtp.writetimeout=5000

# 이메일 인증 코드 유효시간 (5분)
email.verification.expiration=300000
```

## 이메일 인증 기능 사용 방법

### 프론트엔드 흐름
1. **회원가입 페이지 접속** (`/signup`)
2. **Step 1: 이메일 인증**
   - 이메일 주소 입력
   - "인증" 버튼 클릭 → 6자리 인증 코드 이메일 수신
   - 인증 코드 입력 후 "확인" 버튼 클릭
   - 5분 타이머 (시간 초과 시 재전송 필요)
3. **Step 2: 회원정보 입력**
   - 아이디, 비밀번호, 이름, 휴대폰 입력
   - "회원가입" 버튼 클릭

### 백엔드 API 엔드포인트
1. **인증 코드 전송**: `POST /api/email/send-code`
   ```json
   {
     "email": "user@example.com"
   }
   ```

2. **인증 코드 확인**: `POST /api/email/verify-code`
   ```json
   {
     "email": "user@example.com",
     "code": "123456"
   }
   ```

3. **인증 상태 확인**: `GET /api/email/check-verification?email=user@example.com`

## 트러블슈팅

### 이메일이 전송되지 않는 경우
1. Gmail 앱 비밀번호가 올바른지 확인
2. 2단계 인증이 활성화되어 있는지 확인
3. 백엔드 콘솔에서 오류 로그 확인
4. 방화벽에서 포트 587 허용 확인

### 이메일이 스팸함으로 가는 경우
- Gmail 설정에서 해당 이메일 주소를 신뢰할 수 있는 발신자로 등록

### 인증 코드가 만료되는 경우
- 5분 이내에 인증 완료
- 재전송 버튼으로 새 코드 발급

## 주의사항
- **앱 비밀번호는 절대 공개하지 마세요**
- Git에 커밋하기 전에 application.properties 파일에서 실제 이메일과 비밀번호를 제거하세요
- 프로덕션 환경에서는 환경 변수나 암호화된 설정 파일을 사용하세요
