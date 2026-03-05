import AdminHeader from '../components/AdminHeader';
import StatsCards from '../components/StatsCards';
import RecentOrdersTable from '../components/RecentOrdersTable';
import { STORE_STATS, STORE_RECENT_ORDERS } from '../admin.constants';
import styles from './StoreManagerDashboardPage.module.css';

export default function StoreManagerDashboardPage() {
  return (
    <div className={styles.main}>
        <AdminHeader
          breadcrumbs={['Store', 'Dashboard']}
          viewLabel="Store Manager"
          userName="STORE MANAGER"
          initials="SM"
        />

        <StatsCards stats={STORE_STATS} />

        <div className={styles.contentGrid}>
          <RecentOrdersTable orders={STORE_RECENT_ORDERS} />

          <div className={styles.storeInfo}>
            <h2 className={styles.storeInfoTitle}>Store Details</h2>
            <div className={styles.storeInfoItem}>
              <span>Store ID</span>
              <span>STR-D1</span>
            </div>
            <div className={styles.storeInfoItem}>
              <span>Location</span>
              <span>District 1 Flagship</span>
            </div>
            <div className={styles.storeInfoItem}>
              <span>Status</span>
              <span>Active</span>
            </div>
            <div className={styles.storeInfoItem}>
              <span>Hours</span>
              <span>7:00 AM – 10:00 PM</span>
            </div>
            <div className={styles.storeInfoItem}>
              <span>Manager</span>
              <span>Store Admin</span>
            </div>
          </div>
        </div>
    </div>
  );
}
