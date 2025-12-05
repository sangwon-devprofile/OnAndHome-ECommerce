import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useSelector } from "react-redux";
import { cartAPI } from "../../api";
import "./Cart.css";

const Cart = () => {
  const navigate = useNavigate();
  const { isAuthenticated } = useSelector((state) => state.user);

  const [cartItems, setCartItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedItems, setSelectedItems] = useState([]);

  useEffect(() => {
    if (!isAuthenticated) {
      alert("로그인이 필요합니다.");
      navigate("/login");
      return;
    }
    loadCartItems();
  }, [isAuthenticated, navigate]);

  const loadCartItems = async () => {
    try {
      const response = await cartAPI.getCartItems();
      if (response.success && response.data) {
        setCartItems(response.data);
        // 기본적으로 모든 아이템 선택
        setSelectedItems(response.data.map((item) => item.id));
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

  const handleCheckboxChange = (itemId) => {
    setSelectedItems((prev) => {
      if (prev.includes(itemId)) {
        return prev.filter((id) => id !== itemId);
      } else {
        return [...prev, itemId];
      }
    });
  };

  const handleSelectAll = (e) => {
    if (e.target.checked) {
      setSelectedItems(cartItems.map((item) => item.id));
    } else {
      setSelectedItems([]);
    }
  };

  const handleIncreaseQuantity = async (cartItemId, currentQuantity) => {
    try {
      const response = await cartAPI.updateQuantity(
        cartItemId,
        currentQuantity + 1
      );
      if (response.success) {
        loadCartItems();
      } else {
        alert(response.message || "수량 변경에 실패했습니다.");
      }
    } catch (error) {
      console.error("수량 변경 오류:", error);
      alert("수량 변경 중 오류가 발생했습니다.");
    }
  };

  const handleDecreaseQuantity = async (cartItemId, currentQuantity) => {
    if (currentQuantity <= 1) {
      alert("수량은 최소 1개 이상이어야 합니다.");
      return;
    }
    try {
      const response = await cartAPI.updateQuantity(
        cartItemId,
        currentQuantity - 1
      );
      if (response.success) {
        loadCartItems();
      } else {
        alert(response.message || "수량 변경에 실패했습니다.");
      }
    } catch (error) {
      console.error("수량 변경 오류:", error);
      alert("수량 변경 중 오류가 발생했습니다.");
    }
  };

  const handleDeleteItem = async (cartItemId) => {
    if (!window.confirm("장바구니에서 삭제하시겠습니까?")) {
      return;
    }

    try {
      const response = await cartAPI.removeItem(cartItemId);
      if (response.success) {
        loadCartItems();
        setSelectedItems((prev) => prev.filter((id) => id !== cartItemId));
      } else {
        alert(response.message || "삭제에 실패했습니다.");
      }
    } catch (error) {
      console.error("삭제 오류:", error);
      alert("삭제 중 오류가 발생했습니다.");
    }
  };

  const handleDeleteSelected = async () => {
    if (selectedItems.length === 0) {
      alert("삭제할 상품을 선택해주세요.");
      return;
    }

    if (
      !window.confirm(
        `선택한 ${selectedItems.length}개 상품을 삭제하시겠습니까?`
      )
    ) {
      return;
    }

    try {
      await Promise.all(
        selectedItems.map((itemId) => cartAPI.removeItem(itemId))
      );
      alert("선택한 상품이 삭제되었습니다.");
      loadCartItems();
      setSelectedItems([]);
    } catch (error) {
      console.error("삭제 오류:", error);
      alert("삭제 중 오류가 발생했습니다.");
    }
  };

  const getTotalPrice = () => {
    return cartItems
      .filter((item) => selectedItems.includes(item.id))
      .reduce((total, item) => {
        const price = item.product.salePrice || item.product.price;
        return total + price * item.quantity;
      }, 0);
  };

  const handleGoToOrder = () => {
    if (selectedItems.length === 0) {
      alert("구매할 상품을 선택해주세요.");
      return;
    }

    // 선택된 상품들의 정보를 모아서 OrderPayment로 전달
    const selectedProducts = cartItems
      .filter((item) => selectedItems.includes(item.id))
      .map((item) => ({
        id: item.product.id,
        cartItemId: item.id,
        name: item.product.name,
        price: item.product.price,
        salePrice: item.product.salePrice,
        quantity: item.quantity,
        thumbnailImage: item.product.thumbnailImage,
      }));

    navigate("/user/order-payment", {
      state: {
        products: selectedProducts,
        fromCart: true,
      },
    });
  };

  if (loading) {
    return <div className="loading">로딩 중...</div>;
  }

  return (
    <div className="cart-container">
      <div className="cart-inner">
        <h2 className="page-title">장바구니</h2>

        {cartItems.length === 0 ? (
          <div className="empty-cart">
            <p>장바구니가 비어있습니다.</p>
            <button
              className="btn btn-primary"
              onClick={() => navigate("/products")}
            >
              쇼핑 계속하기
            </button>
          </div>
        ) : (
          <>
            <div className="cart-controls">
              <label className="checkbox-label">
                <input
                  type="checkbox"
                  checked={selectedItems.length === cartItems.length}
                  onChange={handleSelectAll}
                />
                <span>전체 선택</span>
              </label>
              <button className="btn btn-delete" onClick={handleDeleteSelected}>
                선택 삭제
              </button>
            </div>

            <div className="cart-items">
              {cartItems.map((item) => {
                const product = item.product;
                const price = product.salePrice || product.price;
                const itemTotal = price * item.quantity;

                return (
                  <div key={item.id} className="cart-item">
                    <div className="item-select">
                      <input
                        type="checkbox"
                        checked={selectedItems.includes(item.id)}
                        onChange={() => handleCheckboxChange(item.id)}
                      />
                    </div>

                    <div className="item-image">
                      <img
                        src={getImageUrl(product.thumbnailImage)}
                        alt={product.name}
                        onError={(e) => {
                          e.target.src = "/images/item.png";
                          e.target.onerror = null;
                        }}
                      />
                    </div>

                    <div className="item-info">
                      <h3 className="item-name">{product.name}</h3>
                      <p className="item-price">{formatPrice(price)}원</p>
                    </div>

                    <div className="item-quantity">
                      <button
                        className="btn-quantity"
                        onClick={() =>
                          handleDecreaseQuantity(item.id, item.quantity)
                        }
                      >
                        -
                      </button>
                      <span className="quantity">{item.quantity}</span>
                      <button
                        className="btn-quantity"
                        onClick={() =>
                          handleIncreaseQuantity(item.id, item.quantity)
                        }
                      >
                        +
                      </button>
                    </div>

                    <div className="item-total">
                      <span className="total-label">합계</span>
                      <span className="total-price">
                        {formatPrice(itemTotal)}원
                      </span>
                    </div>

                    <button
                      className="btn-remove"
                      onClick={() => handleDeleteItem(item.id)}
                      title="삭제"
                    >
                      ✕
                    </button>
                  </div>
                );
              })}
            </div>

            <div className="cart-summary">
              <div className="summary-row">
                <span className="summary-label">총 주문금액</span>
                <span className="summary-value">
                  {formatPrice(getTotalPrice())}원
                </span>
              </div>
            </div>

            <div className="cart-actions">
              <button className="btn btn-order" onClick={handleGoToOrder}>
                구매하기
              </button>
            </div>
          </>
        )}
      </div>
    </div>
  );
};

export default Cart;
