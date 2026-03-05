import { Link } from 'react-router-dom';
import styles from './QuickActions.module.css';

export default function QuickActions({ actions }) {
  return (
    <div className={styles.quickActions}>
      <h2 className={styles.quickActionsTitle}>Quick Actions</h2>
      {actions.map((action, i) => (
        <Link
          key={i}
          to={action.path}
          className={`${styles.actionBtn} ${styles[action.variant] || ''}`}
        >
          {action.label}
        </Link>
      ))}
    </div>
  );
}
