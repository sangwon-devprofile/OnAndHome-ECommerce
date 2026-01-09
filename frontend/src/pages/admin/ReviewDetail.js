import axios from "axios";
import React, { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import AdminSidebar from "../../components/layout/AdminSidebar";
import "./ReviewDetail.css";

const ReviewDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const API_BASE_URL = process.env.REACT_APP_API_URL || "http://localhost:8080";

  const [review, setReview] = useState(null);
  const [replies, setReplies] = useState([]);
  const [loading, setLoading] = useState(true);

  // 수정 상태
  const [editingReplyId, setEditingReplyId] = useState(null);
  const [editedContent, setEditedContent] = useState("");

  // 등록 상태
  const [replyContent, setReplyContent] = useState("");
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    fetchReviewDetail();
  }, [id]);

  const fetchReviewDetail = async () => {
    setLoading(true);
    try {
      const response = await axios.get(
        `${API_BASE_URL}/api/admin/reviews/${id}`,
        {
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${localStorage.getItem("accessToken")}`,
          },
        }
      );

      if (response.data && response.data.success) {
        setReview(response.data.review);
        setReplies(response.data.replies || []);
      }
    } catch (error) {
      alert("리뷰 정보를 불러오는데 실패했습니다.");
      navigate("/admin/reviews");
    } finally {
      setLoading(false);
    }
  };

  // 답글 등록
  const handleSubmitReply = async () => {
    if (!replyContent.trim()) {
      alert("답글 내용을 입력해주세요.");
      return;
    }

    if (!window.confirm("답글을 등록하시겠습니까?")) return;

    setSubmitting(true);

    try {
      const response = await axios.post(
        `${API_BASE_URL}/api/admin/reviews/${id}/reply`,
        { content: replyContent, responder: "Admin" },
        {
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${localStorage.getItem("accessToken")}`,
          },
        }
      );

      if (response.data && response.data.success) {
        alert("답글이 등록되었습니다.");
        setReplyContent("");
        fetchReviewDetail();
      }
    } catch (error) {
      alert("답글 등록 중 오류가 발생했습니다.");
    } finally {
      setSubmitting(false);
    }
  };

  // 답글 삭제
  const handleDeleteReply = async (replyId) => {
    if (!window.confirm("답글을 삭제하시겠습니까?")) return;

    try {
      const response = await axios.delete(
        `${API_BASE_URL}/api/admin/reviews/reply/${replyId}`,
        {
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${localStorage.getItem("accessToken")}`,
          },
        }
      );

      if (response.data && response.data.success) {
        alert("답글이 삭제되었습니다.");
        fetchReviewDetail();
      }
    } catch (error) {
      alert("답글 삭제 중 오류가 발생했습니다.");
    }
  };

  // 답글 수정 시작
  const startEditReply = (reply) => {
    setEditingReplyId(reply.id);
    setEditedContent(reply.content);
  };

  // 수정 취소
  const cancelEdit = () => {
    setEditingReplyId(null);
    setEditedContent("");
  };

  // 수정 저장
  const saveEditedReply = async (replyId) => {
    if (!editedContent.trim()) {
      alert("내용을 입력해주세요.");
      return;
    }

    if (!window.confirm("답글을 수정하시겠습니까?")) return;

    try {
      const response = await axios.put(
        `${API_BASE_URL}/api/admin/reviews/reply/${replyId}`,
        { content: editedContent },
        {
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${localStorage.getItem("accessToken")}`,
          },
        }
      );

      if (response.data && response.data.success) {
        alert("답글이 수정되었습니다.");
        setEditingReplyId(null);
        setEditedContent("");
        fetchReviewDetail();
      }
    } catch {
      alert("답글 수정 중 오류 발생");
    }
  };

  // 상품 상세 페이지 이동 함수 추가
  const handleGoToProduct = () => {
    if (review?.productId) {
      window.location.href = `/products/${review.productId}`;
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return "-";
    try {
      const date = new Date(dateString);
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(
        2,
        "0"
      )}-${String(date.getDate()).padStart(2, "0")} ${String(
        date.getHours()
      ).padStart(2, "0")}:${String(date.getMinutes()).padStart(2, "0")}`;
    } catch {
      return dateString;
    }
  };

  const renderStars = (rating) => {
    return "★".repeat(rating) + "☆".repeat(5 - rating);
  };

  if (loading) {
    return (
      <div className="admin-review-detail">
        <AdminSidebar />
        <div className="review-detail-main">
          <div className="loading">로딩 중...</div>
        </div>
      </div>
    );
  }

  if (!review) {
    return (
      <div className="admin-review-detail">
        <AdminSidebar />
        <div className="review-detail-main">
          <div className="error">리뷰를 찾을 수 없습니다.</div>
        </div>
      </div>
    );
  }

  return (
    <div className="admin-review-detail">
      <AdminSidebar />

      <div className="review-detail-main">
        <div className="page-header">
          <h1>리뷰 상세</h1>
          <button
            className="btn-back"
            onClick={() => navigate("/admin/reviews")}
          >
            목록으로
          </button>
        </div>

        {/* 리뷰 정보 카드 */}
        <div className="review-info-card">
          <table className="detail-table">
            <tbody>
              <tr>
                <th>번호</th>
                <td>{review.id}</td>
              </tr>

              <tr>
                <th>작성자</th>
                <td>{review.author || review.username}</td>
              </tr>

              <tr>
                <th>작성일자</th>
                <td>{formatDate(review.createdAt)}</td>
              </tr>

              <tr>
                <th>상품명</th>
                <td>
                  {review.productName ? (
                    <span className="product-link" onClick={handleGoToProduct}>
                      {review.productName} 🔗
                    </span>
                  ) : (
                    "-"
                  )}
                </td>
              </tr>

              <tr>
                <th>평점</th>
                <td>
                  <span className="stars">{renderStars(review.rating)}</span>
                  <span className="rating-number">{review.rating}/5</span>
                </td>
              </tr>

              <tr>
                <th>리뷰 내용</th>
                <td className="content-cell">
                  <div className="content-box">{review.content}</div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        {/* 답글 목록 */}
        <div className="replies-section">
          <h2>답글 목록 ({replies.length})</h2>

          {replies.length > 0 ? (
            <div className="replies-list">
              {replies.map((reply) => (
                <div key={reply.id} className="reply-item">
                  <div className="reply-header">
                    <div className="reply-meta">
                      <span className="reply-author">{reply.author}</span>
                      <span className="reply-date">
                        {formatDate(reply.createdAt)}
                      </span>
                    </div>

                    {editingReplyId === reply.id ? (
                      <></>
                    ) : (
                      <div className="reply-actions">
                        <button
                          className="btn-edit"
                          onClick={() => startEditReply(reply)}
                        >
                          수정
                        </button>
                        <button
                          className="btn-delete-reply"
                          onClick={() => handleDeleteReply(reply.id)}
                        >
                          삭제
                        </button>
                      </div>
                    )}
                  </div>

                  <div className="reply-content">
                    {editingReplyId === reply.id ? (
                      <>
                        <textarea
                          className="reply-edit-textarea"
                          value={editedContent}
                          onChange={(e) => setEditedContent(e.target.value)}
                        />
                        <div className="reply-edit-actions">
                          <button className="btn-cancel" onClick={cancelEdit}>
                            취소
                          </button>
                          <button
                            className="btn-save"
                            onClick={() => saveEditedReply(reply.id)}
                          >
                            저장
                          </button>
                        </div>
                      </>
                    ) : (
                      reply.content
                    )}
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <div className="no-replies">등록된 답글이 없습니다.</div>
          )}
        </div>

        {/* 답글 작성 */}
        <div className="reply-form">
          <h2>답글 작성</h2>
          <textarea
            className="reply-textarea"
            value={replyContent}
            onChange={(e) => setReplyContent(e.target.value)}
            placeholder="답글 내용을 입력하세요"
            rows="5"
            disabled={submitting}
          />
          <div className="reply-actions">
            <button
              className="btn-submit"
              onClick={handleSubmitReply}
              disabled={submitting || !replyContent.trim()}
            >
              {submitting ? "등록 중..." : "답글 등록"}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ReviewDetail;
