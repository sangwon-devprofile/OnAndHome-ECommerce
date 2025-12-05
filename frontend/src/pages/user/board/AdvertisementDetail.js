import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, useLocation } from 'react-router-dom';
import advertisementApi from '../../../api/advertisementApi';
import './AdvertisementDetail.css';

const AdvertisementDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const location = useLocation();
  const [advertisement, setAdvertisement] = useState(null);
  const [loading, setLoading] = useState(true);

  // 알림에서 온 경우
  const fromNotifications = location.state?.from === 'notifications';

  useEffect(() => {
    fetchAdvertisementDetail();
  }, [id]);

  const fetchAdvertisementDetail = async () => {
    setLoading(true);
    try {
      const response = await advertisementApi.getByIdUser(id);
      if (response.success) {
        setAdvertisement(response.advertisement);
      } else {
        // 마케팅 동의하지 않은 경우
        alert(response.message || '광고를 볼 수 없습니다.');
        navigate('/');
      }
    } catch (error) {
      console.error('광고 로드 실패:', error);
      const message = error.response?.data?.message || '광고를 불러오는데 실패했습니다.';
      alert(message);
      navigate('/');
    } finally {
      setLoading(false);
    }
  };

  const handleBack = () => {
    if (fromNotifications) {
      navigate('/notifications');
    } else {
      navigate('/');
    }
  };

  const handleLinkClick = () => {
    if (advertisement.linkUrl) {
      window.open(advertisement.linkUrl, '_blank');
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return date.toLocaleString('ko-KR', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    });
  };

  if (loading) {
    return (
      <div className="user-notice-detail-container">
        <div className="loading">로딩 중...</div>
      </div>
    );
  }

  if (!advertisement) {
    return (
      <div className="user-notice-detail-container">
        <div className="error-message">광고를 찾을 수 없습니다.</div>
      </div>
    );
  }

  return (
    <div className="user-notice-detail-container">
      <div className="notice-detail-wrapper">
        <div className="notice-detail-header">
          <h1 className="notice-detail-title">
            📢 {advertisement.title}
          </h1>
          <div className="notice-detail-meta">
            <div className="meta-item">
              <span className="meta-label">작성자</span>
              <span className="meta-value">관리자</span>
            </div>
            <div className="meta-item">
              <span className="meta-label">작성일</span>
              <span className="meta-value">{formatDate(advertisement.createdAt)}</span>
            </div>
            {advertisement.sentAt && (
              <div className="meta-item">
                <span className="meta-label">발송일</span>
                <span className="meta-value">{formatDate(advertisement.sentAt)}</span>
              </div>
            )}
          </div>
        </div>

        <div className="notice-detail-divider"></div>

        <div className="notice-detail-content">
          {advertisement.imageUrl && (
            <div className="advertisement-image">
              <img 
                src={advertisement.imageUrl} 
                alt={advertisement.title}
                onError={(e) => {
                  e.target.style.display = 'none';
                }}
              />
            </div>
          )}
          
          <div className="content-text">
            {advertisement.content.split('\n').map((line, index) => (
              <p key={index}>{line || '\u00A0'}</p>
            ))}
          </div>

          {advertisement.linkUrl && (
            <div className="advertisement-link">
              <button 
                className="btn-link"
                onClick={handleLinkClick}
              >
                🔗 자세히 보기
              </button>
            </div>
          )}
        </div>

        <div className="notice-detail-actions">
          <button 
            className="btn-list" 
            onClick={handleBack}
          >
            {fromNotifications ? '알림 목록으로' : '목록으로'}
          </button>
        </div>
      </div>
    </div>
  );
};

export default AdvertisementDetail;
