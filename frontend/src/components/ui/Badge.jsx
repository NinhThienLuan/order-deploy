import React from 'react';
import styles from './Badge.module.css';

const Badge = ({ children, variant = 'neutral', className = '', ...props }) => {
  return (
    <span 
      className={`${styles.badge} ${styles[variant]} ${className}`} 
      {...props}
    >
      {children}
    </span>
  );
};

export default Badge;
