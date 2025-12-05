import React, { useState, useEffect } from "react";
import { useDispatch, useSelector } from "react-redux";
import { addToCompare } from "../../store/slices/compareSlice";
import { productAPI } from "../../api";
import "./CompareSelectModal.css";

const CompareSelectModal = ({ isOpen, onClose, onSelect }) => {
  const dispatch = useDispatch();
  const compareItems = useSelector((state) => state.compare.items);
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedProduct, setSelectedProduct] = useState(null);

  // 첫 번째 비교 상품의 카테고리 가져오기
  const filterCategory =
    compareItems.length > 0 ? compareItems[0].category : null;

  useEffect(() => {
    if (isOpen) {
      fetchProducts();
    }
  }, [isOpen]);

  const fetchProducts = async () => {
    setLoading(true);
    try {
      const response = await productAPI.getAllProductsForUser();
      if (response.success && response.products) {
        // 카테고리 필터링 적용
        let filteredProducts = response.products;

        if (filterCategory) {
          filteredProducts = response.products.filter(
            (product) => product.category === filterCategory
          );
        }

        setProducts(filteredProducts);
      }
    } catch (error) {
      console.error("상품 조회 오류:", error);
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

  const handleProductSelect = (product) => {
    setSelectedProduct(product);
  };

  const handleConfirm = () => {
    if (!selectedProduct) {
      alert("비교할 상품을 선택해주세요.");
      return;
    }

    if (compareItems.length >= 4) {
      alert("최대 4개 상품까지 비교할 수 있습니다.");
      return;
    }

    const isAlreadySelected = compareItems.some(
      (item) => item.id === selectedProduct.id
    );

    if (isAlreadySelected) {
      alert("이미 선택된 상품입니다.");
      return;
    }

    const compareProduct = {
      id: selectedProduct.id,
      name: selectedProduct.name,
      price: selectedProduct.price,
      salePrice: selectedProduct.salePrice,
      category: selectedProduct.category,
      brand: selectedProduct.brand,
      stock: selectedProduct.stock,
      image: selectedProduct.thumbnailImage,
    };

    dispatch(addToCompare(compareProduct));
    setSelectedProduct(null);
    onClose();
  };

  const isProductInCompare = (productId) => {
    return compareItems.some((item) => item.id === productId);
  };

  if (!isOpen) return null;

  return (
    <div className="compare-select-overlay" onClick={onClose}>
      <div
        className="compare-select-modal"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="compare-select-header">
          <h2>
            비교할 제품을 선택하세요
            {filterCategory && (
              <span className="category-filter">
                {" "}
                - {filterCategory} 카테고리
              </span>
            )}
          </h2>
          <button className="close-btn" onClick={onClose}>
            ×
          </button>
        </div>

        <div className="compare-select-content">
          {loading ? (
            <div className="loading">로딩 중...</div>
          ) : products.length === 0 ? (
            <div className="loading">
              {filterCategory
                ? `${filterCategory} 카테고리에 비교 가능한 상품이 없습니다.`
                : "비교 가능한 상품이 없습니다."}
            </div>
          ) : (
            <div className="product-grid-modal">
              {products.map((product) => {
                const isSelected = selectedProduct?.id === product.id;
                const isInCompare = isProductInCompare(product.id);

                return (
                  <div
                    key={product.id}
                    className={`product-card-modal ${
                      isSelected ? "selected" : ""
                    } ${isInCompare ? "disabled" : ""}`}
                    onClick={() => !isInCompare && handleProductSelect(product)}
                  >
                    <div className="product-image-modal">
                      <img
                        src={getImageUrl(product.thumbnailImage)}
                        alt={product.name}
                        onError={(e) => {
                          e.target.src = "/images/placeholder.png";
                        }}
                      />
                    </div>
                    <div className="product-info-modal">
                      <h3 className="product-name-modal">{product.name}</h3>
                      <div className="product-price-modal">
                        {product.salePrice && product.salePrice < product.price
                          ? `${formatPrice(product.salePrice)}원`
                          : `${formatPrice(product.price)}원`}
                      </div>
                    </div>
                    {isSelected && (
                      <div className="check-icon">
                        <svg
                          width="24"
                          height="24"
                          viewBox="0 0 24 24"
                          fill="white"
                        >
                          <path d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z" />
                        </svg>
                      </div>
                    )}
                  </div>
                );
              })}
            </div>
          )}
        </div>

        <div className="compare-select-footer">
          <button className="confirm-btn" onClick={handleConfirm}>
            비교하기
          </button>
        </div>
      </div>
    </div>
  );
};

export default CompareSelectModal;
