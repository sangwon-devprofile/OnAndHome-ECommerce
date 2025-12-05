# 알림 기능 통합 가이드

## 📌 이미 구현된 기능

✅ 백엔드 알림 시스템 (NotificationService, NotificationRestController)
✅ 프론트엔드 알림 UI (헤더 종 아이콘, 알림 페이지)
✅ Redux 상태 관리 (notificationSlice)
✅ 실시간 알림 개수 업데이트 (30초마다)

## 🔧 다양한 이벤트에서 알림 생성하기

알림은 `NotificationService.createNotification()` 메서드를 사용하여 생성합니다.

### 1. OrderService에서 주문 완료 시 알림 생성

```java
// OrderService.java에 NotificationService 의존성 추가
private final NotificationService notificationService;

// 주문 생성 메서드 끝 부분에 추가
public Order createOrder(CreateOrderRequest request, String userId) {
    // ... 기존 주문 생성 로직 ...
    
    // 주문 완료 알림 생성
    try {
        notificationService.createNotification(
            userId,
            "주문 완료",
            "주문이 정상적으로 완료되었습니다. 주문번호: " + savedOrder.getId(),
            "ORDER",
            savedOrder.getId()
        );
    } catch (Exception e) {
        log.error("주문 완료 알림 생성 실패", e);
    }
    
    return savedOrder;
}

// 주문 상태 변경 시 알림
public void updateOrderStatus(Long orderId, String newStatus, String userId) {
    // ... 상태 변경 로직 ...
    
    String message = "";
    switch (newStatus) {
        case "PAYMENT_COMPLETE":
            message = "결제가 완료되었습니다.";
            break;
        case "PREPARING":
            message = "상품 준비 중입니다.";
            break;
        case "SHIPPED":
            message = "배송이 시작되었습니다.";
            break;
        case "DELIVERED":
            message = "배송이 완료되었습니다.";
            break;
    }
    
    if (!message.isEmpty()) {
        notificationService.createNotification(
            userId,
            "주문 상태 변경",
            message,
            "ORDER",
            orderId
        );
    }
}
```

### 2. QnaReplyService에서 Q&A 답변 등록 시 알림 생성

```java
// QnaReplyService.java
private final NotificationService notificationService;
private final QnaRepository qnaRepository;

public QnaReplyDTO createReply(Long qnaId, QnaReplyDTO dto) {
    // ... 기존 답변 생성 로직 ...
    
    // Q&A 작성자에게 알림 전송
    Qna qna = qnaRepository.findById(qnaId)
        .orElseThrow(() -> new RuntimeException("Q&A를 찾을 수 없습니다."));
    
    try {
        notificationService.createNotification(
            qna.getUserId(),  // Q&A 작성자
            "Q&A 답변 등록",
            "문의하신 내용에 답변이 등록되었습니다.",
            "QNA",
            qnaId
        );
    } catch (Exception e) {
        log.error("Q&A 답변 알림 생성 실패", e);
    }
    
    return savedReply;
}
```

### 3. ReviewReplyService에서 리뷰 답글 등록 시 알림 생성

```java
// ReviewReplyService.java
private final NotificationService notificationService;
private final ReviewRepository reviewRepository;

public ReviewReplyDTO createReply(Long reviewId, ReviewReplyDTO dto) {
    // ... 기존 답글 생성 로직 ...
    
    // 리뷰 작성자에게 알림 전송
    Review review = reviewRepository.findById(reviewId)
        .orElseThrow(() -> new RuntimeException("리뷰를 찾을 수 없습니다."));
    
    try {
        notificationService.createNotification(
            review.getUserId(),  // 리뷰 작성자
            "리뷰 답글 등록",
            "작성하신 리뷰에 답글이 등록되었습니다.",
            "REVIEW",
            reviewId
        );
    } catch (Exception e) {
        log.error("리뷰 답글 알림 생성 실패", e);
    }
    
    return savedReply;
}
```

### 4. NoticeService에서 새 공지사항 등록 시 전체 사용자에게 알림

```java
// NoticeService.java
private final NotificationService notificationService;
private final UserRepository userRepository;

public NoticeDto createNotice(NoticeDto dto) {
    // ... 기존 공지사항 생성 로직 ...
    
    // 모든 활성 사용자에게 알림 전송 (비동기 처리 권장)
    try {
        List<User> activeUsers = userRepository.findAll().stream()
            .filter(User::getActive)
            .collect(Collectors.toList());
        
        for (User user : activeUsers) {
            notificationService.createNotification(
                user.getUserId(),
                "새 공지사항",
                notice.getTitle(),
                "NOTICE",
                notice.getId()
            );
        }
    } catch (Exception e) {
        log.error("공지사항 알림 생성 실패", e);
    }
    
    return savedNotice;
}
```

## 🎯 알림 타입 정의

현재 지원하는 알림 타입:
- `ORDER` - 주문 관련
- `REVIEW` - 리뷰 관련
- `QNA` - Q&A 관련
- `NOTICE` - 공지사항
- `SYSTEM` - 시스템 알림

필요에 따라 새로운 타입을 추가할 수 있습니다.

## 📱 프론트엔드에서 알림 처리

알림을 클릭하면 자동으로 해당 페이지로 이동합니다:

```javascript
// Notifications.js
const handleNotificationClick = async (notification) => {
  // 읽음 처리
  await notificationApi.markAsRead(notification.id);
  
  // 타입에 따라 페이지 이동
  switch (notification.type) {
    case 'ORDER':
      navigate('/mypage/orders');
      break;
    case 'REVIEW':
      navigate('/mypage/reviews');
      break;
    case 'QNA':
      navigate('/mypage/qna');
      break;
    case 'NOTICE':
      navigate(`/notices/${notification.referenceId}`);
      break;
  }
};
```

## 🔔 실시간 알림 개수 갱신

Header 컴포넌트에서 30초마다 자동으로 알림 개수를 갱신합니다:

```javascript
useEffect(() => {
  if (isAuthenticated) {
    updateNotificationCount();
    const interval = setInterval(updateNotificationCount, 30000);
    return () => clearInterval(interval);
  }
}, [isAuthenticated]);
```

## 💡 추가 개선 사항

### 1. WebSocket을 통한 실시간 알림 (선택사항)
30초 폴링 대신 WebSocket을 사용하면 즉시 알림을 받을 수 있습니다.

### 2. 알림 설정 기능
사용자가 받고 싶은 알림 타입을 선택할 수 있는 기능을 추가할 수 있습니다.

### 3. 알림 읽음/삭제 일괄 처리
"모두 삭제" 버튼을 추가하여 읽은 알림을 일괄 삭제할 수 있습니다.

## 🐛 트러블슈팅

### 알림이 생성되지 않는 경우
1. 데이터베이스 테이블이 생성되었는지 확인
2. @EnableJpaAuditing이 활성화되었는지 확인
3. userId가 정확한지 확인 (findByUserId 사용)

### 알림 개수가 업데이트되지 않는 경우
1. Redux store가 제대로 설정되었는지 확인
2. 브라우저 콘솔에서 에러 메시지 확인
3. API 호출이 성공하는지 네트워크 탭에서 확인
