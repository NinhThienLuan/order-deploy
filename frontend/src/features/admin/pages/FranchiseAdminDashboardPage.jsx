import AdminHeader from '../components/AdminHeader';
import StatsCards from '../components/StatsCards';
import TopStoresTable from '../components/TopStoresTable';
import QuickActions from '../components/QuickActions';
import { DASHBOARD_STATS, TOP_STORES, QUICK_ACTIONS } from '../admin.constants';
import styles from './FranchiseAdminDashboardPage.module.css';

export default function FranchiseAdminDashboardPage() {
  return (
    <div className={styles.main}>
        <AdminHeader
          breadcrumbs={['Franchise', 'Dashboard']}
          viewLabel="Franchise Admin"
          userName="SYSTEM ADMIN"
          initials="FA"
        />

        <StatsCards stats={DASHBOARD_STATS} />

        <div className={styles.contentGrid}>
          <TopStoresTable stores={TOP_STORES} />
          <QuickActions actions={QUICK_ACTIONS} />
        </div>
    </div>
  );
}
