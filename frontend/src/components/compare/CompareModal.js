import React, { useState } from "react";
import { useSelector, useDispatch } from "react-redux";
import {
  removeFromCompare,
  clearCompare,
} from "../../store/slices/compareSlice";
import CompareSelectModal from "./CompareSelectModal";
import "./CompareModal.css";

const CompareModal = ({ isOpen, onClose }) => {
  const dispatch = useDispatch();
  const compareItems = useSelector((state) => state.compare.items);
  const [isMinimized, setIsMinimized] = useState(false);
  const [isSelectModalOpen, setIsSelectModalOpen] = useState(false);

  const handleRemoveItem = (productId) => {
    dispatch(removeFromCompare(productId));
  };

  const handleClearAll = () => {
    if (window.confirm("모든 비교 상품을 삭제하시겠습니까?")) {
      dispatch(clearCompare());
    }
  };

  const toggleMinimize = () => {
    setIsMinimized(!isMinimized);
  };

  const handleOverlayClick = () => {
    if (!isMinimized) {
      onClose();
    }
  };

  const handleAddProduct = () => {
    if (compareItems.length >= 4) {
      alert("최대 4개 상품까지 비교할 수 있습니다.");
      return;
    }
    setIsSelectModalOpen(true);
  };

  const formatPrice = (price) => {
    if (!price) return "0";
    return price.toLocaleString();
  };

  const getImageUrl = (imagePath) => {
    console.log("원본 imagePath:", imagePath);

    if (!imagePath) return "/images/no-image.png";

    // uploads/ 경로면 백엔드 서버에서 가져오기
    if (imagePath.startsWith("uploads/") || imagePath.startsWith("/uploads/")) {
      return `http://localhost:8080${
        imagePath.startsWith("/") ? "" : "/"
      }${imagePath}`;
    }

    // 짧은 이름이면 public/product_img/ 폴더에서 가져오기
    if (!imagePath.includes("/") && !imagePath.startsWith("http")) {
      return `/product_img/${imagePath}.jpg`;
    }

    return imagePath;
  };

  if (!isOpen) return null;

  return (
    <>
      {/* 오버레이 - 접혔을 때는 표시 안 함 */}
      {!isMinimized && (
        <div
          className={`compare-overlay ${isOpen ? "active" : ""}`}
          onClick={handleOverlayClick}
        />
      )}

      {/* 비교 모달 */}
      <div
        className={`compare-modal ${isOpen ? "active" : ""} ${
          isMinimized ? "minimized" : ""
        }`}
      >
        {/* 헤더 */}
        <div className="compare-modal-header">
          <div className="compare-header-left">
            <h2>상품 비교 ({compareItems.length}/4)</h2>
            <button className="minimize-btn" onClick={toggleMinimize}>
              {isMinimized ? "펼치기" : "접기"}
            </button>
          </div>
          <div className="compare-header-right">
            {!isMinimized && compareItems.length > 0 && (
              <button className="clear-all-btn" onClick={handleClearAll}>
                전체 삭제
              </button>
            )}
            <button className="close-btn" onClick={onClose}>
              ✕
            </button>
          </div>
        </div>

        {/* 비교 상품 목록 */}
        {!isMinimized && (
          <div className="compare-modal-content">
            <div className="compare-products-grid">
              {/* 비교 상품 카드들 */}
              {compareItems.map((product) => (
                <div key={product.id} className="compare-product-card">
                  <button
                    className="remove-product-btn"
                    onClick={() => handleRemoveItem(product.id)}
                  >
                    ✕
                  </button>
                  <div className="compare-product-image">
                    <img
                      src={getImageUrl(product.image)}
                      alt={product.name}
                      onError={(e) => {
                        e.target.src = "/images/placeholder.png";
                      }}
                    />
                  </div>
                  <div className="compare-product-info">
                    <h3>{product.name}</h3>
                    <p className="compare-product-price">
                      {formatPrice(product.price)}원
                    </p>
                  </div>
                  <div className="compare-product-specs">
                    <div className="spec-item">
                      <span className="spec-label">카테고리</span>
                      <span className="spec-value">
                        {product.category || "-"}
                      </span>
                    </div>
                    <div className="spec-item">
                      <span className="spec-label">브랜드</span>
                      <span className="spec-value">{product.brand || "-"}</span>
                    </div>
                    <div className="spec-item">
                      <span className="spec-label">재고</span>
                      <span className="spec-value">{product.stock || 0}개</span>
                    </div>
                  </div>
                </div>
              ))}

              {/* 빈 슬롯 */}
              {[...Array(4 - compareItems.length)].map((_, index) => (
                <div
                  key={`empty-${index}`}
                  className="compare-product-card empty"
                  onClick={handleAddProduct}
                  style={{ cursor: "pointer" }}
                >
                  <div className="empty-slot">
                    <span className="plus-icon">+</span>
                    <p>비교하고 싶은 제품을 최대 4개까지 선택해주세요.</p>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}
      </div>

      {/* 상품 선택 모달 */}
      <CompareSelectModal
        isOpen={isSelectModalOpen}
        onClose={() => setIsSelectModalOpen(false)}
      />
    </>
  );
};

export default CompareModal;
