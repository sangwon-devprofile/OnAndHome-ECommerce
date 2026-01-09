/**
 * 장바구니 사이드 패널 컴포넌트
 *
 * ========================================
 * 이 컴포넌트 개요
 * ========================================
 * - 파일 위치: src/components/cart/CartSidePanel.js
 * - 역할: 화면 우측에서 슬라이드되는 장바구니 미니 패널
 * - 트리거: CartFloatingButton 클릭 시 열림
 *
 * ========================================
 * 주요 기능
 * ========================================
 * 1. 장바구니 아이템 목록 표시
 * 2. 수량 증가/감소 (+/- 버튼)
 * 3. 개별 아이템 삭제
 * 4. 총 금액 계산 (할인가 적용)
 * 5. 장바구니 페이지로 이동
 *
 * ========================================
 * Props
 * ========================================
 * | prop명      | 타입      | 설명                              |
 * |-------------|-----------|----------------------------------|
 * | isOpen      | boolean   | 패널 열림/닫힘 상태                 |
 * | onClose     | function  | 패널 닫기 콜백                     |
 * | onCartUpdate| function  | 장바구니 변경 시 부모에게 알림        |
 *
 * ========================================
 * 연동 API (cartAPI)
 * ========================================
 * - getCartItems(): 장바구니 목록 조회
 * - updateQuantity(cartItemId, quantity): 수량 변경
 * - removeItem(cartItemId): 아이템 삭제
 *
 * ========================================
 * UI 구조
 * ========================================
 * ┌─────────────────────────────────────┐
 * │[Header] 장바구니 (3)    [접기][X] │
 * ├─────────────────────────────────────┤
 * │┌─────────────────────────────────┐│
 * ││[이미지] 상품명             ││
 * ││         10,000원→8,000원  ││
 * ││         수량 [-] 2 [+]     ││
 * ││         소계: 16,000원    ││
 * │└─────────────────────────────────┘│
 * │┌─────────────────────────────────┐│
 * ││... 다른 아이템들 ...        ││
 * │└─────────────────────────────────┘│
 * ├─────────────────────────────────────┤
 * │[Footer] 총 금액: 50,000원     │
 * │         [장바구니 가기]         │
 * └─────────────────────────────────────┘
 */

import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { cartAPI } from "../../../api";
import "./CartSidePanel.css";

/**
 * CartSidePanel 컴포넌트
 *
 * props:
 * - isOpen: 패널 열림 상태
 * - onClose: 닫기 버튼 클릭 시 호출되는 콜백
 * - onCartUpdate: 장바구니 변경 시 부모 컴포넌트에 알리는 콜백
 */
