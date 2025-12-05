import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import qnaApi from '../../api/qnaApi';
import './MyQna.css';

const MyQna = () => {
  const navigate = useNavigate();
  const [qnaList, setQnaList] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    fetchMyQna();
  }, []);

  const fetchMyQna = async () => {
    setLoading(true);
    try {
      const response = await qnaApi.getMyQna();
      if (response.success) {
        setQnaList(response.data);
      }
    } catch (error) {
      console.error('내 Q&A 조회 실패:', error);
      alert('Q&A를 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const handleQnaClick = (qna) => {
    // 상품 상세 페이지의 Q&A 탭으로 이동
    navigate(`/products/${qna.productId}?tab=qna&qnaId=${qna.id}`);
  };

  const formatDate = (dateString) => {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return date.toLocaleDateString('ko-KR', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit'
    }).replace(/\. /g, '-').replace('.', '');
  };

  if (loading) {
    return (
      <div className="my-qna-container">
        <div className="loading">로딩 중...</div>
      </div>
    );
  }

  return (
    <div className="my-qna-container">
      <div className="my-qna-header">
        <h2>문의 내역</h2>
        <p>작성하신 문의를 확인할 수 있습니다.</p>
      </div>

      {qnaList.length === 0 ? (
        <div className="empty-state">
          <p>작성한 문의가 없습니다.</p>
        </div>
      ) : (
        <div className="qna-list">
          {qnaList.map((qna) => (
            <div 
              key={qna.id} 
              className="qna-item"
              onClick={() => handleQnaClick(qna)}
            >
              <div className="qna-item-header">
                <div className="product-info">
                  <span className="product-name">{qna.productName}</span>
                  <span className="qna-date">{formatDate(qna.createdAt)}</span>
                </div>
                <div className="qna-status">
                  {qna.replies && qna.replies.length > 0 ? (
                    <span className="status-badge answered">답변완료</span>
                  ) : (
                    <span className="status-badge waiting">답변대기</span>
                  )}
                </div>
              </div>
              <div className="qna-item-body">
                <h3 className="qna-title">{qna.title || '상품 문의'}</h3>
                <p className="qna-question">{qna.question}</p>
                {qna.replies && qna.replies.length > 0 && (
                  <div className="qna-reply-preview">
                    <span className="reply-label">답변:</span>
                    <span className="reply-content">
                      {qna.replies[0].content.substring(0, 100)}
                      {qna.replies[0].content.length > 100 && '...'}
                    </span>
                  </div>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default MyQna;
