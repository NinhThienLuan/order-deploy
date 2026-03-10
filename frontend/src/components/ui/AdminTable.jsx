import React from 'react';
import Card from './Card';
import styles from './AdminTable.module.css';

/**
 * Standard div-based table for Admin management pages.
 * @param {{ 
 *   headers: React.ReactNode[], 
 *   children: React.ReactNode, 
 *   gridTemplate: string,
 *   loading?: boolean,
 *   loadingText?: string,
 *   noData?: boolean,
 *   noDataText?: string,
 *   pagination?: React.ReactNode
 * }} props
 */
const AdminTable = ({ 
  headers, 
  children, 
  gridTemplate, 
  loading, 
  loadingText = "Loading...", 
  noData, 
  noDataText = "No data found.",
  pagination
}) => {
  const gridStyle = { gridTemplateColumns: gridTemplate };

  if (loading) {
    return <div className={styles.loading}>{loadingText}</div>;
  }

  return (
    <Card padding={false} className={styles.tableContainer}>
      <div className={styles.tableHeader} style={gridStyle}>
        {headers.map((header, idx) => (
          <div key={idx}>{header}</div>
        ))}
      </div>

      {noData ? (
        <div className={styles.noItems}>{noDataText}</div>
      ) : (
        <div className={styles.tableBody}>
          {React.Children.map(children, child => {
            if (React.isValidElement(child)) {
              return React.cloneElement(child, { 
                style: { ...(child.props.style || {}), ...gridStyle },
                className: `${child.props.className || ''} ${styles.tableRow}`
              });
            }
            return child;
          })}
        </div>
      )}

      {pagination && (
        <div className={styles.pagination}>
          {pagination}
        </div>
      )}
    </Card>
  );
};

export default AdminTable;
