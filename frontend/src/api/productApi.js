import apiClient from './axiosConfig';

/**
 * 상품 관련 API - Spring Boot 엔드포인트에 맞게 수정
 */
const productApi = {
  /**
   * 모든 상품 조회
   * GET /api/products/list
   */
  getAllProducts: async () => {
    const response = await apiClient.get('/api/products/list');
    return response.data;
  },

  /**
   * 상품 목록 조회 (페이지네이션)
   * 호환성을 위해 getProducts 메서드 추가
   */
  getProducts: async (params = {}) => {
    const response = await apiClient.get('/api/products/list', { params });
    return response.data;
  },

  /**
   * 상품 상세 조회
   * GET /api/products/{id}
   */
  getProductDetail: async (productId) => {
    const response = await apiClient.get(`/api/products/${productId}`);
    return response.data;
  },

  /**
   * 상품 검색
   * GET /api/products/search?keyword=검색어
   */
  searchProducts: async (keyword) => {
    const response = await apiClient.get(`/api/products/search`, {
      params: { keyword }
    });
    return response.data;
  },

  /**
   * 카테고리별 상품 조회
   * GET /user/product/api/category/{category}
   */
  getProductsByCategory: async (category) => {
    const response = await apiClient.get(`/user/product/api/category/${encodeURIComponent(category)}`);
    return response.data;
  },

  /**
   * 모든 상품 조회 (사용자용)
   * GET /user/product/api/all
   */
  getAllProductsForUser: async () => {
    const response = await apiClient.get('/user/product/api/all');
    return response.data;
  },

  /**
   * 상품 생성 (관리자 전용)
   * POST /api/products/create
   */
  createProduct: async (productData) => {
    const response = await apiClient.post('/api/products/create', productData);
    return response.data;
  },

  /**
   * 상품 수정 (관리자 전용)
   * PUT /api/products/{id}
   */
  updateProduct: async (productId, productData) => {
    const response = await apiClient.put(`/api/products/${productId}`, productData);
    return response.data;
  },

  /**
   * 상품 삭제 (관리자 전용)
   * DELETE /api/products/{id}
   */
  deleteProduct: async (productId) => {
    const response = await apiClient.delete(`/api/products/${productId}`);
    return response.data;
  },
};

export default productApi;
