// 테스트용 간단한 알림 버튼 컴포넌트
import React from 'react';
import { useNavigate } from 'react-router-dom';
import { useSelector } from 'react-redux';
import './NotificationBell.css';

const NotificationBell = () => {
  const navigate = useNavigate();
  const unreadCount = useSelector((state) => state.notification?.unreadCount || 0);
  
  console.log('🔔 NotificationBell rendered, unreadCount:', unreadCount);

  return (
    <div 
      className="notification-bell-wrapper"
      onClick={() => {
        console.log('종 클릭!');
        navigate('/notifications');
      }}
      title="알림"
    >
      <span className="bell-icon">🔔</span>
      {unreadCount > 0 && (
        <span className="bell-badge">{unreadCount > 99 ? '99+' : unreadCount}</span>
      )}
    </div>
  );
};

export default NotificationBell;
