// 관리자 API 서비스
import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

// axios 인스턴스 생성
const adminApi = axios.create({
  baseURL: `${API_BASE_URL}/admin`,
  headers: {
    'Content-Type': 'application/json',
  },
});

// 요청 인터셉터 - 토큰 추가
adminApi.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('adminToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 응답 인터셉터 - 에러 처리
adminApi.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // 인증 실패 시 로그인 페이지로 이동
      localStorage.removeItem('adminToken');
      window.location.href = '/admin/login';
    }
    return Promise.reject(error);
  }
);

// 관리자 API 서비스
const adminService = {
  // 로그인
  login: async (credentials) => {
    const response = await adminApi.post('/login', credentials);
    return response.data;
  },

  // 로그아웃
  logout: async () => {
    const response = await adminApi.post('/logout');
    localStorage.removeItem('adminToken');
    return response.data;
  },

  // 대시보드 데이터 가져오기
  getDashboard: async () => {
    const response = await adminApi.get('/dashboard');
    return response.data;
  },

  // 회원 관리
  getUsers: async (params) => {
    const response = await adminApi.get('/users', { params });
    return response.data;
  },

  getUserDetail: async (userId) => {
    const response = await adminApi.get(`/users/${userId}`);
    return response.data;
  },

  updateUserStatus: async (userId, status) => {
    const response = await adminApi.put(`/users/${userId}/status`, { status });
    return response.data;
  },

  deleteUser: async (userId) => {
    const response = await adminApi.delete(`/users/${userId}`);
    return response.data;
  },

  deleteUsers: async (userIds) => {
    const response = await adminApi.post('/users/delete', { userIds });
    return response.data;
  },

  // 상품 관리
  getProducts: async (params) => {
    const response = await adminApi.get('/products', { params });
    return response.data;
  },

  getProductDetail: async (productId) => {
    const response = await adminApi.get(`/products/${productId}`);
    return response.data;
  },

  createProduct: async (productData) => {
    const response = await adminApi.post('/products', productData);
    return response.data;
  },

  updateProduct: async (productId, productData) => {
    const response = await adminApi.put(`/products/${productId}`, productData);
    return response.data;
  },

  deleteProduct: async (productId) => {
    const response = await adminApi.delete(`/products/${productId}`);
    return response.data;
  },

  // 주문 관리
  getOrders: async (params) => {
    const response = await adminApi.get('/orders', { params });
    return response.data;
  },

  getOrderDetail: async (orderId) => {
    const response = await adminApi.get(`/orders/${orderId}`);
    return response.data;
  },

  updateOrderStatus: async (orderId, status) => {
    const response = await adminApi.put(`/orders/${orderId}/status`, { status });
    return response.data;
  },

  // 게시판 관리
  getNotices: async (params) => {
    const response = await adminApi.get('/notices', { params });
    return response.data;
  },

  createNotice: async (noticeData) => {
    const response = await adminApi.post('/notices', noticeData);
    return response.data;
  },

  updateNotice: async (noticeId, noticeData) => {
    const response = await adminApi.put(`/notices/${noticeId}`, noticeData);
    return response.data;
  },

  deleteNotice: async (noticeId) => {
    const response = await adminApi.delete(`/notices/${noticeId}`);
    return response.data;
  },

  // Q&A 관리
  getQnas: async (params) => {
    const response = await adminApi.get('/qnas', { params });
    return response.data;
  },

  getQnaDetail: async (qnaId) => {
    const response = await adminApi.get(`/qnas/${qnaId}`);
    return response.data;
  },

  answerQna: async (qnaId, answer) => {
    const response = await adminApi.post(`/qnas/${qnaId}/answer`, { answer });
    return response.data;
  },

  // 리뷰 관리
  getReviews: async (params) => {
    const response = await adminApi.get('/reviews', { params });
    return response.data;
  },

  deleteReview: async (reviewId) => {
    const response = await adminApi.delete(`/reviews/${reviewId}`);
    return response.data;
  },

  // 통계 데이터
  getSalesStats: async (params) => {
    const response = await adminApi.get('/stats/sales', { params });
    return response.data;
  },

  getUserStats: async (params) => {
    const response = await adminApi.get('/stats/users', { params });
    return response.data;
  },

  getProductStats: async (params) => {
    const response = await adminApi.get('/stats/products', { params });
    return response.data;
  },
};

export default adminService;
