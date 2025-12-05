import axiosInstance from './axiosConfig';

// 알림 목록 조회
export const getNotifications = async () => {
  const response = await axiosInstance.get('/api/notifications');
  return response.data;
};

// 읽지 않은 알림 개수 조회
export const getUnreadCount = async () => {
  const response = await axiosInstance.get('/api/notifications/unread-count');
  return response.data;
};

// 알림 읽음 처리
export const markAsRead = async (notificationId) => {
  const response = await axiosInstance.put(`/api/notifications/${notificationId}/read`);
  return response.data;
};

// 모든 알림 읽음 처리
export const markAllAsRead = async () => {
  const response = await axiosInstance.put('/api/notifications/read-all');
  return response.data;
};

// 알림 삭제
export const deleteNotification = async (notificationId) => {
  const response = await axiosInstance.delete(`/api/notifications/${notificationId}`);
  return response.data;
};

const notificationApi = {
  getNotifications,
  getUnreadCount,
  markAsRead,
  markAllAsRead,
  deleteNotification,
};

export default notificationApi;