const CartSidePanel = ({ isOpen, onClose, onCartUpdate }) => {
  // React Router의 프로그래매틱 네비게이션
  const navigate = useNavigate();

  // ========================================
  // 상태 (State) 정의
  // ========================================

  /**
   * 장바구니 아이템 배열
   * 구조: [{ id, product: {...}, quantity }, ...]
   */
  const [cartItems, setCartItems] = useState([]);

  /**
   * API 로딩 중 표시
   * true일 때 "로딩 중..." 메시지 표시
   */
  const [loading, setLoading] = useState(false);

  /**
   * 패널 최소화 상태 (현재는 사용하지 않고 닫기로만 동작)
   */
  const [isMinimized, setIsMinimized] = useState(false);

  // ========================================
  // Effect Hooks
  // ========================================

  /**
   * 패널이 닫힐 때 minimize 상태 초기화
   *
   * 의존성: [isOpen]
   * - 패널이 닫히면(isOpen: false) isMinimized를 false로 리셋
   * - 다음에 열릴 때 항상 펼쳐진 상태로 시작
   */
  useEffect(() => {
    if (!isOpen) {
      setIsMinimized(false);
    }
  }, [isOpen]);

  /**
   * 패널이 열릴 때 장바구니 목록 로드
   *
   * 의존성: [isOpen]
   * - 패널이 열리면(isOpen: true) API 호출하여 최신 아이템 로드
   * - 매번 열릴 때마다 새로 조회 (캐시 없이)
   */
  useEffect(() => {
    if (isOpen) {
      loadCartItems();
    }
  }, [isOpen]);

  // ========================================
  // API 호출 함수
  // ========================================

  /**
   * 장바구니 목록 로드
   *
   * 처리 흐름:
   * 1. loading 상태를 true로 설정
   * 2. cartAPI.getCartItems() 호출
   * 3. 성공 시 cartItems 상태 업데이트
   * 4. 실패 시 빈 배열로 설정
   * 5. finally에서 loading을 false로 설정
   */
  const loadCartItems = async () => {
    setLoading(true);
    try {
      const response = await cartAPI.getCartItems();
      console.log("=== 장바구니 API 응답 ===", response);

      if (response.success && response.data) {
        setCartItems(response.data);
      } else {
        setCartItems([]);
      }
    } catch (error) {
      console.error("장바구니 조회 오류:", error);
      setCartItems([]);
    } finally {
      setLoading(false);
    }
  };

  // ========================================
  // 이벤트 핸들러
  // ========================================

  /**
   * 아이템 삭제 핸들러
   *
   * 처리 흐름:
   * 1. 삭제 확인 다이얼로그 표시
   * 2. 확인 시 cartAPI.removeItem() 호출
   * 3. 성공 시 목록 새로고침 + 부모에게 알림
   * 4. 실패 시 에러 알림
   *
   * cartItemId: 삭제할 장바구니 아이템의 PK
   */
  const handleRemoveItem = async (cartItemId) => {
    // 삭제 확인 (취소 시 함수 종료)
    if (!window.confirm("이 상품을 장바구니에서 삭제하시겠습니까?")) {
      return;
    }

    try {
      const response = await cartAPI.removeItem(cartItemId);
      if (response.success) {
        // 삭제 성공: 목록 새로고침
        await loadCartItems();
        // 부모 컴포넌트에 변경 알림 (배지 숫자 갱신용)
        if (onCartUpdate) onCartUpdate();
      } else {
        alert(response.message || "삭제에 실패했습니다.");
      }
    } catch (error) {
      console.error("삭제 오류:", error);
      alert("삭제 중 오류가 발생했습니다.");
    }
  };

  /**
   * 수량 변경 핸들러
   *
   * 처리 흐름:
   * 1. 새 수량 계산 (현재 수량 + 변경값)
   * 2. 1 미만이면 경고 후 종료
   * 3. cartAPI.updateQuantity() 호출
   * 4. 성공 시 목록 새로고침 + 부모에게 알림
   *
   * cartItemId: 장바구니 아이템 PK
   * currentQuantity: 현재 수량
   * change: 변경값 (+1 또는 -1)
   */
  const handleQuantityChange = async (cartItemId, currentQuantity, change) => {
    const newQuantity = currentQuantity + change;

    // 최소 수량 체크
    if (newQuantity < 1) {
      alert("수량은 1개 이상이어야 합니다.");
      return;
    }

    try {
      const response = await cartAPI.updateQuantity(cartItemId, newQuantity);
      if (response.success) {
        // 수량 변경 성공: 목록 새로고침
        await loadCartItems();
        // 부모 컴포넌트에 변경 알림
        if (onCartUpdate) onCartUpdate();
      } else {
        alert(response.message || "수량 변경에 실패했습니다.");
      }
    } catch (error) {
      console.error("수량 변경 오류:", error);
      alert("수량 변경 중 오류가 발생했습니다.");
    }
  };

  /**
   * 접기 버튼 핸들러
   * 현재는 최소화 대신 완전히 닫기로 동작
   */
  const toggleMinimize = () => {
    onClose();  // 접기 버튼을 클릭하면 완전히 닫기
  };

  /**
   * 오버레이 클릭 핸들러
   * 패널 바깥 영역(어두운 부분) 클릭 시 패널 닫기
   */
  const handleOverlayClick = () => {
    if (!isMinimized) {
      onClose();
    }
  };

  /**
   * 장바구니 페이지로 이동
   * "장바구니 가기" 버튼 클릭 시 호출
   */
  const handleGoToCart = () => {
    navigate("/cart");  // 장바구니 페이지로 라우팅
    onClose();          // 패널 닫기
  };

  // ========================================
  // 유틸리티 함수
  // ========================================

  /**
   * 가격 포맷팅(천 단위 콤마)
   *
   * 예시:
   * - formatPrice(10000) → "10,000"
   * - formatPrice(null) → "0"
   *
   * price: 숫자 또는 null/undefined
   * return: 포맷팅된 문자열
   */
  const formatPrice = (price) => {
    if (!price) return "0";
    return price.toLocaleString();
  };

  /**
   * 할인율 계산
   *
   * 공식: ((정가 - 할인가) / 정가) × 100
   *
   * 예시:
   * - calculateDiscountRate(10000, 8000) → 20
   * - calculateDiscountRate(10000, null) → 0
   *
   * originalPrice: 정가
   * salePrice: 할인가
   * return: 할인율 (정수, %)
   */
  const calculateDiscountRate = (originalPrice, salePrice) => {
    // 유효하지 않은 경우 0 반환
    if (!originalPrice || !salePrice || salePrice >= originalPrice) return 0;
    // 정수로 반올림하여 정수로 반환
    return Math.round(((originalPrice - salePrice) / originalPrice) * 100);
  };

  /**
   * 상품 이미지 URL 생성
   *
   * 다양한 이미지 경로 형식을 처리:
   * 1. 완전한 URL (http://, https://) → 그대로 사용
   * 2. uploads/ 경로 → 백엔드 URL 붙이기
   * 3. 파일명만 있는 경우 → /product_img/{name}.jpg
   * 4. 이미지 없음 → 기본 이미지
   *
   * product: 상품 객체
   * return: 이미지 URL 문자열
   */
  const getImageUrl = (product) => {
    // 이미지 경로 추출 (우선순위: thumbnailImage > imageUrl > mainImage)
    let imagePath =
      product?.thumbnailImage || product?.imageUrl || product?.mainImage;

    // 이미지 경로가 없으면 기본 이미지 반환
    if (!imagePath) {
      return "/images/no-image.png";
    }

    // Case 1: 완전한 URL인 경우 그대로 반환
    if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) {
      return imagePath;
    }

    // Case 2: uploads 폴더 경로인 경우 백엔드 URL 붙이기
    if (imagePath.startsWith("uploads/") || imagePath.startsWith("/uploads/")) {
      return `http://localhost:8080${
        imagePath.startsWith("/") ? "" : "/"
      }${imagePath}`;
    }

    // Case 3: 파일명만 있는 경우 (확장자 없이)
    // 예: "product001" → "/product_img/product001.jpg"
    if (!imagePath.includes("/") && !imagePath.startsWith("http")) {
      return `/product_img/${imagePath}.jpg`;
    }
  
    // 그 외의 경우 그대로 반환
    return imagePath;
  };

  /**
   * 총 금액 계산
   *
   * 모든 아이템의 (최종가격 × 수량) 합계
   * 할인가가 있으면 할인가, 없으면 정가 사용
   *
   * return: 총 금액 (숫자)
   */
  const getTotalPrice = () => {
    return cartItems.reduce((total, item) => {
      const originalPrice = item.product?.price || 0;
      const salePrice = item.product?.salePrice || 0;
      // 할인가가 0보다 크면 할인가 사용, 아니면 정가 사용
      const finalPrice = salePrice > 0 ? salePrice : originalPrice;
      return total + finalPrice * (item.quantity || 0);
    }, 0);
  };

  // ========================================
  // 렌더링
  // ========================================

  // 패널이 닫혀있으면 아무것도 렌더링하지 않음
  if (!isOpen) return null;

  return (
    <>
      {/* ========================================
          오버레이 (배경 어둡게)
          - 패널이 열려있고 최소화되지 않았을 때만 표시
          - 클릭 시 패널 닫기
          ======================================== */}
      {!isMinimized && (
        <div
          className={`cart-overlay ${isOpen ? "active" : ""}`}
          onClick={handleOverlayClick}
        />
      )}

      {/* ========================================
          사이드 패널 본체
          - active 클래스: 슬라이드 인 애니메이션
          - minimized 클래스: 최소화 상태 스타일
          ======================================== */}
      <div
        className={`cart-side-panel ${isOpen ? "active" : ""} ${
          isMinimized ? "minimized" : ""
        }`}
      >
        {/* ========================================
            헤더 영역
            - 제목 + 아이템 개수
            - 접기/닫기 버튼
            ======================================== */}
        <div className="cart-panel-header">
          <div className="cart-header-left">
            <h2>장바구니 ({cartItems.length})</h2>
            <button className="minimize-btn" onClick={toggleMinimize}>
              {isMinimized ? "펼치기" : "접기"}
            </button>
          </div>
          <button className="close-btn" onClick={onClose}>
            ✕
          </button>
        </div>

        {/* ========================================
            콘텐츠 영역 (아이템 목록)
            - 최소화 상태가 아닐 때만 표시
            - 로딩 중 / 빈 장바구니 / 아이템 목록 분기
            ======================================== */}
        {!isMinimized && (
          <div className="cart-panel-content">
            {loading ? (
              // 로딩 상태
              <div className="loading-cart">
                <p>로딩 중...</p>
              </div>
            ) : cartItems.length === 0 ? (
              // 빈 장바구니
              <div className="empty-cart">
                <p>장바구니가 비어있습니다.</p>
              </div>
            ) : (
              // 아이템 목록
              <div className="cart-items-list">
                {cartItems.map((item) => {
                  // 상품 정보 추출 (안전하게 기본값 설정)
                  const product = item.product || {};
                  const originalPrice = product.price || 0;
                  const salePrice = product.salePrice || 0;
                  const finalPrice = salePrice > 0 ? salePrice : originalPrice;
                  const discountRate = calculateDiscountRate(
                    originalPrice,
                    salePrice
                  );
                  const quantity = item.quantity || 0;
                  const itemTotal = finalPrice * quantity;
                  const productName = product.name || "상품명 없음";

                  return (
                    <div key={item.id} className="cart-item-card">
                      {/* 삭제 버튼 (우측 상단 X) */}
                      <button
                        className="remove-item-btn"
                        onClick={() => handleRemoveItem(item.id)}
                      >
                        ✕
                      </button>

                      {/* 상품 이미지 */}
                      <div className="item-image">
                        <img
                          src={getImageUrl(product)}
                          alt={productName}
                          onError={(e) => {
                            // 이미지 로드 실패 시 기본 이미지로 대체
                            e.target.src = "/images/no-image.png";
                          }}
                        />
                      </div>

                      {/* 상품 정보 */}
                      <div className="item-info">
                        <h3 className="item-name">{productName}</h3>

                        {/* 가격 표시 영역 */}
                        <div className="item-price-container">
                          {discountRate > 0 ? (
                            // 할인 상품인 경우
                            <>
                              <div className="original-price">
                                {formatPrice(originalPrice)}원
                              </div>
                              <div className="sale-price-row">
                                <span className="sale-price">
                                  {formatPrice(salePrice)}원
                                </span>
                                <span className="discount-badge">
                                  {discountRate}% 할인
                                </span>
                              </div>
                            </>
                          ) : (
                            // 할인 없는 경우
                            <div className="item-price">
                              {formatPrice(originalPrice)}원
                            </div>
                          )}
                        </div>

                        {/* 수량 및 소계 */}
                        <div className="item-details">
                          {/* 수량 조절 버튼 */}
                          <div className="item-quantity-control">
                            <span className="label">수량</span>
                            <div className="quantity-buttons">
                              <button
                                className="quantity-btn minus"
                                onClick={() =>
                                  handleQuantityChange(item.id, quantity, -1)
                                }
                                disabled={quantity <= 1}
                              >
                                −
                              </button>
                              <span className="quantity-value">{quantity}</span>
                              <button
                                className="quantity-btn plus"
                                onClick={() =>
                                  handleQuantityChange(item.id, quantity, 1)
                                }
                              >
                                +
                              </button>
                            </div>
                          </div>

                          {/* 소계 (아이템별 금액) */}
                          <div className="item-subtotal">
                            <span className="label">소계</span>
                            <strong>{formatPrice(itemTotal)}원</strong>
                          </div>
                        </div>
                      </div>
                    </div>
                  );
                })}
              </div>
            )}
          </div>
        )}

        {/* ========================================
            푸터 영역
            - 총 금액 표시
            - 장바구니 가기 버튼
            - 최소화 상태가 아닐 때만 표시
            ======================================== */}
        {!isMinimized && (
          <div className="cart-panel-footer">
            <div className="total-price">
              <span>총 금액</span>
              <strong>{formatPrice(getTotalPrice())}원</strong>
            </div>
            <button
              className="go-to-cart-btn"
              onClick={handleGoToCart}
              disabled={cartItems.length === 0}
            >
              장바구니 가기
            </button>
          </div>
        )}
      </div>
    </>
  );
};

