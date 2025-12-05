// 브라우저 개발자 도구(F12) > Console 탭에서 실행하세요

// 1. 알림 데이터 확인
console.log('=== 알림 데이터 확인 ===');
fetch('http://localhost:8080/api/notifications', {
  headers: {
    'Authorization': 'Bearer ' + localStorage.getItem('accessToken')
  }
})
.then(r => r.json())
.then(data => {
  console.log('📋 전체 알림:', data);
  if (data.notifications) {
    const orderNotifications = data.notifications.filter(n => n.type === 'ORDER');
    console.log('📦 주문 알림들:', orderNotifications);
    if (orderNotifications.length > 0) {
      console.log('✅ 첫 번째 주문 알림의 referenceId:', orderNotifications[0].referenceId);
    }
  }
});

// 2. 주문 목록 확인
console.log('\n=== 주문 목록 확인 ===');
const userInfo = JSON.parse(localStorage.getItem('userInfo'));
if (userInfo && userInfo.id) {
  fetch(`http://localhost:8080/api/orders/user/${userInfo.id}`, {
    headers: {
      'Authorization': 'Bearer ' + localStorage.getItem('accessToken')
    }
  })
  .then(r => r.json())
  .then(data => {
    console.log('📦 주문 목록:', data);
    if (data.data && data.data.length > 0) {
      console.log('✅ 최근 주문 ID:', data.data[0].orderId);
    }
  });
}

// 3. 라우트 테스트
console.log('\n=== 라우트 테스트 ===');
console.log('현재 URL:', window.location.href);
console.log('Path:', window.location.pathname);
