// API 모듈들을 하나로 통합
import authApi from './authApi';
import productApi from './productApi';
import cartApi from './cartApi';
import orderApi from './orderApi';
import userApi from './userApi';
import reviewApi from './reviewApi';
import qnaApi from './qnaApi';
import noticeApi from './noticeApi';
import { favoriteAPI } from './favoriteApi';

// 대문자 export도 추가 (기존 코드 호환성)
export const authAPI = authApi;
export const productAPI = productApi;
export const cartAPI = cartApi;
export const orderAPI = orderApi;
export const userAPI = userApi;
export const reviewAPI = reviewApi;
export const qnaAPI = qnaApi;
export const noticeAPI = noticeApi;
export { favoriteAPI };

// 소문자 export (새 코드용)
export {
  authApi,
  productApi,
  cartApi,
  orderApi,
  userApi,
  reviewApi,
  qnaApi,
  noticeApi,
};

// companyApi 추가 (Footer에서 사용)
export const companyAPI = {
  getCompanyInfo: async () => {
    // 임시로 빈 객체 반환 (나중에 구현)
    return { data: {} };
  }
};
