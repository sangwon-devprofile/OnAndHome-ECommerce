import axios from './axiosConfig';

const advertisementApi = {
  // 광고 목록 조회
  getAll: async () => {
    const response = await axios.get('/api/admin/advertisements');
    return response.data;
  },

  // 광고 상세 조회
  getById: async (id) => {
    const response = await axios.get(`/api/admin/advertisements/${id}`);
    return response.data;
  },

  // 광고 상세 조회 (사용자용 - 마케팅 동의 필요)
  getByIdUser: async (id) => {
    const response = await axios.get(`/api/user/advertisements/${id}`);
    return response.data;
  },

  // 광고 생성
  create: async (data) => {
    const response = await axios.post('/api/admin/advertisements', data);
    return response.data;
  },

  // 광고 수정
  update: async (id, data) => {
    const response = await axios.put(`/api/admin/advertisements/${id}`, data);
    return response.data;
  },

  // 광고 삭제
  delete: async (id) => {
    const response = await axios.delete(`/api/admin/advertisements/${id}`);
    return response.data;
  },

  // 광고 알림 발송
  sendNotification: async (id) => {
    const response = await axios.post(`/api/admin/advertisements/${id}/send`);
    return response.data;
  },
};

export default advertisementApi;
