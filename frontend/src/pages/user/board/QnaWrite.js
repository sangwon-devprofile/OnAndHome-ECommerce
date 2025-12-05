import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useSelector } from "react-redux";
import { qnaAPI } from "../../../api";
import "./QnaWrite.css";

const QnaWrite = () => {
  const navigate = useNavigate();
  const { isAuthenticated, user } = useSelector((state) => state.user);

  const [formData, setFormData] = useState({
    title: "",
    question: "",
    isPrivate: false,
  });

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: type === "checkbox" ? checked : value,
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!isAuthenticated) {
      alert("로그인이 필요합니다.");
      navigate("/login");
      return;
    }

    if (!formData.title.trim()) {
      alert("제목을 입력해주세요.");
      return;
    }

    if (!formData.question.trim()) {
      alert("내용을 입력해주세요.");
      return;
    }

    try {
      const response = await qnaAPI.createQna({
        title: formData.title,
        question: formData.question,
        isPrivate: formData.isPrivate,
        userId: user.id,
        writer: user.username || user.userId,
      });

      if (response.success) {
        alert("문의가 등록되었습니다.");
        navigate("/qna");
      } else {
        alert(response.message || "문의 등록에 실패했습니다.");
      }
    } catch (error) {
      console.error("문의 작성 오류:", error);
      alert("문의 작성 중 오류가 발생했습니다.");
    }
  };

  return (
    <div className="qna-write-container">
      <div className="qna-write-inner">
        <h2 className="page-title">문의하기</h2>

        <form onSubmit={handleSubmit} className="qna-write-form">
          <div className="form-group">
            <label htmlFor="title">제목</label>
            <input
              type="text"
              id="title"
              name="title"
              value={formData.title}
              onChange={handleChange}
              placeholder="제목을 입력하세요"
              className="form-input"
            />
          </div>

          <div className="form-group">
            <label htmlFor="question">내용</label>
            <textarea
              id="question"
              name="question"
              value={formData.question}
              onChange={handleChange}
              placeholder="문의 내용을 입력하세요"
              className="form-textarea"
              rows="10"
            />
          </div>

          <div className="form-group checkbox-group">
            <label className="checkbox-label">
              <input
                type="checkbox"
                name="isPrivate"
                checked={formData.isPrivate}
                onChange={handleChange}
              />
              <span>비밀글로 작성</span>
            </label>
          </div>

          <div className="form-actions">
            <button
              type="button"
              className="btn btn-cancel"
              onClick={() => navigate("/qna")}
            >
              취소
            </button>
            <button type="submit" className="btn btn-submit">
              등록
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default QnaWrite;
