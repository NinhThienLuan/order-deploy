import { useLocation } from 'react-router-dom';
import { useCart } from '@/features/orders/services/CartContext';
import styles from './CartFAB.module.css';

const CartFAB = () => {
    const { totalItems, openSidebar } = useCart();
    const location = useLocation();

    // Show FAB ONLY on the landing page ('/' or empty)
    const isLandingPage = location.pathname === '/' || location.pathname === '' || location.pathname.includes('index.html');

    if (!isLandingPage) {
        return null;
    }

    return (
        <button
            className={styles.fab}
            onClick={openSidebar}
            aria-label={`Open shopping cart (${totalItems} items)`}
        >
            <div className={styles.iconWrapper}>
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                    <path d="M6 2L3 6v14a2 2 0 002 2h14a2 2 0 002-2V6l-3-4z" />
                    <line x1="3" y1="6" x2="21" y2="6" />
                    <path d="M16 10a4 4 0 01-8 0" />
                </svg>
                {totalItems > 0 && (
                    <span className={styles.badge}>{totalItems}</span>
                )}
            </div>
        </button>
    );
};

export default CartFAB;
