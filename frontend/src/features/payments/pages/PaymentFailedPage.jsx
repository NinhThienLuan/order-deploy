import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from '../../../layouts/Navbar';
import styles from './PaymentResultPage.module.css';

/**
 * Shown after VNPay redirects the browser to /payment/failed.
 * The backend has already marked the payment as FAILED via the
 * /vnpay-return handler.
 */
const PaymentFailedPage = () => {
    const navigate = useNavigate();
    const [orderId] = useState(() => {
        const id = sessionStorage.getItem('pendingOrderId');
        // Clear after reading
        sessionStorage.removeItem('pendingOrderId');
        sessionStorage.removeItem('pendingPaymentMethod');
        return id;
    });

    return (
        <>
            <Navbar />
            <div className={styles.page}>
                <div className={styles.resultCard}>
                    <div className={`${styles.resultIcon} ${styles.iconFailed}`}>✕</div>
                    <h2 className={styles.resultTitle}>Payment Failed</h2>
                    <p className={styles.resultMsg}>
                        Your payment was not completed. You can retry from the order page.
                    </p>
                    <button
                        className={styles.retryBtn}
                        onClick={() =>
                            orderId
                                ? navigate(`/orders/${orderId}`, { replace: true })
                                : navigate('/orders', { replace: true })
                        }
                    >
                        {orderId ? 'View Order & Retry' : 'Go to Orders'}
                    </button>
                </div>
            </div>
        </>
    );
};

export default PaymentFailedPage;
