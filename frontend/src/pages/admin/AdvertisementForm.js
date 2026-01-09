import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import AdminSidebar from '../../components/layout/AdminSidebar';
import advertisementApi from '../../api/advertisementApi';
import toast from 'react-hot-toast';
import './AdvertisementForm.css';

const AdvertisementForm = () => {
  const navigate = useNavigate();
  const { id } = useParams();
  const isEditMode = !!id;

  const [formData, setFormData] = useState({
    title: '',
    content: '',
    imageUrl: '',
    linkUrl: '',
    active: true,
  });

  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (isEditMode) {
      fetchAdvertisement();
    }
  }, [id]);

  const fetchAdvertisement = async () => {
    try {
      const response = await advertisementApi.getById(id);
      if (response.success) {
        setFormData(response.advertisement);
      }
    } catch (error) {
      console.error('광고 조회 실패:', error);
      toast.error('광고 정보를 불러오는데 실패했습니다.');
    }
  };

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value
    }));
  };

  const validateForm = () => {
    if (!formData.title.trim()) {
      toast.error('제목을 입력해주세요.');
      return false;
    }
    if (!formData.content.trim()) {
      toast.error('내용을 입력해주세요.');
      return false;
    }
    return true;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!validateForm()) {
      return;
    }

    try {
      setLoading(true);
      let response;

      if (isEditMode) {
        response = await advertisementApi.update(id, formData);
      } else {
        response = await advertisementApi.create(formData);
      }

      if (response.success) {
        toast.success(response.message);
        navigate('/admin/advertisements');
      }
    } catch (error) {
      console.error('광고 저장 실패:', error);
      toast.error(error.response?.data?.message || '광고 저장에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = () => {
    if (window.confirm('작성을 취소하시겠습니까? 작성 중인 내용은 저장되지 않습니다.')) {
      navigate('/admin/advertisements');
    }
  };

  return (
    <div className="admin-dashboard">
      <AdminSidebar />
      
      <div className="dashboard-main">
        <div className="notice-write-container">
          <div className="notice-write-header">
            <h1>{isEditMode ? '광고 수정' : '광고 작성'}</h1>
            <p className="notice-description">
              {isEditMode ? '광고 정보를 수정합니다.' : '새로운 광고를 작성합니다.'}
            </p>
          </div>

          <form onSubmit={handleSubmit} className="notice-write-form">
            <div className="form-card">
              <div className="form-section">
                <label className="form-label required">
                  제목
                </label>
                <input
                  type="text"
                  name="title"
                  value={formData.title}
                  onChange={handleChange}
                  placeholder="광고 제목을 입력하세요"
                  className="form-input"
                  maxLength={200}
                  disabled={loading}
                />
                <span className="char-count">{formData.title.length} / 200</span>
              </div>

              <div className="form-section">
                <label className="form-label required">
                  내용
                </label>
                <textarea
                  name="content"
                  value={formData.content}
                  onChange={handleChange}
                  placeholder="광고 내용을 입력하세요"
                  className="form-textarea"
                  rows={8}
                  maxLength={2000}
                  disabled={loading}
                />
                <span className="char-count">{formData.content.length} / 2000</span>
              </div>

              <div className="form-section">
                <label className="form-label">
                  이미지 URL
                </label>
                <input
                  type="text"
                  name="imageUrl"
                  value={formData.imageUrl}
                  onChange={handleChange}
                  placeholder="이미지 URL을 입력하세요 (선택사항)"
                  className="form-input"
                  disabled={loading}
                />
                <span className="form-hint">광고에 사용할 이미지의 URL을 입력하세요</span>
              </div>

              <div className="form-section">
                <label className="form-label">
                  링크 URL
                </label>
                <input
                  type="text"
                  name="linkUrl"
                  value={formData.linkUrl}
                  onChange={handleChange}
                  placeholder="링크 URL을 입력하세요 (선택사항)"
                  className="form-input"
                  disabled={loading}
                />
                <span className="form-hint">클릭 시 이동할 페이지 URL을 입력하세요</span>
              </div>

              <div className="form-section">
                <div className="checkbox-wrapper">
                  <label className="checkbox-label">
                    <input
                      type="checkbox"
                      name="active"
                      checked={formData.active}
                      onChange={handleChange}
                      disabled={loading}
                    />
                    <span className="checkbox-text">활성 상태</span>
                  </label>
                  <span className="form-hint">
                    비활성 상태로 설정하면 알림 발송이 되지 않습니다
                  </span>
                </div>
              </div>

              <div className="form-actions">
                <button
                  type="button"
                  onClick={handleCancel}
                  className="btn-cancel"
                  disabled={loading}
                >
                  취소
                </button>
                <button
                  type="submit"
                  className="btn-submit"
                  disabled={loading}
                >
                  {loading ? '처리중...' : isEditMode ? '수정' : '등록'}
                </button>
              </div>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default AdvertisementForm;

