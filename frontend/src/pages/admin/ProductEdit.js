import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import AdminSidebar from '../../components/layout/AdminSidebar';
import apiClient from '../../api/axiosConfig';
import './ProductForm.css';

const ProductEdit = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';

  const [formData, setFormData] = useState({
    productCode: '',
    createdAt: '',
    name: '',
    category: '',
    subCategory: '',
    stock: '',
    thumbnailImage: null,
    detailImage: null,
    manufacturer: '',
    country: '',
    price: '',
    salePrice: '',
    additionalFeatures: ''
  });

  const [loading, setLoading] = useState(false);
  const [fetchLoading, setFetchLoading] = useState(true);
  const [thumbnailPreview, setThumbnailPreview] = useState(null);
  const [detailPreview, setDetailPreview] = useState(null);
  
  // 카테고리 데이터 state
  const [categories, setCategories] = useState([]);
  const [selectedMainCategory, setSelectedMainCategory] = useState(null);

  useEffect(() => {
    fetchCategories();
    fetchProductData();
  }, [id]);

  // 카테고리 데이터 가져오기
  const fetchCategories = async () => {
    try {
      const response = await apiClient.get('/api/admin/products/categories');
      setCategories(response.data);
      console.log('카테고리 데이터 로드 성공:', response.data);
    } catch (error) {
      console.error('카테고리 로드 실패:', error);
      alert('카테고리 데이터를 불러오는데 실패했습니다.');
    }
  };

  const fetchProductData = async () => {
    setFetchLoading(true);
    try {
      const response = await apiClient.get(`/api/admin/products/${id}`);
      const product = response.data;

      console.log('Fetched product:', product);

      // 상세스펙 파싱
      let specs = {};
      try {
        specs = product.description ? JSON.parse(product.description) : {};
      } catch (e) {
        console.error('Failed to parse description:', e);
      }

      setFormData({
        productCode: product.productCode || '',
        createdAt: product.createdAt || '',
        name: product.name || '',
        category: product.category || '',
        subCategory: product.subCategory || '',
        stock: product.stock || 0,
        manufacturer: product.manufacturer || '',
        country: product.country || '',
        price: product.price || 0,
        salePrice: product.salePrice || 0,
        additionalFeatures: specs.additionalFeatures || '',
        thumbnailImage: null,
        detailImage: null
      });

      // 이미지 미리보기 설정
      if (product.thumbnailImage) {
        setThumbnailPreview(product.thumbnailImage);
      }
      if (product.detailImage) {
        setDetailPreview(product.detailImage);
      }

    } catch (error) {
      console.error('상품 정보 조회 실패:', error);
      alert('상품 정보를 불러올 수 없습니다.');
      navigate('/admin/products');
    } finally {
      setFetchLoading(false);
    }
  };

  // 현재 상품의 카테고리에 맞는 대카테고리 찾기
  useEffect(() => {
    if (formData.category && categories.length > 0) {
      const mainCat = categories.find(cat => 
        cat.subCategories.includes(formData.category)
      );
      if (mainCat) {
        setSelectedMainCategory(mainCat.parentCategory);
      }
    }
  }, [formData.category, categories]);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  // 대카테고리 선택 핸들러
  const handleMainCategoryChange = (e) => {
    const mainCategoryValue = e.target.value;
    setSelectedMainCategory(mainCategoryValue);
    setFormData(prev => ({
      ...prev,
      category: '' // 서브카테고리 초기화
    }));
  };

  // 서브카테고리 선택 핸들러
  const handleSubCategoryChange = (e) => {
    const subCategoryValue = e.target.value;
    setFormData(prev => ({
      ...prev,
      category: subCategoryValue
    }));
  };

  const handleFileChange = (e, type) => {
    const file = e.target.files[0];
    if (file) {
      setFormData(prev => ({
        ...prev,
        [type]: file
      }));

      const reader = new FileReader();
      reader.onloadend = () => {
        if (type === 'thumbnailImage') {
          setThumbnailPreview(reader.result);
        } else {
          setDetailPreview(reader.result);
        }
      };
      reader.readAsDataURL(file);
    }
  };

  const handleRemoveFile = (type) => {
    setFormData(prev => ({
      ...prev,
      [type]: null
    }));
    if (type === 'thumbnailImage') {
      setThumbnailPreview(null);
    } else {
      setDetailPreview(null);
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return '';
    try {
      const date = new Date(dateString);
      return date.toISOString().slice(0, 16).replace('T', ' ');
    } catch {
      return dateString;
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!formData.name || !formData.category || !formData.price) {
      alert('상품명, 카테고리, 정상가격은 필수 입력 항목입니다.');
      return;
    }

    setLoading(true);

    try {
      const submitData = new FormData();
      
      submitData.append('name', formData.name);
      submitData.append('category', formData.category);
      submitData.append('productCode', formData.productCode || '');
      submitData.append('manufacturer', formData.manufacturer || '');
      submitData.append('country', formData.country || '');
      submitData.append('price', formData.price || 0);
      submitData.append('salePrice', formData.salePrice || formData.price);
      submitData.append('stock', formData.stock || 0);
      submitData.append('description', formData.additionalFeatures || '');

      // 파일이 실제 File 객체일 때만 추가
      if (formData.thumbnailImage instanceof File) {
        submitData.append('thumbnailImage', formData.thumbnailImage);
        console.log('Uploading new thumbnail image');
      }
      if (formData.detailImage instanceof File) {
        submitData.append('detailImage', formData.detailImage);
        console.log('Uploading new detail image');
      }

      console.log('Updating product data...');
      
      // FormData 내용 로깅
      for (let pair of submitData.entries()) {
        console.log(pair[0] + ': ' + pair[1]);
      }

      const response = await apiClient.put(
        `/api/admin/products/${id}`,
        submitData,
        {
          headers: {
            'Content-Type': 'multipart/form-data'
          }
        }
      );

      console.log('Product updated:', response.data);
      
      if (response.data && response.data.success) {
        alert('상품이 수정되었습니다.');
        navigate('/admin/products');
      } else {
        alert(response.data.message || '상품 수정에 실패했습니다.');
      }

    } catch (error) {
      console.error('상품 수정 실패:', error);
      
      if (error.response?.data?.message) {
        alert(`상품 수정 실패: ${error.response.data.message}`);
      } else {
        alert('상품 수정 중 오류가 발생했습니다.');
      }
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = () => {
    if (window.confirm('수정 중인 내용이 삭제됩니다. 취소하시겠습니까?')) {
      navigate('/admin/products');
    }
  };

  // 현재 선택된 대카테고리의 서브카테고리 목록 가져오기
  const getCurrentSubCategories = () => {
    if (!selectedMainCategory) return [];
    const mainCat = categories.find(cat => cat.parentCategory === selectedMainCategory);
    return mainCat ? mainCat.subCategories : [];
  };

  if (fetchLoading) {
    return (
      <div className="admin-product-form">
        <AdminSidebar />
        <div className="product-form-main">
          <div className="loading-overlay">
            <div className="loading-spinner">로딩 중...</div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="admin-product-form">
      <AdminSidebar />
      
      <div className="product-form-main">
        <h1>Product Edit</h1>

        {loading && (
          <div className="loading-overlay">
            <div className="loading-spinner">수정 중...</div>
          </div>
        )}

        <form onSubmit={handleSubmit}>
          <div className="form-section">
            <table className="form-table">
              <tbody>
                <tr>
                  <th>상품코드</th>
                  <td>
                    <input
                      type="text"
                      value={formData.productCode}
                      readOnly
                      className="readonly-input"
                    />
                  </td>
                </tr>
                <tr>
                  <th>등록일자</th>
                  <td>
                    <input
                      type="text"
                      value={formatDate(formData.createdAt)}
                      readOnly
                      className="readonly-input"
                    />
                  </td>
                </tr>
                <tr>
                  <th>상품명</th>
                  <td>
                    <input
                      type="text"
                      name="name"
                      value={formData.name}
                      onChange={handleInputChange}
                      placeholder="상품명을 입력하세요"
                      required
                    />
                  </td>
                </tr>
                <tr>
                  <th>카테고리</th>
                  <td>
                    <div className="category-select">
                      <select
                        value={selectedMainCategory || ''}
                        onChange={handleMainCategoryChange}
                        required
                      >
                        <option value="">선택</option>
                        {categories.map(cat => (
                          <option key={cat.parentCategory} value={cat.parentCategory}>
                            {cat.parentCategoryName}
                          </option>
                        ))}
                      </select>
                      <select
                        value={formData.category}
                        onChange={handleSubCategoryChange}
                        disabled={!selectedMainCategory}
                        required
                      >
                        <option value="">선택2</option>
                        {getCurrentSubCategories().map(subCat => (
                          <option key={subCat} value={subCat}>{subCat}</option>
                        ))}
                      </select>
                    </div>
                  </td>
                </tr>
                <tr>
                  <th>재고수량</th>
                  <td>
                    <input
                      type="number"
                      name="stock"
                      value={formData.stock}
                      onChange={handleInputChange}
                      placeholder="재고 수량"
                      min="0"
                    />
                  </td>
                </tr>
                <tr>
                  <th>목록이미지</th>
                  <td>
                    <div className="file-upload-container">
                      <label className="file-upload-btn">
                        파일첨부
                        <input
                          type="file"
                          accept="image/*"
                          onChange={(e) => handleFileChange(e, 'thumbnailImage')}
                          style={{ display: 'none' }}
                        />
                      </label>
                      {thumbnailPreview && (
                        <div className="file-preview">
                          {formData.thumbnailImage instanceof File ? (
                            <img src={thumbnailPreview} alt="Thumbnail" />
                          ) : (
                            <span className="file-name">{thumbnailPreview.split('/').pop()}</span>
                          )}
                          <button
                            type="button"
                            className="remove-file-btn"
                            onClick={() => handleRemoveFile('thumbnailImage')}
                          >
                            ×
                          </button>
                        </div>
                      )}
                    </div>
                  </td>
                </tr>
                <tr>
                  <th>상세페이지</th>
                  <td>
                    <div className="file-upload-container">
                      <label className="file-upload-btn">
                        파일첨부
                        <input
                          type="file"
                          accept="image/*"
                          onChange={(e) => handleFileChange(e, 'detailImage')}
                          style={{ display: 'none' }}
                        />
                      </label>
                      {detailPreview && (
                        <div className="file-preview">
                          {formData.detailImage instanceof File ? (
                            <img src={detailPreview} alt="Detail" />
                          ) : (
                            <span className="file-name">{detailPreview.split('/').pop()}</span>
                          )}
                          <button
                            type="button"
                            className="remove-file-btn"
                            onClick={() => handleRemoveFile('detailImage')}
                          >
                            ×
                          </button>
                        </div>
                      )}
                    </div>
                  </td>
                </tr>
                <tr>
                  <th>제조사</th>
                  <td>
                    <input
                      type="text"
                      name="manufacturer"
                      value={formData.manufacturer}
                      onChange={handleInputChange}
                      placeholder="제조사"
                    />
                  </td>
                </tr>
                <tr>
                  <th>제조국</th>
                  <td>
                    <input
                      type="text"
                      name="country"
                      value={formData.country}
                      onChange={handleInputChange}
                      placeholder="제조국"
                    />
                  </td>
                </tr>
                <tr>
                  <th>정상가격</th>
                  <td>
                    <input
                      type="number"
                      name="price"
                      value={formData.price}
                      onChange={handleInputChange}
                      placeholder="정상가격"
                      required
                      min="0"
                    />
                  </td>
                </tr>
                <tr>
                  <th>할인가격</th>
                  <td>
                    <input
                      type="number"
                      name="salePrice"
                      value={formData.salePrice}
                      onChange={handleInputChange}
                      placeholder="할인가격"
                      min="0"
                    />
                  </td>
                </tr>
              </tbody>
            </table>

            <div className="form-buttons">
              <button type="button" className="cancel-btn" onClick={handleCancel}>
                목록
              </button>
              <button type="submit" className="submit-btn" disabled={loading}>
                등록
              </button>
            </div>
          </div>

          <div className="form-section">
            <h2>상세스펙</h2>
            <table className="form-table">
              <tbody>
                <tr>
                  <th>추가구성품</th>
                  <td>
                    <input
                      type="text"
                      name="additionalFeatures"
                      value={formData.additionalFeatures}
                      onChange={handleInputChange}
                      placeholder="추가구성품"
                    />
                  </td>
                </tr>
              </tbody>
            </table>

            <div className="form-buttons">
              <button type="button" className="cancel-btn" onClick={handleCancel}>
                목록
              </button>
              <button type="submit" className="submit-btn" disabled={loading}>
                수정
              </button>
            </div>
          </div>
        </form>
      </div>
    </div>
  );
};

export default ProductEdit;
