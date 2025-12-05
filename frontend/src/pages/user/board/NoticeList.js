import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useSelector } from 'react-redux';
import noticeApi from '../../../api/noticeApi';
import './NoticeList.css';

const NoticeList = () => {
  const navigate = useNavigate();
  const { user } = useSelector((state) => state.user);
  const [notices, setNotices] = useState([]);
  const [loading, setLoading] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 10;

  useEffect(() => {
    fetchNotices();
  }, []);

  const fetchNotices = async () => {
    setLoading(true);
    try {
      const data = await noticeApi.getAllNotices();
      // 날짜 기준 내림차순 정렬
      const sortedData = data.sort((a, b) => 
        new Date(b.createdAt) - new Date(a.createdAt)
      );
      setNotices(sortedData);
    } catch (error) {
      console.error('공지사항 로드 실패:', error);
      alert('공지사항을 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = (e) => {
    setSearchTerm(e.target.value);
    setCurrentPage(1);
  };

  const filteredNotices = notices.filter(notice =>
    notice.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
    (notice.writer && notice.writer.toLowerCase().includes(searchTerm.toLowerCase()))
  );

  const indexOfLastItem = currentPage * itemsPerPage;
  const indexOfFirstItem = indexOfLastItem - itemsPerPage;
  const currentNotices = filteredNotices.slice(indexOfFirstItem, indexOfLastItem);
  const totalPages = Math.ceil(filteredNotices.length / itemsPerPage);

  const handlePageChange = (pageNumber) => {
    setCurrentPage(pageNumber);
  };

  const handleNoticeClick = (id) => {
    navigate(`/notices/${id}`);
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

  // 관리자 여부 확인
  const isAdmin = () => {
    if (!user) return false;
    return user.role === 0 || user.role === "0" || Number(user.role) === 0;
  };

  return (
    <div className="user-notice-container">
      <div className="user-notice-inner">
        <div className="user-notice-header">
          <h1>공지사항</h1>
          <p className="user-notice-description">On&Home의 새로운 소식과 공지사항을 확인하세요</p>
        </div>

        <div className="user-notice-controls">
          <div className="search-box">
            <input
              type="text"
              placeholder="제목 또는 작성자로 검색"
              value={searchTerm}
              onChange={handleSearch}
              className="search-input"
            />
            <span className="search-icon">🔍</span>
          </div>
        </div>

        <div className="user-notice-stats">
          <span className="total-count">전체 {filteredNotices.length}건</span>
        </div>

        {loading ? (
          <div className="loading">로딩 중...</div>
        ) : (
          <>
            <div className="user-notice-table-wrapper">
              <table className="user-notice-table">
                <thead>
                  <tr>
                    <th style={{ width: '80px' }}>번호</th>
                    <th style={{ width: 'auto' }}>제목</th>
                    <th style={{ width: '120px' }}>작성자</th>
                    <th style={{ width: '120px' }}>작성일</th>
                  </tr>
                </thead>
                <tbody>
                  {currentNotices.length === 0 ? (
                    <tr>
                      <td colSpan="4" className="no-data">
                        등록된 공지사항이 없습니다.
                      </td>
                    </tr>
                  ) : (
                    currentNotices.map((notice, index) => (
                      <tr key={notice.id}>
                        <td>{indexOfFirstItem + index + 1}</td>
                        <td className="title-cell">
                          <span
                            className="notice-title"
                            onClick={() => handleNoticeClick(notice.id)}
                          >
                            {notice.title}
                          </span>
                        </td>
                        <td>{notice.writer || '관리자'}</td>
                        <td>{formatDate(notice.createdAt)}</td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>

            {totalPages > 1 && (
              <div className="pagination">
                <button
                  className="page-btn"
                  onClick={() => handlePageChange(currentPage - 1)}
                  disabled={currentPage === 1}
                >
                  이전
                </button>
                
                {[...Array(totalPages)].map((_, index) => (
                  <button
                    key={index + 1}
                    className={`page-btn ${currentPage === index + 1 ? 'active' : ''}`}
                    onClick={() => handlePageChange(index + 1)}
                  >
                    {index + 1}
                  </button>
                ))}
                
                <button
                  className="page-btn"
                  onClick={() => handlePageChange(currentPage + 1)}
                  disabled={currentPage === totalPages}
                >
                  다음
                </button>
              </div>
            )}
          </>
        )}
      </div>
    </div>
  );
};

export default NoticeList;
