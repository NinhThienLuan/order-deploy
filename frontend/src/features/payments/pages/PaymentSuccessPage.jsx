import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from '../../../layouts/Navbar';
import styles from './PaymentResultPage.module.css';

/**
 * Shown after VNPay redirects the browser to /payment/success.
 * The backend has already verified the VNPay signature and updated
 * the order status via the /vnpay-return handler, so all we need
 * to do here is display the result and navigate to the order.
 */
const PaymentSuccessPage = () => {
    const navigate = useNavigate();
    const [orderId] = useState(() => sessionStorage.getItem('pendingOrderId'));

    useEffect(() => {
        // Clear session storage after reading
        sessionStorage.removeItem('pendingOrderId');
        sessionStorage.removeItem('pendingPaymentMethod');

        const timer = setTimeout(() => {
            if (orderId) {
                navigate(`/orders/${orderId}`, { replace: true });
            } else {
                navigate('/orders', { replace: true });
            }
        }, 2000);
        return () => clearTimeout(timer);
    }, [navigate, orderId]);

    return (
        <>
            <Navbar />
            <div className={styles.page}>
                <div className={styles.resultCard}>
                    <div className={`${styles.resultIcon} ${styles.iconSuccess}`}>✓</div>
                    <h2 className={styles.resultTitle}>Payment Successful</h2>
                    <p className={styles.resultMsg}>Your payment has been confirmed. Redirecting to your order…</p>
                    {orderId && (
                        <button
                            className={styles.retryBtn}
                            style={{ background: 'var(--color-primary, #2563eb)' }}
                            onClick={() => navigate(`/orders/${orderId}`, { replace: true })}
                        >
                            View Order
                        </button>
                    )}
                </div>
            </div>
        </>
    );
};

export default PaymentSuccessPage;
