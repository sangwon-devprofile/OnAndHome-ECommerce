import React from 'react';
import './DashboardCard.css';

const DashboardCard = ({ title, type, children }) => {
  const getCardIcon = () => {
    switch(type) {
      case 'sales':
        return '💰';
      case 'products':
        return '📦';
      case 'members':
        return '👥';
      case 'board':
        return '📋';
      default:
        return '📊';
    }
  };

  return (
    <div className={`dashboard-card ${type}`}>
      <div className="card-header">
        <div className="card-icon">{getCardIcon()}</div>
        <h3 className="card-title">{title}</h3>
      </div>
      <div className="card-body">
        {children}
      </div>
    </div>
  );
};

export default DashboardCard;
