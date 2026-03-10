import { useAuth } from '@/features/authentication/services/AuthContext';
import AdminPageHeader from '@/components/layout/AdminPageHeader';
import StatsCards from '@/features/admin/components/shared/StatsCards';
import TopStoresTable from '@/features/admin/components/shared/TopStoresTable';
import QuickActions from '@/features/admin/components/shared/QuickActions';
import RecentOrdersTable from '@/features/admin/components/shared/RecentOrdersTable';
import {
    DASHBOARD_STATS, TOP_STORES, QUICK_ACTIONS,
    STORE_STATS, STORE_RECENT_ORDERS,
} from '@/features/admin/constants/shared/admin.constants';
import styles from './FranchiseAdminDashboardPage.module.css';
import storeStyles from './StoreManagerDashboardPage.module.css';

export default function AdminDashboardPage() {
    const { user } = useAuth();
    const role = user?.role;

    if (role === 'STORE_MANAGER') {
        return (
            <div className={storeStyles.main}>
                    <div className={storeStyles.container}>
                        <AdminPageHeader 
                            title="Store Dashboard"
                            subtitle={`Welcome back, ${user?.name || 'Manager'}. Here is your store's performance overview.`}
                        />
                        
                        <StatsCards stats={STORE_STATS} />
                        
                        <div className={storeStyles.contentGrid}>
                            <RecentOrdersTable orders={STORE_RECENT_ORDERS} />
                            <div className={storeStyles.storeInfo}>
                                <h2 className={storeStyles.storeInfoTitle}>Store Details</h2>
                                <div className={storeStyles.storeInfoItem}>
                                    <span>Location</span>
                                    <span>District 1 Flagship</span>
                                </div>
                                <div className={storeStyles.storeInfoItem}>
                                    <span>Status</span>
                                    <span>Active</span>
                                </div>
                                <div className={storeStyles.storeInfoItem}>
                                    <span>Hours</span>
                                    <span>7:00 AM – 10:00 PM</span>
                                </div>
                                <div className={storeStyles.storeInfoItem}>
                                    <span>Manager</span>
                                    <span>{user?.name || '—'}</span>
                                </div>
                            </div>
                        </div>
                    </div>
            </div>
        );
    }

    // Default: ADMIN view
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
