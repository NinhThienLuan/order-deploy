import AdminPageHeader from '@/components/layout/AdminPageHeader';
import StatsCards from '@/features/admin/components/StatsCards';
import TopStoresTable from '@/features/admin/components/TopStoresTable';
import QuickActions from '@/features/admin/components/QuickActions';
import { DASHBOARD_STATS, TOP_STORES, QUICK_ACTIONS } from '@/features/admin/constants/shared/admin.constants';
import styles from './FranchiseAdminDashboardPage.module.css';

export default function FranchiseAdminDashboardPage() {
  return (
    <div className={styles.main}>
        <div className={styles.container}>
          <AdminPageHeader 
            title="Franchise Overview"
            subtitle="Monitor performance and manage operations across all stores."
          />

          <StatsCards stats={DASHBOARD_STATS} />

          <div className={styles.contentGrid}>
            <TopStoresTable stores={TOP_STORES} />
            <QuickActions actions={QUICK_ACTIONS} />
          </div>
        </div>
    </div>
  );
}
