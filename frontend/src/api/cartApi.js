import apiClient from './axiosConfig';

/**
 * 장바구니 관련 API - Spring Boot 엔드포인트에 맞게 수정
 */
const cartApi = {
  /**
   * 장바구니에 상품 추가
   * POST /api/cart/add
   */
  addToCart: async (productId, quantity = 1) => {
    const response = await apiClient.post('/api/cart/add', {
      productId,
      quantity
    });
    return response.data;
  },

  /**
   * 장바구니 목록 조회
   * GET /api/cart
   */
  getCartItems: async () => {
    const response = await apiClient.get('/api/cart');
    return response.data;
  },

  /**
   * 장바구니 아이템 개수 조회
   * GET /api/cart/count
   */
  getCartCount: async () => {
    const response = await apiClient.get('/api/cart/count');
    return response.data;
  },

  /**
   * 장바구니 아이템 수량 수정
   * PUT /api/cart/{cartItemId}
   */
  updateQuantity: async (cartItemId, quantity) => {
    const response = await apiClient.put(`/api/cart/${cartItemId}`, {
      quantity
    });
    return response.data;
  },

  /**
   * 장바구니 아이템 삭제
   * DELETE /api/cart/{cartItemId}
   */
  removeItem: async (cartItemId) => {
    const response = await apiClient.delete(`/api/cart/${cartItemId}`);
    return response.data;
  },

  /**
   * 장바구니 전체 비우기
   * DELETE /api/cart/clear/all
   */
  clearCart: async () => {
    const response = await apiClient.delete('/api/cart/clear/all');
    return response.data;
  },
};

export default cartApi;
