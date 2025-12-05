import React, { useEffect, useState } from "react";
import toast from "react-hot-toast";
import { useNavigate } from "react-router-dom";
import advertisementApi from "../../api/advertisementApi";
import AdminSidebar from "../../components/admin/AdminSidebar";
import "./AdvertisementList.css";

const AdvertisementList = () => {
  const navigate = useNavigate();
  const [advertisements, setAdvertisements] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState("");
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 10;

  useEffect(() => {
    fetchAdvertisements();
  }, []);

  const fetchAdvertisements = async () => {
    try {
      setLoading(true);
      const response = await advertisementApi.getAll();
      if (response.success) {
        setAdvertisements(response.advertisements);
      }
    } catch (error) {
      console.error("광고 목록 조회 실패:", error);
      toast.error("광고 목록을 불러오는데 실패했습니다.");
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = (e) => {
    setSearchTerm(e.target.value);
    setCurrentPage(1);
  };

  const filteredAdvertisements = advertisements.filter(
    (ad) =>
      ad.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
      ad.content.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const indexOfLastItem = currentPage * itemsPerPage;
  const indexOfFirstItem = indexOfLastItem - itemsPerPage;
  const currentAdvertisements = filteredAdvertisements.slice(
    indexOfFirstItem,
    indexOfLastItem
  );
  const totalPages = Math.ceil(filteredAdvertisements.length / itemsPerPage);

  const handlePageChange = (pageNumber) => {
    setCurrentPage(pageNumber);
  };

  const handleCreate = () => {
    navigate("/admin/advertisements/create");
  };

  const handleEdit = (id) => {
    navigate(`/admin/advertisements/edit/${id}`);
  };

  const handleDelete = async (id) => {
    if (!window.confirm("정말 삭제하시겠습니까?")) {
      return;
    }

    try {
      const response = await advertisementApi.delete(id);
      if (response.success) {
        toast.success("광고가 삭제되었습니다.");
        fetchAdvertisements();
      }
    } catch (error) {
      console.error("광고 삭제 실패:", error);
      toast.error("광고 삭제에 실패했습니다.");
    }
  };

  const handleSendNotification = async (id, title) => {
    if (
      !window.confirm(
        `'${title}' 광고 알림을 발송하시겠습니까?\n마케팅 동의한 사용자에게만 전송됩니다.`
      )
    ) {
      return;
    }

    try {
      const response = await advertisementApi.sendNotification(id);
      if (response.success) {
        toast.success(response.message);
        fetchAdvertisements();
      }
    } catch (error) {
      console.error("광고 알림 발송 실패:", error);
      toast.error("광고 알림 발송에 실패했습니다.");
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return "-";
    const date = new Date(dateString);
    return date
      .toLocaleDateString("ko-KR", {
        year: "numeric",
        month: "2-digit",
        day: "2-digit",
      })
      .replace(/\. /g, "-")
      .replace(".", "");
  };

  if (loading) {
    return (
      <div className="admin-dashboard">
        <AdminSidebar />
        <div className="dashboard-main">
          <div className="loading">로딩중...</div>
        </div>
      </div>
    );
  }

  return (
    <div className="admin-dashboard">
      <AdminSidebar />

      <div className="dashboard-main">
        <div className="notice-container">
          <div className="notice-header">
            <h1>광고 관리</h1>
            <p className="notice-description">
              마케팅 동의 사용자에게 전송할 광고를 관리합니다
            </p>
          </div>

          <div className="notice-controls">
            <div className="search-box">
              <input
                type="text"
                placeholder="제목 또는 내용으로 검색..."
                value={searchTerm}
                onChange={handleSearch}
                className="search-input"
              />
              <span className="search-icon">🔍</span>
            </div>
            <button onClick={handleCreate} className="btn-write">
              ✏️ 광고 등록
            </button>
          </div>

          <div className="advertisement-stats">
            <div className="stat-item">
              <span className="stat-label">전체 광고</span>
              <span className="stat-value">{advertisements.length}개</span>
            </div>
            <div className="stat-item">
              <span className="stat-label">활성 광고</span>
              <span className="stat-value">
                {advertisements.filter((ad) => ad.active).length}개
              </span>
            </div>
            <div className="stat-item">
              <span className="stat-label">발송된 광고</span>
              <span className="stat-value">
                {advertisements.filter((ad) => ad.sentAt).length}개
              </span>
            </div>
          </div>

          <div className="notice-count">
            전체 {filteredAdvertisements.length}건
          </div>

          {currentAdvertisements.length === 0 ? (
            <div className="empty-state">
              <p>등록된 광고가 없습니다.</p>
            </div>
          ) : (
            <>
              <table className="notice-table">
                <thead>
                  <tr>
                    <th style={{ width: "80px" }}>번호</th>
                    <th>제목</th>
                    <th style={{ width: "120px" }}>작성자</th>
                    <th style={{ width: "120px" }}>작성일</th>
                    <th style={{ width: "250px" }}>관리</th>
                  </tr>
                </thead>
                <tbody>
                  {currentAdvertisements.map((ad, index) => (
                    <tr key={ad.id}>
                      <td>
                        {filteredAdvertisements.length -
                          (indexOfFirstItem + index)}
                      </td>
                      <td className="title-cell">
                        <span className="title-text">{ad.title}</span>
                        {!ad.active && (
                          <span className="status-badge inactive">비활성</span>
                        )}
                        {ad.sentAt && (
                          <span className="status-badge sent">발송완료</span>
                        )}
                      </td>
                      <td>관리자</td>
                      <td>{formatDate(ad.createdAt)}</td>
                      <td>
                        <div className="action-buttons">
                          {!ad.sentAt && ad.active && (
                            <button
                              className="btn-action btn-send"
                              onClick={() =>
                                handleSendNotification(ad.id, ad.title)
                              }
                            >
                              발송
                            </button>
                          )}
                          <button
                            className="btn-action btn-edit"
                            onClick={() => handleEdit(ad.id)}
                          >
                            수정
                          </button>
                          <button
                            className="btn-action btn-delete"
                            onClick={() => handleDelete(ad.id)}
                          >
                            삭제
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>

              {totalPages > 1 && (
                <div className="pagination">
                  <button
                    onClick={() => handlePageChange(1)}
                    disabled={currentPage === 1}
                    className="page-btn"
                  >
                    처음
                  </button>
                  <button
                    onClick={() => handlePageChange(currentPage - 1)}
                    disabled={currentPage === 1}
                    className="page-btn"
                  >
                    이전
                  </button>

                  {[...Array(totalPages)].map((_, i) => (
                    <button
                      key={i + 1}
                      onClick={() => handlePageChange(i + 1)}
                      className={`page-btn ${
                        currentPage === i + 1 ? "active" : ""
                      }`}
                    >
                      {i + 1}
                    </button>
                  ))}

                  <button
                    onClick={() => handlePageChange(currentPage + 1)}
                    disabled={currentPage === totalPages}
                    className="page-btn"
                  >
                    다음
                  </button>
                  <button
                    onClick={() => handlePageChange(totalPages)}
                    disabled={currentPage === totalPages}
                    className="page-btn"
                  >
                    마지막
                  </button>
                </div>
              )}
            </>
          )}
        </div>
      </div>
    </div>
  );
};

export default AdvertisementList;
