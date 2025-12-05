import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import AdminSidebar from '../../components/admin/AdminSidebar';
import axios from 'axios';
import './ReviewList.css';

const ReviewList = () => {
  const navigate = useNavigate();
  const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';

  const [reviews, setReviews] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectAll, setSelectAll] = useState(false);
  const [searchKeyword, setSearchKeyword] = useState('');
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 10;

  useEffect(() => {
    fetchReviews();
  }, []);

  const fetchReviews = async () => {
    setLoading(true);
    try {
      const response = await axios.get(`${API_BASE_URL}/api/admin/reviews`, {
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
        }
      });

      console.log('리뷰 목록 응답:', response.data);

      if (response.data && Array.isArray(response.data)) {
        const reviewsWithCheck = response.data.map(review => ({
          ...review,
          checked: false
        }));
        setReviews(reviewsWithCheck);
      } else {
        setReviews([]);
      }
    } catch (error) {
      console.error('리뷰 목록 조회 실패:', error);
      alert('리뷰 목록을 불러오는데 실패했습니다.');
      setReviews([]);
    } finally {
      setLoading(false);
    }
  };

  const handleSelectAll = (e) => {
    const checked = e.target.checked;
    setSelectAll(checked);
    setReviews(reviews.map(review => ({ ...review, checked })));
  };

  const handleSelectReview = (reviewId) => {
    const updatedReviews = reviews.map(review =>
      review.id === reviewId ? { ...review, checked: !review.checked } : review
    );
    setReviews(updatedReviews);

    const allChecked = updatedReviews.every(review => review.checked);
    setSelectAll(allChecked);
  };

  const handleSearch = () => {
    // 검색 기능은 필터링으로 구현
    fetchReviews();
  };

  const handleDeleteSelected = async () => {
    const selectedReviews = reviews.filter(review => review.checked);

    if (selectedReviews.length === 0) {
      alert('삭제할 리뷰를 선택해주세요.');
      return;
    }

    if (!window.confirm(`선택한 ${selectedReviews.length}개의 리뷰를 삭제하시겠습니까?\n\n이 작업은 되돌릴 수 없습니다.`)) {
      return;
    }

    setLoading(true);

    try {
      const reviewIds = selectedReviews.map(review => review.id);

      const response = await axios.post(
        `${API_BASE_URL}/api/admin/reviews/delete`,
        { ids: reviewIds },
        {
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
          }
        }
      );

      if (response.data && response.data.success) {
        alert(response.data.message || `${selectedReviews.length}개의 리뷰가 삭제되었습니다.`);
        await fetchReviews();
        setSelectAll(false);
      } else {
        alert(response.data.message || '리뷰 삭제에 실패했습니다.');
      }
    } catch (error) {
      console.error('리뷰 삭제 실패:', error);
      alert('리뷰 삭제 중 오류가 발생했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const handleRowClick = (reviewId) => {
    navigate(`/admin/reviews/${reviewId}`);
  };

  const formatDate = (dateString) => {
    if (!dateString) return '-';

    try {
      const date = new Date(dateString);
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const day = String(date.getDate()).padStart(2, '0');
      const hours = String(date.getHours()).padStart(2, '0');
      const minutes = String(date.getMinutes()).padStart(2, '0');
      return `${year}-${month}-${day} ${hours}:${minutes}`;
    } catch {
      return dateString;
    }
  };

  const renderStars = (rating) => {
    const stars = [];
    for (let i = 1; i <= 5; i++) {
      stars.push(
        <span key={i} className={i <= rating ? 'star filled' : 'star'}>
          ★
        </span>
      );
    }
    return <div className="rating-stars">{stars}</div>;
  };

  // 검색어 필터링
  const filteredReviews = reviews.filter(review => {
    if (!searchKeyword.trim()) return true;

    const keyword = searchKeyword.toLowerCase();
    return (
      review.content?.toLowerCase().includes(keyword) ||
      review.author?.toLowerCase().includes(keyword) ||
      review.productName?.toLowerCase().includes(keyword)
    );
  });

  // 페이지네이션
  const indexOfLastItem = currentPage * itemsPerPage;
  const indexOfFirstItem = indexOfLastItem - itemsPerPage;
  const currentItems = filteredReviews.slice(indexOfFirstItem, indexOfLastItem);
  const totalPages = Math.ceil(filteredReviews.length / itemsPerPage);

  const handlePageChange = (pageNumber) => {
    setCurrentPage(pageNumber);
  };

  if (loading) {
    return (
      <div className="admin-review-list">
        <AdminSidebar />
        <div className="review-list-main">
          <div className="loading">로딩 중...</div>
        </div>
      </div>
    );
  }

  return (
    <div className="admin-review-list">
      <AdminSidebar />

      <div className="review-list-main">
        <div className="page-header">
          <h1>리뷰 관리</h1>

          <div className="search-box">
            <input
              type="text"
              placeholder="상품명, 작성자, 내용을 입력하세요"
              value={searchKeyword}
              onChange={(e) => setSearchKeyword(e.target.value)}
              onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
            />
            <button onClick={handleSearch} className="search-btn">
              🔍
            </button>
          </div>
        </div>

        {/* 리뷰 목록 테이블 */}
        <div className="table-container">
          <table className="admin-table">
            <thead>
              <tr>
                <th style={{ width: '50px' }}>
                  <input
                    type="checkbox"
                    checked={selectAll}
                    onChange={handleSelectAll}
                    disabled={currentItems.length === 0}
                  />
                </th>
                <th style={{ width: '80px' }}>번호</th>
                <th style={{ width: '200px' }}>상품명</th>
                <th>내용</th>
                <th style={{ width: '120px' }}>평점</th>
                <th style={{ width: '120px' }}>작성자</th>
                <th style={{ width: '150px' }}>작성일자</th>
              </tr>
            </thead>
            <tbody>
              {currentItems.length === 0 ? (
                <tr>
                  <td colSpan="7" className="no-data">
                    리뷰가 없습니다.
                  </td>
                </tr>
              ) : (
                currentItems.map((review, index) => (
                  <tr 
                    key={review.id}
                    onClick={() => handleRowClick(review.id)}
                    style={{ cursor: 'pointer' }}
                  >
                    <td onClick={(e) => e.stopPropagation()}>
                      <input
                        type="checkbox"
                        checked={review.checked || false}
                        onChange={() => handleSelectReview(review.id)}
                      />
                    </td>
                    <td>{filteredReviews.length - (indexOfFirstItem + index)}</td>
                    <td className="text-left">{review.productName || '-'}</td>
                    <td className="text-left content-preview">
                      {review.content?.length > 50
                        ? review.content.substring(0, 50) + '...'
                        : review.content}
                    </td>
                    <td>{renderStars(review.rating)}</td>
                    <td>{review.author || '-'}</td>
                    <td>{formatDate(review.createdAt)}</td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        {/* 테이블 하단 */}
        <div className="table-footer">
          <button
            className="delete-btn"
            onClick={handleDeleteSelected}
            disabled={loading || reviews.filter(r => r.checked).length === 0}
          >
            삭제
          </button>

          {/* 페이지네이션 */}
          {totalPages > 1 && (
            <div className="pagination">
              <button
                onClick={() => handlePageChange(currentPage - 1)}
                disabled={currentPage === 1}
                className="page-button"
              >
                이전
              </button>

              {[...Array(totalPages)].map((_, index) => (
                <button
                  key={index + 1}
                  onClick={() => handlePageChange(index + 1)}
                  className={`page-button ${currentPage === index + 1 ? 'active' : ''}`}
                >
                  {index + 1}
                </button>
              ))}

              <button
                onClick={() => handlePageChange(currentPage + 1)}
                disabled={currentPage === totalPages}
                className="page-button"
              >
                다음
              </button>
            </div>
          )}
        </div>

        {/* 통계 정보 */}
        <div className="review-stats">
          <p>전체 리뷰: {filteredReviews.length}개</p>
        </div>
      </div>
    </div>
  );
};

export default ReviewList;
