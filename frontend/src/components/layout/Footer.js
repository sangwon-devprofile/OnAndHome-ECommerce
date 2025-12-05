import React from 'react';
import { Link } from 'react-router-dom';
import './Footer.css';

const Footer = () => {
  return (
    <footer className="footer-o">
      {/* 푸터 추천 상품 섹션 */}
      <div className="footer-banner-section">
        <div className="footer-banner-inner">
          <div className="footer-banner-left">
            <img src="/product_img/item_01_01.jpg" alt="Bespoke AI 제트 400W" />
          </div>
          <div className="footer-banner-right">
            <div className="footer-banner-item">
              <div className="footer-banner-img">
                <img src="/product_img/item_01_02.jpg" alt="비더프르트 안마의자" />
              </div>
              <div className="footer-banner-text">
                <p className="footer-banner-title">비더프르트 플로츠 안마의자 볼스케이로북</p>
                <p className="footer-banner-desc">코가는 올라구 가능은 편안하게</p>
              </div>
            </div>

            <div className="footer-banner-item">
              <div className="footer-banner-img">
                <img src="/product_img/item_01_03.jpg" alt="삼성 AI 세탁기" />
              </div>
              <div className="footer-banner-text">
                <p className="footer-banner-title">삼성 AI 세탁기 WF25DG8650BE 25kg</p>
                <p className="footer-banner-desc">세탁에서 할 수여서 질리지 AI 와 함께하는</p>
              </div>
            </div>

            <div className="footer-banner-item">
              <div className="footer-banner-img">
                <img src="/product_img/item_01_04.jpg" alt="젠스도 스피커" />
              </div>
              <div className="footer-banner-text">
                <p className="footer-banner-title">젠스도 2세대 블루픽 Hi-Fi 스피커 X08T</p>
                <p className="footer-banner-desc">높은깔 경우를 가지다면</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* 회사 정보 섹션 */}
      <div className="footer-info-section">
        <div className="footer-info-inner">
          <div className="footer-logo-box">
            <Link to="/">
              <div className="footer-logo"></div>
            </Link>
          </div>

          <div className="footer-company-box">
            <div className="footer-contact">
              <span className="footer-contact-label">고객센터</span>
              <span className="footer-contact-number">1544-7777</span>
            </div>

            <div className="footer-details">
              <table className="footer-details-table">
                <tbody>
                  <tr>
                    <td className="footer-label">회사명</td>
                    <td className="footer-value">(주)캡소프트</td>
                  </tr>
                  <tr>
                    <td className="footer-label">대표</td>
                    <td className="footer-value">이주영</td>
                  </tr>
                  <tr>
                    <td className="footer-label">팩스</td>
                    <td className="footer-value">02-1544-7778</td>
                  </tr>
                  <tr>
                    <td className="footer-label">이메일</td>
                    <td className="footer-value">faker@naver.com</td>
                  </tr>
                  <tr>
                    <td className="footer-label">주소</td>
                    <td className="footer-value">서울 서초구 서초동 123-456</td>
                  </tr>
                  <tr>
                    <td className="footer-label">사업자등록번호</td>
                    <td className="footer-value">123-456789</td>
                  </tr>
                  <tr>
                    <td className="footer-label">통신판매업신고</td>
                    <td className="footer-value">카456-7894</td>
                  </tr>
                  <tr>
                    <td className="footer-label">개인정보 책임자</td>
                    <td className="footer-value">최우제</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>
    </footer>
  );
};

export default Footer;
