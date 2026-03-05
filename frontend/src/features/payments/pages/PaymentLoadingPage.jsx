import { useEffect, useRef } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import Navbar from '../../../layouts/Navbar';
import styles from './PaymentResultPage.module.css';
import { processPayment } from '../../orders/orders.service';

/**
 * /payment/loading
 *
 * Intermediary page shown after the user confirms checkout.
 * Performs the POST /v1/payments/create call here so the user
 * sees a loading state, then redirects to the gateway URL.
 *
 * Expects navigation state:
 *   { orderId, paymentMethod, momoRequestType?, amount }
 */
const PaymentLoadingPage = () => {
    const navigate  = useNavigate();
    const location  = useLocation();
    const calledRef = useRef(false);

    // Pull params from navigation state (set by CheckoutPage / OrderDetailPage)
    const {
        orderId,
        paymentMethod,
        momoRequestType,
        amount,
    } = location.state || {};

    useEffect(() => {
        if (calledRef.current) return; // strict-mode double-fire guard
        calledRef.current = true;

        if (!orderId || !amount) {
            // No order info — send back to orders list
            navigate('/orders', { replace: true });
            return;
        }

        const call = async () => {
            try {
                const payment = await processPayment(orderId, {
                    paymentMethod,
                    amount,
                    ...(paymentMethod === 'MOMO' && momoRequestType ? { momoRequestType } : {}),
                });

                if (payment?.paymentUrl) {
                    window.location.href = payment.paymentUrl;
                } else {
                    // No URL → payment was instant (CASH/WALLET) or error
                    navigate(`/orders/${orderId}`, { replace: true });
                }
            } catch (err) {
                const msg = err?.response?.data?.message || err?.message || 'Payment initiation failed.';
                navigate(`/orders/${orderId}`, {
                    replace: true,
                    state: { paymentError: msg },
                });
            }
        };

        call();
    // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    return (
        <>
            <Navbar />
            <div className={styles.page}>
                <div className={styles.resultCard}>
                    <div className={styles.spinner} />
                    <h2 className={styles.resultTitle}>Processing Payment</h2>
                    <p className={styles.resultMsg}>
                        Please wait while we connect to{' '}
                        {paymentMethod === 'MOMO' ? 'MoMo' : paymentMethod === 'VNPAY' ? 'VNPay' : 'the payment gateway'}…
                    </p>
                </div>
            </div>
        </>
    );
};

export default PaymentLoadingPage;
