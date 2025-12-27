import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import './HomePage.css';

const HomePage = () => {
  const navigate = useNavigate();
  const [currentSlide, setCurrentSlide] = useState(0);
  const [products, setProducts] = useState([]);
  const [topProducts, setTopProducts] = useState([]);

  // 슬라이드 데이터
  const slides = [
    { id: 423, image: '/product_img/slide_01.jpg', alt: '슬라이드 1' },
    { id: 431, image: '/product_img/slide_02.jpg', alt: '슬라이드 2' },
    { id: 469, image: '/product_img/slide_03.jpg', alt: '슬라이드 3' },
    { id: 493, image: '/product_img/slide_04.jpg', alt: '슬라이드 4' },
  ];

  // 자동 슬라이드
  useEffect(() => {
    const timer = setInterval(() => {
      setCurrentSlide((prev) => (prev + 1) % slides.length);
    }, 5000);

    return () => clearInterval(timer);
  }, [slides.length]);

  // MD's CHOICE 상품 로드
  useEffect(() => {
    const fetchMdChoice = async () => {
      try {
        const response = await fetch('http://localhost:8080/api/product/list?page=0&size=6');
        const data = await response.json();
        if (data.success) {
          setProducts(data.content || []);
        }
      } catch (error) {
        console.error('MD\'s CHOICE 로드 실패:', error);
      }
    };

    fetchMdChoice();
  }, []);

  // 최다 구매 상품 로드
  useEffect(() => {
    const fetchTopSales = async () => {
      try {
        const response = await fetch('http://localhost:8080/api/product/list?page=0&size=6');
        const data = await response.json();
        if (data.success) {
          setTopProducts(data.content || []);
        }
      } catch (error) {
        console.error('최다 구매 상품 로드 실패:', error);
      }
    };

    fetchTopSales();
  }, []);

  const formatPrice = (price) => {
    return price?.toLocaleString() || '0';
  };

  const handleProductClick = (product) => {
    if (product.stock === 0 || product.status === '판매중지') {
      alert("품절된 상품입니다.");
      return;
    }
    navigate(`/products/${product.productId}`);
  };

  const handleSlideClick = (productId) => {
    navigate(`/products/${productId}`);
  };

  const handleDotClick = (index) => {
    setCurrentSlide(index);
  };

  const handleViewAllProducts = () => {
    navigate('/products');
  };

  return (
    <div className="on-main-wrap">
      {/* 상단 슬라이드 */}
      <div className="main-slide-wrapper">
        <div
          className="main-slide-track"
          style={{ transform: `translateX(-${currentSlide * 100}%)` }}
        >
          {slides.map((slide, index) => (
            <div
              key={index}
              className="main-slide"
              onClick={() => handleSlideClick(slide.id)}
              style={{ cursor: 'pointer' }}
            >
              <img src={slide.image} alt={slide.alt} />
            </div>
          ))}
        </div>
        
        {/* 슬라이드 네비게이션 점 */}
        <div className="slide-dots">
          {slides.map((_, index) => (
            <div
              key={index}
              className={`slide-dot ${currentSlide === index ? 'is-active' : ''}`}
              onClick={() => handleDotClick(index)}
            />
          ))}
        </div>
      </div>

      {/* 금주의 HOT아이템 - 큰 배너 섹션 */}
      <div className="on-item01">
        <div className="on-item01-inner">
          <h2 className="sub-title">금주의 HOT아이템</h2>
          <div className="on-item01-wrapper">
            {/* 왼쪽 */}
            <div className="on-item01-l">
              <div className="on-item01-l-up">
                <div className="on-item01-01" onClick={() => handleProductClick(439)} style={{ cursor: 'pointer' }}>
                  <img src="/product_img/item_01_01.jpg" alt="상품 이미지" />
                </div>
                <div className="on-item01-02" onClick={() => handleProductClick(480)} style={{ cursor: 'pointer' }}>
                  <img src="/product_img/item_01_02.jpg" alt="상품 이미지" />
                </div>
              </div>
              {/* 아래 */}
              <div className="on-item01-03" onClick={() => handleProductClick(475)} style={{ cursor: 'pointer' }}>
                <img src="/product_img/item_01_03.jpg" alt="상품 이미지" />
              </div>
            </div>
            {/* 오른쪽 */}
            <div className="on-item01-r" onClick={() => handleProductClick(436)} style={{ cursor: 'pointer' }}>
              <img src="/product_img/item_01_04.jpg" alt="상품 이미지" />
            </div>
          </div>
        </div>
      </div>

      {/* 아이템 영역2 - MD's CHOICE */}
      <div className="on-item02">
        <div className="on-item02-inner">
          <h2 className="sub-title">MD's CHOICE</h2>
          <div id="mdChoiceContainer">
            {products.length > 0 && (
              <>
                <div className="item-array">
                  {products.slice(0, 3).map((product) => {
                    const isSoldOut = product.stock === 0 || product.status === '판매중지';
                    return (
                      <div
                        key={product.productId}
                        className={`item-thum-wrapper ${isSoldOut ? 'is-sold-out' : ''}`}
                        onClick={() => handleProductClick(product)}
                        style={{ position: 'relative' }}
                      >
                        <img 
                          src={product.mainImg || '/images/placeholder.png'} 
                          alt={product.productName} 
                          style={isSoldOut ? { filter: 'grayscale(1) opacity(0.6)' } : {}}
                        />
                        {isSoldOut && (
                          <div className="sold-out-overlay-small">
                            <span>SOLD OUT</span>
                          </div>
                        )}
                        <div className="item-info">
                          <div className="item-title">{product.productName}</div>
                          <ul className="price-info">
                            <li className="price-info-sale">{formatPrice(product.salePrice)}원</li>
                          </ul>
                        </div>
                      </div>
                    );
                  })}
                </div>
                <div className="item-array">
                  {products.slice(3, 6).map((product) => {
                    const isSoldOut = product.stock === 0 || product.status === '판매중지';
                    return (
                      <div
                        key={product.productId}
                        className={`item-thum-wrapper ${isSoldOut ? 'is-sold-out' : ''}`}
                        onClick={() => handleProductClick(product)}
                        style={{ position: 'relative' }}
                      >
                        <img 
                          src={product.mainImg || '/images/placeholder.png'} 
                          alt={product.productName} 
                          style={isSoldOut ? { filter: 'grayscale(1) opacity(0.6)' } : {}}
                        />
                        {isSoldOut && (
                          <div className="sold-out-overlay-small">
                            <span>SOLD OUT</span>
                          </div>
                        )}
                        <div className="item-info">
                          <div className="item-title">{product.productName}</div>
                          <ul className="price-info">
                            <li className="price-info-sale">{formatPrice(product.salePrice)}원</li>
                          </ul>
                        </div>
                      </div>
                    );
                  })}
                </div>
              </>
            )}
          </div>
        </div>
      </div>

      {/* 아이템 영역3 - 이달의 최다 구매 상품 */}
      <div className="on-item02">
        <div className="on-item02-inner">
          <h2 className="sub-title">이달의 최다 구매 상품</h2>
          <div id="topSalesContainer">
            {topProducts.length > 0 && (
              <>
                <div className="item-array">
                  {topProducts.slice(0, 3).map((product) => {
                    const isSoldOut = product.stock === 0 || product.status === '판매중지';
                    return (
                      <div
                        key={product.productId}
                        className={`item-thum-wrapper ${isSoldOut ? 'is-sold-out' : ''}`}
                        onClick={() => handleProductClick(product)}
                        style={{ position: 'relative' }}
                      >
                        <img 
                          src={product.mainImg || '/images/placeholder.png'} 
                          alt={product.productName} 
                          style={isSoldOut ? { filter: 'grayscale(1) opacity(0.6)' } : {}}
                        />
                        {isSoldOut && (
                          <div className="sold-out-overlay-small">
                            <span>SOLD OUT</span>
                          </div>
                        )}
                        <div className="item-info">
                          <div className="item-title">{product.productName}</div>
                          <ul className="price-info">
                            <li className="price-info-sale">{formatPrice(product.salePrice)}원</li>
                          </ul>
                        </div>
                      </div>
                    );
                  })}
                </div>
                <div className="item-array">
                  {topProducts.slice(3, 6).map((product) => {
                    const isSoldOut = product.stock === 0 || product.status === '판매중지';
                    return (
                      <div
                        key={product.productId}
                        className={`item-thum-wrapper ${isSoldOut ? 'is-sold-out' : ''}`}
                        onClick={() => handleProductClick(product)}
                        style={{ position: 'relative' }}
                      >
                        <img 
                          src={product.mainImg || '/images/placeholder.png'} 
                          alt={product.productName} 
                          style={isSoldOut ? { filter: 'grayscale(1) opacity(0.6)' } : {}}
                        />
                        {isSoldOut && (
                          <div className="sold-out-overlay-small">
                            <span>SOLD OUT</span>
                          </div>
                        )}
                        <div className="item-info">
                          <div className="item-title">{product.productName}</div>
                          <ul className="price-info">
                            <li className="price-info-sale">{formatPrice(product.salePrice)}원</li>
                          </ul>
                        </div>
                      </div>
                    );
                  })}
                </div>
              </>
            )}
          </div>

          {/* 전체 상품 보기 버튼 */}
          <div className="view-all-products-wrapper">
            <button className="btn-view-all-products" onClick={handleViewAllProducts}>
              전체 상품 보기
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default HomePage;
