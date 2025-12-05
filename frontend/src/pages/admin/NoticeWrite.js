import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import AdminSidebar from '../../components/admin/AdminSidebar';
import noticeApi from '../../api/noticeApi';
import './NoticeWrite.css';

const NoticeWrite = () => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    title: '',
    content: '',
    writer: '관리자'
  });
  const [loading, setLoading] = useState(false);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!formData.title.trim()) {
      alert('제목을 입력해주세요.');
      return;
    }
    
    if (!formData.content.trim()) {
      alert('내용을 입력해주세요.');
      return;
    }

    setLoading(true);
    try {
      await noticeApi.createNotice(formData);
      alert('공지사항이 등록되었습니다.');
      navigate('/admin/notices');
    } catch (error) {
      console.error('공지사항 등록 실패:', error);
      alert('공지사항 등록에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = () => {
    if (window.confirm('작성을 취소하시겠습니까? 작성 중인 내용은 저장되지 않습니다.')) {
      navigate('/admin/notices');
    }
  };

  return (
    <div className="admin-dashboard">
      <AdminSidebar />
      
      <div className="dashboard-main">
        <div className="notice-write-container">
          <div className="notice-write-header">
            <h1>공지사항 작성</h1>
            <p className="notice-description">새로운 공지사항을 작성합니다</p>
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
                  onChange={handleInputChange}
                  placeholder="공지사항 제목을 입력하세요"
                  className="form-input"
                  maxLength={100}
                />
                <div className="char-count">
                  {formData.title.length} / 100
                </div>
              </div>

              <div className="form-section">
                <label className="form-label required">
                  작성자
                </label>
                <input
                  type="text"
                  name="writer"
                  value={formData.writer}
                  onChange={handleInputChange}
                  placeholder="작성자명을 입력하세요"
                  className="form-input"
                  maxLength={50}
                />
              </div>

              <div className="form-section">
                <label className="form-label required">
                  내용
                </label>
                <textarea
                  name="content"
                  value={formData.content}
                  onChange={handleInputChange}
                  placeholder="공지사항 내용을 입력하세요"
                  className="form-textarea"
                  rows={15}
                />
                <div className="char-count">
                  {formData.content.length}자
                </div>
              </div>
            </div>

            <div className="form-actions">
              <button
                type="button"
                onClick={handleCancel}
                className="btn-cancel"
              >
                취소
              </button>
              <button
                type="submit"
                className="btn-submit"
                disabled={loading}
              >
                {loading ? '등록 중...' : '등록하기'}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default NoticeWrite;
