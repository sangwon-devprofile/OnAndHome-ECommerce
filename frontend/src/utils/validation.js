/**
 * 검증 관련 유틸리티
 */

/**
 * 이메일 검증
 */
export const validateEmail = (email) => {
  const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return regex.test(email);
};

/**
 * 비밀번호 검증
 * - 최소 8자 이상
 * - 영문, 숫자, 특수문자 포함
 */
export const validatePassword = (password) => {
  if (password.length < 8) {
    return { valid: false, message: '비밀번호는 최소 8자 이상이어야 합니다.' };
  }
  
  const hasLetter = /[a-zA-Z]/.test(password);
  const hasNumber = /[0-9]/.test(password);
  const hasSpecial = /[!@#$%^&*(),.?":{}|<>]/.test(password);
  
  if (!hasLetter || !hasNumber || !hasSpecial) {
    return { 
      valid: false, 
      message: '비밀번호는 영문, 숫자, 특수문자를 모두 포함해야 합니다.' 
    };
  }
  
  return { valid: true, message: '' };
};

/**
 * 전화번호 검증
 */
export const validatePhoneNumber = (phoneNumber) => {
  const cleaned = phoneNumber.replace(/\D/g, '');
  return cleaned.length === 10 || cleaned.length === 11;
};

/**
 * 이름 검증 (한글, 영문만 허용)
 */
export const validateName = (name) => {
  const regex = /^[가-힣a-zA-Z\s]+$/;
  return regex.test(name) && name.length >= 2;
};

/**
 * 우편번호 검증 (5자리 숫자)
 */
export const validateZipCode = (zipCode) => {
  const regex = /^\d{5}$/;
  return regex.test(zipCode);
};

/**
 * 빈 값 검증
 */
export const isEmpty = (value) => {
  if (value === null || value === undefined) return true;
  if (typeof value === 'string') return value.trim() === '';
  if (Array.isArray(value)) return value.length === 0;
  if (typeof value === 'object') return Object.keys(value).length === 0;
  return false;
};

/**
 * 숫자 검증
 */
export const isNumber = (value) => {
  return !isNaN(parseFloat(value)) && isFinite(value);
};

/**
 * 양수 검증
 */
export const isPositiveNumber = (value) => {
  return isNumber(value) && parseFloat(value) > 0;
};
