import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import AdminSidebar from '../../components/layout/AdminSidebar';
import noticeApi from '../../api/noticeApi';
import './NoticeWrite.css';

const NoticeEdit = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    title: '',
    content: '',
    writer: '관리자'
  });
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    fetchNoticeDetail();
  }, [id]);

  const fetchNoticeDetail = async () => {
    setLoading(true);
    try {
      const data = await noticeApi.getNoticeDetail(id);
      setFormData({
        title: data.title,
        content: data.content,
        writer: data.writer || '관리자'
      });
    } catch (error) {
      console.error('공지사항 로드 실패:', error);
      alert('공지사항을 불러오는데 실패했습니다.');
      navigate('/admin/notices');
    } finally {
      setLoading(false);
    }
  };

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

    setSubmitting(true);
    try {
      await noticeApi.updateNotice(id, formData);
      alert('공지사항이 수정되었습니다.');
      navigate(`/admin/notices/${id}`);
    } catch (error) {
      console.error('공지사항 수정 실패:', error);
      alert('공지사항 수정에 실패했습니다.');
    } finally {
      setSubmitting(false);
    }
  };

  const handleCancel = () => {
    if (window.confirm('수정을 취소하시겠습니까? 변경사항은 저장되지 않습니다.')) {
      navigate(`/admin/notices/${id}`);
    }
  };

  if (loading) {
    return (
      <div className="admin-dashboard">
        <AdminSidebar />
        <div className="dashboard-main">
          <div className="loading">로딩 중...</div>
        </div>
      </div>
    );
  }

  return (
    <div className="admin-dashboard">
      <AdminSidebar />
      
      <div className="dashboard-main">
        <div className="notice-write-container">
          <div className="notice-write-header">
            <h1>공지사항 수정</h1>
            <p className="notice-description">공지사항을 수정합니다</p>
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
                disabled={submitting}
              >
                {submitting ? '수정 중...' : '수정하기'}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default NoticeEdit;
