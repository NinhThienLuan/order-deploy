import React from 'react';
import styles from './Select.module.css';

const Select = ({ label, children, className = '', error, ...props }) => {
  return (
    <div className={`${styles.container} ${className}`}>
      {label && <label className={styles.label}>{label}</label>}
      <div className={styles.wrapper}>
        <select className={`${styles.select} ${error ? styles.selectError : ''}`} {...props}>
          {children}
        </select>
        <svg className={styles.chevron} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
          <polyline points="6 9 12 15 18 9" />
        </svg>
      </div>
      {error && <span className={styles.errorText}>{error}</span>}
    </div>
  );
};

export default Select;
