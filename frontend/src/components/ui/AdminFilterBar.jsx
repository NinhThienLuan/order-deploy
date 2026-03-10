import React from 'react';
import styles from './AdminFilterBar.module.css';

/**
 * Standard sticky filter bar for Admin pages.
 * @param {{ children: React.ReactNode }} props
 */
const AdminFilterBar = ({ children }) => {
  return (
    <div className={styles.filterBar}>
      <div className={styles.filterContent}>
        {children}
      </div>
    </div>
  );
};

export default AdminFilterBar;
