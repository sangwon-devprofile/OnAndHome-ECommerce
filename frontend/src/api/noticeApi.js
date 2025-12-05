import apiClient from './axiosConfig';

/**
 * 공지사항 관련 API
 */
const noticeApi = {
  /**
   * 공지사항 목록 조회 (전체)
   */
  getAllNotices: async () => {
    const response = await apiClient.get('/api/notices');
    return response.data;
  },

  /**
   * 공지사항 상세 조회
   */
  getNoticeDetail: async (noticeId) => {
    const response = await apiClient.get(`/api/notices/${noticeId}`);
    return response.data;
  },

  /**
   * 공지사항 작성 (관리자)
   */
  createNotice: async (noticeData) => {
    const response = await apiClient.post('/api/notices', noticeData);
    return response.data;
  },

  /**
   * 공지사항 수정 (관리자)
   */
  updateNotice: async (noticeId, noticeData) => {
    const response = await apiClient.put(`/api/notices/${noticeId}`, noticeData);
    return response.data;
  },

  /**
   * 공지사항 삭제 (관리자)
   */
  deleteNotice: async (noticeId) => {
    const response = await apiClient.delete(`/api/notices/${noticeId}`);
    return response.data;
  },

  /**
   * 공지사항 검색
   */
  searchNotices: async (keyword) => {
    const response = await apiClient.get('/api/notices/search', {
      params: { keyword }
    });
    return response.data;
  },

  /**
   * 최근 공지사항 조회
   */
  getRecentNotices: async (limit = 8) => {
    const response = await apiClient.get('/api/notices/recent', {
      params: { limit }
    });
    return response.data;
  }
};

export default noticeApi;
