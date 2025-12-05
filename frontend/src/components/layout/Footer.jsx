import React from 'react';
import './Footer.css';

const Footer = () => {
  return (
    <footer className="footer">
      <div className="footer-inner">
        <div className="footer-info">
          <div className="footer-logo">
            <img src="/images/logo.png" alt="On&Home" />
          </div>
          
          <div className="footer-content">
            <div className="company-info">
              <p><strong>On&Home</strong></p>
              <p>대표이사: 홍길동 | 사업자등록번호: 123-45-67890</p>
              <p>주소: 서울특별시 강남구 테헤란로 123</p>
              <p>고객센터: 1588-1234 | 이메일: support@onandhome.com</p>
            </div>
            
            <div className="footer-links">
              <a href="/terms">이용약관</a>
              <span>|</span>
              <a href="/privacy"><strong>개인정보처리방침</strong></a>
              <span>|</span>
              <a href="/guide">이용안내</a>
            </div>
          </div>
        </div>
        
        <div className="footer-bottom">
          <p>© 2025 On&Home. All rights reserved.</p>
        </div>
      </div>
    </footer>
  );
};

export default Footer;
