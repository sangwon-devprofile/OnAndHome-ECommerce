// JWT 토큰 저장 및 조회 유틸리티

/**
 * 액세스 토큰 저장
 */
export const setAccessToken = (token) => {
  localStorage.setItem('accessToken', token);
};

/**
 * 리프레시 토큰 저장
 */
export const setRefreshToken = (token) => {
  localStorage.setItem('refreshToken', token);
};

/**
 * 액세스 토큰 조회
 */
export const getAccessToken = () => {
  return localStorage.getItem('accessToken');
};

/**
 * 리프레시 토큰 조회
 */
export const getRefreshToken = () => {
  return localStorage.getItem('refreshToken');
};

/**
 * 토큰 삭제 (로그아웃)
 */
export const removeTokens = () => {
  localStorage.removeItem('accessToken');
  localStorage.removeItem('refreshToken');
};

/**
 * JWT 토큰 디코딩 (Payload 추출)
 */
export const decodeToken = (token) => {
  try {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split('')
        .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    );
    return JSON.parse(jsonPayload);
  } catch (error) {
    console.error('토큰 디코딩 실패:', error);
    return null;
  }
};

/**
 * 토큰 만료 확인
 */
export const isTokenExpired = (token) => {
  if (!token) return true;
  
  const decoded = decodeToken(token);
  if (!decoded || !decoded.exp) return true;
  
  // exp는 초 단위이므로 1000을 곱해서 밀리초로 변환
  const expirationTime = decoded.exp * 1000;
  const currentTime = Date.now();
  
  return currentTime >= expirationTime;
};

/**
 * 현재 사용자 정보 조회 (토큰에서)
 */
export const getCurrentUserInfo = () => {
  const token = getAccessToken();
  if (!token || isTokenExpired(token)) {
    return null;
  }
  return decodeToken(token);
};

/**
 * 사용자 권한 확인
 */
export const checkUserRole = (requiredRole) => {
  const userInfo = getCurrentUserInfo();
  if (!userInfo) return false;
  
  // role: 0 = admin, 1 = user
  if (requiredRole === 'admin') {
    return userInfo.role === 0;
  }
  return true;
};

/**
 * 로그인 상태 확인
 */
export const isAuthenticated = () => {
  const token = getAccessToken();
  return token && !isTokenExpired(token);
};
