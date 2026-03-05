import { useAuth } from '@/features/auth/AuthContext';
import AdminHeader from '../components/AdminHeader';
import StatsCards from '../components/StatsCards';
import TopStoresTable from '../components/TopStoresTable';
import QuickActions from '../components/QuickActions';
import RecentOrdersTable from '../components/RecentOrdersTable';
import {
    DASHBOARD_STATS, TOP_STORES, QUICK_ACTIONS,
    STORE_STATS, STORE_RECENT_ORDERS,
} from '../admin.constants';
import styles from './FranchiseAdminDashboardPage.module.css';
import storeStyles from './StoreManagerDashboardPage.module.css';

export default function AdminDashboardPage() {
    const { user } = useAuth();
    const role = user?.role;

    if (role === 'STORE_MANAGER') {
        return (
            <div className={storeStyles.main}>
                    <AdminHeader
                        breadcrumbs={['Store', 'Dashboard']}
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
        );
    }

    // Default: ADMIN view
    return (
        <div className={styles.main}>
                <AdminHeader
                    breadcrumbs={['Franchise', 'Dashboard']}
                />
                <StatsCards stats={DASHBOARD_STATS} />
                <div className={styles.contentGrid}>
                    <TopStoresTable stores={TOP_STORES} />
                    <QuickActions actions={QUICK_ACTIONS} />
                </div>
            </div>
    );
}