export default CartSidePanel;


/*
 * ========================================
 * 컴포넌트 상호작용 흐름
 * ========================================
 *
 * [CartFloatingButton.js]
 *        │
 *        ├─클릭: setIsPanelOpen(true)
 *        ▼
 * [CartSidePanel.js]
 *        │
 *        ├──→ isOpen=true → useEffect 트리거
 *        │                       │
 *        │                       ▼
 *        │              loadCartItems()
 *        │                       │
 *        │                       ▼
 *        │              cartAPI.getCartItems()
 *        │                       │
 *        │                       ▼
 *        │              setCartItems(response.data)
 *        │                       │
 *        │                       ▼
 *        │              UI 렌더링 (아이템 목록)
 *        │
 *        ├──→ 수량 변경 버튼 클릭
 *        │       │
 *        │       ▼
 *        │  handleQuantityChange()
 *        │       │
 *        │       ▼
 *        │  cartAPI.updateQuantity()
 *        │       │
 *        │       ▼
 *        │  loadCartItems() → UI 갱신
 *        │       │
 *        │       ▼
 *        │  onCartUpdate() → 부모 컴포넌트 알림
 *        │
 *        ├──→ 삭제 버튼 클릭
 *        │       │
 *        │       ▼
 *        │  handleRemoveItem()
 *        │       │
 *        │       ▼
 *        │  confirm() → cartAPI.removeItem()
 *        │       │
 *        │       ▼
 *        │  loadCartItems() → UI 갱신
 *        │
 *        └──→ 장바구니 가기 버튼 클릭
 *                 │
 *                 ▼
 *            navigate("/cart") + onClose()
 *
 *
 * ========================================
 * CSS 클래스 구조
 * ========================================
 *
 * .cart-overlay          - 배경 오버레이 (반투명 검정)
 *   └── .active          - 표시 상태
 *
 * .cart-side-panel       - 패널 컨테이너
 *   └── .active          - 슬라이드 인 상태
 *   └── .minimized       - 최소화 상태
 *   │
 *   ├── .cart-panel-header   - 헤더
 *   │  ├── .cart-header-left
 *   │  │  ├── h2           - 제목 + 개수
 *   │  │  └── .minimize-btn - 접기 버튼
 *   │  └── .close-btn       - 닫기 버튼
 *   │
 *   ├── .cart-panel-content  - 콘텐츠 영역
 *   │  ├── .loading-cart    - 로딩 상태
 *   │  ├── .empty-cart      - 빈 장바구니
 *   │  └── .cart-items-list - 아이템 목록
 *   │      └── .cart-item-card  - 개별 아이템
 *   │          ├── .remove-item-btn   - 삭제 버튼
 *   │          ├── .item-image        - 이미지
 *   │          └── .item-info         - 정보
 *   │              ├── .item-name     - 상품명
 *   │              ├── .item-price-container - 가격
 *   │              │  ├── .original-price   - 정가
 *   │              │  ├── .sale-price       - 할인가
 *   │              │  └── .discount-badge   - 할인율
 *   │              └── .item-details  - 수량/소계
 *   │                  ├── .item-quantity-control
 *   │                  └── .item-subtotal
 *   │
 *   └── .cart-panel-footer   - 푸터
 *       ├── .total-price     - 총 금액
 *       └── .go-to-cart-btn  - 이동 버튼
 *
 *
 * ========================================
 * 이미지 URL 처리 케이스
 * ========================================
 *
 * ┌─────────────────────────────────────┬────────────────────────────────────┐
 * │입력값                       │출력값                            │
 * ├─────────────────────────────────────┼────────────────────────────────────┤
 * │null / undefined             │/images/no-image.png              │
 * │https://example.com/img.jpg  │https://example.com/img.jpg       │
 * │uploads/product/img.jpg      │http://localhost:8080/uploads/... │
 * │/uploads/product/img.jpg     │http://localhost:8080/uploads/... │
 * │product001                   │/product_img/product001.jpg       │
 * └─────────────────────────────────────┴────────────────────────────────────┘
 *
 */
