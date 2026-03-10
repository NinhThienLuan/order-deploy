import { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import Navbar from '@/layouts/Navbar';
import styles from './PaymentResultPage.module.css';

const METHOD_LABELS = { VNPAY: 'VNPay', MOMO: 'MoMo' };

const PaymentResultPage = () => {
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();

    const isMock    = searchParams.get('mock') === 'true';
    const method    = searchParams.get('method') || sessionStorage.getItem('pendingPaymentMethod') || 'MOMO';

    // Resolve real orderId:
    // MoMo returns the real UUID in base64-encoded extraData: {"orderId":"<uuid>"}
    // The 'orderId' param is MoMo's own format "MOMO-<uuid>-<timestamp>" — don't use it directly.
    const orderId = (() => {
        try {
            const raw = searchParams.get('extraData');
            if (raw) {
                const decoded = JSON.parse(atob(decodeURIComponent(raw)));
                if (decoded?.orderId) return decoded.orderId;
            }
        } catch { /* ignore */ }
        return sessionStorage.getItem('pendingOrderId');
    })();

    // For mock, these come from URL; for real they're not needed here
    const paymentId  = searchParams.get('paymentId');

    const [status, setStatus]   = useState(null); // null | 'processing' | 'success' | 'failed'
    const [error, setError]     = useState(null);

    // For real MoMo gateway callbacks — resultCode=0 means success
    useEffect(() => {
        if (!isMock) {
            const momoCode = searchParams.get('resultCode');
            if (momoCode !== null) {
                // Real MoMo return: IPN has already updated the server; just show UI
                const success = momoCode === '0';
                setStatus(success ? 'success' : 'failed');
                if (!success) {
                    setError(`MoMo returned code ${momoCode}: payment was not completed.`);
                }
                // Clear sessionStorage then navigate to order detail in both cases
                sessionStorage.removeItem('pendingOrderId');
                sessionStorage.removeItem('pendingPaymentMethod');
                if (orderId) {
                    setTimeout(() => navigate(`/orders/${orderId}`, { replace: true }), 2500);
                }
            }
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [isMock]);

    // ── Mock: confirmPayment endpoint still used for simulated flow ───────
    const handleMockConfirm = async (success) => {
        if (!orderId || !paymentId) {
            setError('Missing order information.');
            return;
        }
        setStatus('processing');
        try {
            const { confirmPayment: confirm } = await import('@/features/orders/services/order.service');
            await confirm(orderId, paymentId, success);
            setStatus(success ? 'success' : 'failed');
            setTimeout(() => navigate(`/orders/${orderId}`, { replace: true }), 1500);
        } catch (err) {
            setError(err?.response?.data?.message || 'Could not confirm payment.');
            setStatus('failed');
        }
    };

    // ── Mock gateway UI ───────────────────────────────────
    if (isMock && status === null) {
        return (
            <>
                <Navbar />
                <div className={styles.page}>
                    <div className={styles.gatewayCard}>
                        <div className={styles.gatewayLogo}>
                            {METHOD_LABELS[method] || method}
                        </div>
                        <h2 className={styles.gatewayTitle}>Payment Gateway (Simulated)</h2>
                        <p className={styles.gatewayDesc}>
                            This is a mock payment page. In production this would be the real {METHOD_LABELS[method]} checkout.
                        </p>
                        <div className={styles.gatewayActions}>
                            <button
                                className={styles.successBtn}
                                onClick={() => handleMockConfirm(true)}
                            >
                                ✓ Simulate Success
                            </button>
                            <button
                                className={styles.failBtn}
                                onClick={() => handleMockConfirm(false)}
                            >
                                ✕ Simulate Failure
                            </button>
                        </div>
                        <button
                            className={styles.backLink}
                            onClick={() => navigate(`/orders/${orderId}`)}
                        >
                            ← Cancel &amp; go to order
                        </button>
                    </div>
                </div>
            </>
        );
    }

    // ── Processing / result ───────────────────────────────
    return (
        <>
            <Navbar />
            <div className={styles.page}>
                <div className={styles.resultCard}>
                    {status === 'processing' && (
                        <>
                            <div className={styles.spinner} />
                            <p className={styles.resultMsg}>Confirming payment…</p>
                        </>
                    )}
                    {status === 'success' && (
                        <>
                            <div className={styles.resultIcon + ' ' + styles.iconSuccess}>✓</div>
                            <h2 className={styles.resultTitle}>Payment Successful</h2>
                            <p className={styles.resultMsg}>Redirecting to your order…</p>
                        </>
                    )}
                    {status === 'failed' && (
                        <>
                            <div className={styles.resultIcon + ' ' + styles.iconFailed}>✕</div>
                            <h2 className={styles.resultTitle}>Payment Failed</h2>
                            <p className={styles.resultMsg}>
                                {error || 'Your payment was not completed.'}
                            </p>
                            <p className={styles.resultMsg}>Redirecting back to your order…</p>
                            <button
                                className={styles.retryBtn}
                                onClick={() => navigate(`/orders/${orderId}`, { replace: true })}
                            >
                                View Order &amp; Retry
                            </button>
                        </>
                    )}
                </div>
            </div>
        </>
    );
};

export default PaymentResultPage;
