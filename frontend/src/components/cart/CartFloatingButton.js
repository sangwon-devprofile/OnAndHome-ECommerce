import React, { useState, useEffect } from "react";
import { useSelector } from "react-redux";
import { cartAPI } from "../../api";
import CartSidePanel from "./CartSidePanel";
import "./CartFloatingButton.css";

const CartFloatingButton = () => {
  const [cartCount, setCartCount] = useState(0);
  const [isPanelOpen, setIsPanelOpen] = useState(false);
  const { isAuthenticated } = useSelector((state) => state.user);

  // 장바구니 개수 로드
  const loadCartCount = async () => {
    // 로그인하지 않은 경우 API 호출하지 않음
    if (!isAuthenticated) {
      setCartCount(0);
      return;
    }

    try {
      const response = await cartAPI.getCartCount();
      if (response.success) {
        setCartCount(response.data || response.count || 0);  // data 또는 count 필드 확인
      }
    } catch (error) {
      console.debug('장바구니 개수 조회 실패:', error.message);
      setCartCount(0);
    }
  };

  useEffect(() => {
    loadCartCount();
    
    // 로그인한 경우에만 5초마다 장바구니 개수 갱신
    if (isAuthenticated) {
      const interval = setInterval(() => {
        loadCartCount();
      }, 5000);

      return () => clearInterval(interval);
    }
  }, [isAuthenticated]);

  const handleClick = () => {
    setIsPanelOpen(true);
  };

  const handleClose = () => {
    setIsPanelOpen(false);
    loadCartCount(); // 패널 닫을 때 개수 갱신
  };

  return (
    <>
      <div className="cart-floating-btn" onClick={handleClick}>
        <div className="cart-icon">
          <svg
            width="24"
            height="24"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="2"
          >
            <circle cx="9" cy="21" r="1" />
            <circle cx="20" cy="21" r="1" />
            <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6" />
          </svg>
        </div>
        {cartCount > 0 && (
          <div className="cart-count">{cartCount}</div>
        )}
        <div className="cart-text">장바구니</div>
      </div>

      <CartSidePanel 
        isOpen={isPanelOpen} 
        onClose={handleClose}
        onCartUpdate={loadCartCount}
      />
    </>
  );
};

export default CartFloatingButton;