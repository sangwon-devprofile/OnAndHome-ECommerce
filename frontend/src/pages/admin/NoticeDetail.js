import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import AdminSidebar from '../../components/admin/AdminSidebar';
import noticeApi from '../../api/noticeApi';
import './NoticeDetail.css';

const NoticeDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [notice, setNotice] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchNoticeDetail();
  }, [id]);

  const fetchNoticeDetail = async () => {
    setLoading(true);
    try {
      const data = await noticeApi.getNoticeDetail(id);
      setNotice(data);
    } catch (error) {
      console.error('공지사항 로드 실패:', error);
      alert('공지사항을 불러오는데 실패했습니다.');
      navigate('/admin/notices');
    } finally {
      setLoading(false);
    }
  };

  const handleEdit = () => {
    navigate(`/admin/notices/edit/${id}`);
  };

  const handleDelete = async () => {
    if (!window.confirm('정말 삭제하시겠습니까?')) {
      return;
    }

    try {
      await noticeApi.deleteNotice(id);
      alert('삭제되었습니다.');
      navigate('/admin/notices');
    } catch (error) {
      console.error('삭제 실패:', error);
      alert('삭제에 실패했습니다.');
    }
  };

  const handleList = () => {
    navigate('/admin/notices');
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
      <div className="admin-dashboard">
        <AdminSidebar />
        <div className="dashboard-main">
          <div className="loading">로딩 중...</div>
        </div>
      </div>
    );
  }

  if (!notice) {
    return (
      <div className="admin-dashboard">
        <AdminSidebar />
        <div className="dashboard-main">
          <div className="error-message">공지사항을 찾을 수 없습니다.</div>
        </div>
      </div>
    );
  }

  return (
    <div className="admin-dashboard">
      <AdminSidebar />
      
      <div className="dashboard-main">
        <div className="notice-detail-container">
          <div className="notice-detail-header">
            <h1>공지사항 상세</h1>
          </div>

          <div className="notice-detail-card">
            {/* 헤더 */}
            <div className="detail-header">
              <div className="title-section">
                <h2 className="detail-title">{notice.title}</h2>
              </div>
              
              <div className="meta-info">
                <div className="meta-item">
                  <span className="meta-label">작성자</span>
                  <span className="meta-value">{notice.writer || '관리자'}</span>
                </div>
                <div className="meta-item">
                  <span className="meta-label">작성일</span>
                  <span className="meta-value">{formatDate(notice.createdAt)}</span>
                </div>
                {notice.updatedAt && (
                  <div className="meta-item">
                    <span className="meta-label">수정일</span>
                    <span className="meta-value">{formatDate(notice.updatedAt)}</span>
                  </div>
                )}
              </div>
            </div>

            {/* 내용 */}
            <div className="detail-content">
              <div className="content-body">
                {notice.content.split('\n').map((line, index) => (
                  <p key={index}>{line || '\u00A0'}</p>
                ))}
              </div>
            </div>

            {/* 액션 버튼 */}
            <div className="detail-actions">
              <button className="btn-list" onClick={handleList}>
                목록
              </button>
              <div className="action-group">
                <button className="btn-edit" onClick={handleEdit}>
                  수정
                </button>
                <button className="btn-delete" onClick={handleDelete}>
                  삭제
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default NoticeDetail;
