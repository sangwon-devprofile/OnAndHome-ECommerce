import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import './ResetPassword.css';

const ResetPassword = () => {
  const navigate = useNavigate();
  
  // 단계: 1=이메일 입력, 2=인증 코드 입력, 3=새 비밀번호 입력
  const [step, setStep] = useState(1);
  
  const [formData, setFormData] = useState({
    email: '',
    verificationCode: '',
    newPassword: '',
    passwordConfirm: '',
  });
  
  const [emailVerification, setEmailVerification] = useState({
    codeSent: false,
    codeVerified: false,
    timer: 0,
    timerInterval: null,
  });
  
  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(false);
  const [successMessage, setSuccessMessage] = useState('');
  const [errorAlert, setErrorAlert] = useState('');
  
  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
    
    setErrors(prev => ({
      ...prev,
      [name]: ''
    }));
    setErrorAlert('');
  };
  
  // 타이머 시작
  const startTimer = () => {
    if (emailVerification.timerInterval) {
      clearInterval(emailVerification.timerInterval);
    }
    
    setEmailVerification(prev => ({ ...prev, timer: 300 })); // 5분
    
    const interval = setInterval(() => {
      setEmailVerification(prev => {
        if (prev.timer <= 1) {
          clearInterval(interval);
          return { ...prev, timer: 0, timerInterval: null, codeSent: false };
        }
        return { ...prev, timer: prev.timer - 1 };
      });
    }, 1000);
    
    setEmailVerification(prev => ({ ...prev, timerInterval: interval }));
  };
  
  // 타이머 포맷팅
  const formatTime = (seconds) => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins}:${secs.toString().padStart(2, '0')}`;
  };
  
  // 인증 코드 전송
  const handleSendCode = async () => {
    if (!formData.email) {
      setErrorAlert('이메일을 입력해주세요.');
      return;
    }
    
    const emailRegex = /^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/;
    if (!emailRegex.test(formData.email)) {
      setErrorAlert('올바른 이메일 형식이 아닙니다.');
      return;
    }
    
    setLoading(true);
    setErrorAlert('');
    
    try {
      const response = await fetch('http://localhost:8080/api/email/send-password-reset-code', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ email: formData.email }),
      });
      
      const data = await response.json();
      
      if (data.success) {
        setEmailVerification(prev => ({
          ...prev,
          codeSent: true,
          codeVerified: false,
        }));
        startTimer();
        setSuccessMessage('인증 코드가 이메일로 전송되었습니다.');
        setTimeout(() => setSuccessMessage(''), 3000);
        setStep(2);
      } else {
        setErrorAlert(data.message || '인증 코드 전송에 실패했습니다.');
      }
    } catch (error) {
      console.error('인증 코드 전송 오류:', error);
      setErrorAlert('인증 코드 전송 중 오류가 발생했습니다.');
    } finally {
      setLoading(false);
    }
  };
  
  // 인증 코드 확인
  const handleVerifyCode = async () => {
    if (!formData.verificationCode) {
      setErrorAlert('인증 코드를 입력해주세요.');
      return;
    }
    
    setLoading(true);
    setErrorAlert('');
    
    try {
      const response = await fetch('http://localhost:8080/api/email/verify-password-reset-code', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          email: formData.email,
          code: formData.verificationCode,
        }),
      });
      
      const data = await response.json();
      
      if (data.success) {
        if (emailVerification.timerInterval) {
          clearInterval(emailVerification.timerInterval);
        }
        setEmailVerification(prev => ({
          ...prev,
          codeVerified: true,
          timer: 0,
          timerInterval: null,
        }));
        setSuccessMessage('인증이 완료되었습니다!');
        setTimeout(() => {
          setSuccessMessage('');
          setStep(3); // 새 비밀번호 입력 단계로 이동
        }, 1500);
      } else {
        setErrorAlert(data.message || '인증 코드가 올바르지 않습니다.');
      }
    } catch (error) {
      console.error('인증 코드 확인 오류:', error);
      setErrorAlert('인증 코드 확인 중 오류가 발생했습니다.');
    } finally {
      setLoading(false);
    }
  };
  
  // 비밀번호 재설정
  const handleResetPassword = async (e) => {
    e.preventDefault();
    setErrorAlert('');
    setSuccessMessage('');
    
    if (!formData.newPassword || formData.newPassword.length < 8) {
      setErrorAlert('비밀번호는 8자 이상이어야 합니다.');
      return;
    }
    
    if (formData.newPassword !== formData.passwordConfirm) {
      setErrorAlert('비밀번호가 일치하지 않습니다.');
      return;
    }
    
    setLoading(true);
    
    try {
      const response = await fetch('http://localhost:8080/api/user/reset-password', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          email: formData.email,
          newPassword: formData.newPassword,
        }),
      });
      
      const data = await response.json();
      
      if (data.success) {
        setSuccessMessage('비밀번호가 성공적으로 변경되었습니다! 로그인 페이지로 이동합니다.');
        setTimeout(() => {
          navigate('/login');
        }, 2000);
      } else {
        setErrorAlert(data.message || '비밀번호 변경에 실패했습니다.');
      }
    } catch (error) {
      console.error('비밀번호 재설정 실패:', error);
      setErrorAlert('비밀번호 재설정 처리 중 오류가 발생했습니다.');
    } finally {
      setLoading(false);
    }
  };
  
  const handleBack = () => {
    if (step > 1) {
      setStep(step - 1);
      if (step === 3) {
        setFormData(prev => ({ ...prev, newPassword: '', passwordConfirm: '' }));
      }
    } else {
      navigate('/login');
    }
  };
  
  useEffect(() => {
    return () => {
      if (emailVerification.timerInterval) {
        clearInterval(emailVerification.timerInterval);
      }
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);
  
  return (
    <div className="on-main-wrap">
      <div className="reset-password-wrapper mb-40">
        <div className="reset-password-inner">
          <h2 className="mb-30">비밀번호 재설정</h2>
          <div className="border-1p"></div>
          
          {/* Step 1: 이메일 입력 */}
          {step === 1 && (
            <div className="mt-40">
              <p className="reset-description">
                입력하신 이메일로 주소로 인증번호가 발송되었습니다.<br />
                인증번호 6자를 입력해 주세요.
              </p>
              
              <div className="form-group">
                <label className="login-label" htmlFor="email">
                  이메일 (필수)
                </label>
                <input
                  type="email"
                  id="email"
                  className="input"
                  name="email"
                  value={formData.email}
                  onChange={handleChange}
                  placeholder="skadnqls89@naver.com"
                  disabled={loading}
                />
                {errors.email && (
                  <div className="error-message">{errors.email}</div>
                )}
              </div>
              
              {successMessage && (
                <div className="success-message">{successMessage}</div>
              )}
              
              {errorAlert && (
                <div className="error-alert">{errorAlert}</div>
              )}
              
              <div className="button-group">
                <button
                  type="button"
                  className="btn btn--primary"
                  onClick={handleSendCode}
                  disabled={loading}
                >
                  {loading ? '전송중...' : '재전송'}
                </button>
              </div>
            </div>
          )}
          
          {/* Step 2: 인증 코드 입력 */}
          {step === 2 && (
            <div className="mt-40">
              <p className="reset-description reset-description-verified">
                인증번호가 발송되었습니다
              </p>
              
              <div className="form-group">
                <label className="login-label-small">인증번호 입력</label>
                <div className="verification-row">
                  <input
                    type="text"
                    className="input verification-code-input-reset"
                    name="verificationCode"
                    value={formData.verificationCode}
                    onChange={handleChange}
                    placeholder=""
                    maxLength="6"
                    disabled={loading}
                  />
                  {emailVerification.timer > 0 && (
                    <div className="timer-display-reset">
                      {formatTime(emailVerification.timer)}
                    </div>
                  )}
                  <button
                    type="button"
                    className="btn btn--primary-outline verify-btn"
                    onClick={handleVerifyCode}
                    disabled={loading}
                  >
                    인증 완료
                  </button>
                </div>
              </div>
              
              {successMessage && (
                <div className="success-message">{successMessage}</div>
              )}
              
              {errorAlert && (
                <div className="error-alert">{errorAlert}</div>
              )}
              
              <div className="button-group">
                <button
                  type="button"
                  className="btn btn--secondary"
                  onClick={handleBack}
                  disabled={loading}
                >
                  다음
                </button>
              </div>
            </div>
          )}
          
          {/* Step 3: 새 비밀번호 입력 */}
          {step === 3 && (
            <form className="mt-40" onSubmit={handleResetPassword}>
              <p className="reset-description">
                새로운 비밀번호를 입력해주세요.
              </p>
              
              {/* 새 비밀번호 */}
              <div className="form-group">
                <label className="login-label" htmlFor="newPassword">
                  새 비밀번호 <span style={{ color: '#d32f2f' }}>*</span>
                </label>
                <input
                  type="password"
                  className="input"
                  id="newPassword"
                  name="newPassword"
                  value={formData.newPassword}
                  onChange={handleChange}
                  placeholder="8글자 이상"
                  required
                  disabled={loading}
                />
                {errors.newPassword && (
                  <div className="error-message">{errors.newPassword}</div>
                )}
              </div>
              
              {/* 비밀번호 확인 */}
              <div className="form-group">
                <label className="login-label" htmlFor="passwordConfirm">
                  비밀번호 확인 <span style={{ color: '#d32f2f' }}>*</span>
                </label>
                <input
                  type="password"
                  className="input"
                  id="passwordConfirm"
                  name="passwordConfirm"
                  value={formData.passwordConfirm}
                  onChange={handleChange}
                  placeholder="비밀번호 재입력"
                  required
                  disabled={loading}
                />
                {errors.passwordConfirm && (
                  <div className="error-message">{errors.passwordConfirm}</div>
                )}
              </div>
              
              {successMessage && (
                <div className="success-message">{successMessage}</div>
              )}
              
              {errorAlert && (
                <div className="error-alert">{errorAlert}</div>
              )}
              
              <div className="border-2p"></div>
              
              <div className="button-group justify-start flex mb-20">
                <button
                  type="submit"
                  className="btn btn--primary"
                  disabled={loading}
                >
                  {loading ? '처리중...' : '비밀번호 변경'}
                </button>
                <button
                  type="button"
                  className="btn btn--primary-outline"
                  onClick={handleBack}
                  disabled={loading}
                >
                  취소
                </button>
              </div>
            </form>
          )}
        </div>
      </div>
    </div>
  );
};

export default ResetPassword;
