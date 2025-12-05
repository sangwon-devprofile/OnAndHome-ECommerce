import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { favoriteAPI } from '../../api/favoriteApi';
import './MyFavorites.css';

const MyFavorites = () => {
  const navigate = useNavigate();
  const [favorites, setFavorites] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchFavorites();
  }, []);

  const fetchFavorites = async () => {
    try {
      const response = await favoriteAPI.getList();
      if (response.success) {
        setFavorites(response.data);
      }
    } catch (error) {
      console.error('찜 목록 조회 실패:', error);
      alert('찜 목록을 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const handleRemoveFavorite = async (productId) => {
    if (!window.confirm('찜 목록에서 삭제하시겠습니까?')) {
      return;
    }

    try {
      const response = await favoriteAPI.toggle(productId);
      if (response.success) {
        // 목록에서 제거
        setFavorites(favorites.filter(fav => fav.productId !== productId));
        alert('찜 목록에서 제거되었습니다.');
      }
    } catch (error) {
      console.error('찜 삭제 실패:', error);
      alert('찜 목록에서 제거하는데 실패했습니다.');
    }
  };

  const handleProductClick = (productId) => {
    navigate(`/products/${productId}`);
  };

  const formatPrice = (price) => {
    return price?.toLocaleString() || '0';
  };

  const getImageUrl = (imagePath) => {
    if (!imagePath) return '/images/no-image.png';

    if (imagePath.startsWith('uploads/') || imagePath.startsWith('/uploads/')) {
      return `http://localhost:8080${imagePath.startsWith('/') ? '' : '/'}${imagePath}`;
    }

    if (!imagePath.includes('/') && !imagePath.startsWith('http')) {
      return `/product_img/${imagePath}.jpg`;
    }

    return imagePath;
  };

  if (loading) {
    return (
      <div className="my-favorites-container">
        <div className="loading">로딩 중...</div>
      </div>
    );
  }

  return (
    <div className="my-favorites-container">
      <h2 className="favorites-title">찜 목록</h2>
      
      {favorites.length === 0 ? (
        <div className="empty-favorites">
          <p>찜한 상품이 없습니다.</p>
          <button 
            className="btn-go-shopping"
            onClick={() => navigate('/products')}
          >
            상품 둘러보기
          </button>
        </div>
      ) : (
        <>
          <div className="favorites-count">
            총 {favorites.length}개의 상품
          </div>
          
          <div className="favorites-grid">
            {favorites.map((favorite) => (
              <div key={favorite.id} className="favorite-card">
                <div 
                  className="favorite-image"
                  onClick={() => handleProductClick(favorite.productId)}
                >
                  <img
                    src={getImageUrl(favorite.thumbnailImage)}
                    alt={favorite.productName}
                    onError={(e) => {
                      e.target.src = '/images/placeholder.png';
                    }}
                  />
                </div>
                
                <div className="favorite-info">
                  <h3 
                    className="favorite-name"
                    onClick={() => handleProductClick(favorite.productId)}
                  >
                    {favorite.productName}
                  </h3>
                  
                  <div className="favorite-category">
                    {favorite.category}
                  </div>
                  
                  <div className="favorite-prices">
                    {favorite.salePrice && favorite.salePrice < favorite.price ? (
                      <>
                        <span className="original-price">
                          {formatPrice(favorite.price)}원
                        </span>
                        <div className="price-row">
                          <span className="sale-price">
                            {formatPrice(favorite.salePrice)}원
                          </span>
                          <span className="discount-rate">
                            {Math.round(((favorite.price - favorite.salePrice) / favorite.price) * 100)}% 할인
                          </span>
                        </div>
                      </>
                    ) : (
                      <span className="sale-price">
                        {formatPrice(favorite.price)}원
                      </span>
                    )}
                  </div>
                  
                  <div className="favorite-actions">
                    <button
                      className="btn-remove"
                      onClick={() => handleRemoveFavorite(favorite.productId)}
                    >
                      삭제
                    </button>
                    <button
                      className="btn-view"
                      onClick={() => handleProductClick(favorite.productId)}
                    >
                      상품 보기
                    </button>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </>
      )}
    </div>
  );
};

export default MyFavorites;