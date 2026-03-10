import React from 'react';
import styles from './AdminPageHeader.module.css';

/**
 * Standard header for Admin/Management pages.
 * @param {{ preTitle?: string, title: string, subtitle?: string, actions?: React.ReactNode }} props
 */
const AdminPageHeader = ({ preTitle, title, subtitle, actions }) => {
  return (
    <div className={styles.pageHeader}>
      <div className={styles.headerMeta}>
        {preTitle && <div className={styles.preTitle}>{preTitle}</div>}
        <h1 className={styles.pageTitle}>{title}</h1>
        {subtitle && <p className={styles.headerSubtitle}>{subtitle}</p>}
      </div>
      {actions && <div className={styles.headerActions}>{actions}</div>}
    </div>
  );
};

export default AdminPageHeader;
