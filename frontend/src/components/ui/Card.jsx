import React from 'react';
import styles from './Card.module.css';

const Card = ({ children, className = '', padding = true, ...props }) => {
  return (
    <div 
      className={`${styles.card} ${padding ? styles.padding : ''} ${className}`} 
      {...props}
    >
      {children}
    </div>
  );
};

export const CardHeader = ({ children, className = '', ...props }) => (
  <div className={`${styles.header} ${className}`} {...props}>
    {children}
  </div>
);

export const CardBody = ({ children, className = '', ...props }) => (
  <div className={`${styles.body} ${className}`} {...props}>
    {children}
  </div>
);

export default Card;
