import React from 'react';
import './Loading.css';

const Loading = ({ fullPage = false, size = 'medium' }) => {
  if (fullPage) {
    return (
      <div className="loading-overlay">
        <div className={`spinner spinner-${size}`}></div>
      </div>
    );
  }

  return (
    <div className="loading-container">
      <div className={`spinner spinner-${size}`}></div>
    </div>
  );
};

export default Loading;
