import { useState } from 'react';
import styles from './CancelOrderButton.module.css';
import { cancelOrder } from '../../orders.service';
import { CANCEL_BLOCKED_STATUSES } from '../../orders.constants';

/**
 * CancelOrderButton
 * @param {{ orderId: string|number, currentStatus: string, onCancelled: function }} props
 */
const CancelOrderButton = ({ orderId, currentStatus, onCancelled }) => {
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [success, setSuccess] = useState(false);

    const isBlocked = CANCEL_BLOCKED_STATUSES.includes(currentStatus);

    const handleCancel = async () => {
        // Guard: show inline error if status blocks cancellation
        if (isBlocked) {
            setError('Order is being prepared and cannot be cancelled at this time.');
            return;
        }

        setLoading(true);
        setError(null);

        try {
            await cancelOrder(orderId);
            setSuccess(true);
            if (onCancelled) onCancelled();
        } catch (err) {
            setError(
                err?.response?.data?.message ||
                'An error occurred while cancelling the order. Please try again.'
            );
        } finally {
            setLoading(false);
        }
    };

    if (success) {
        return (
            <div className={styles.successBox}>
                <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                    <circle cx="8" cy="8" r="7" stroke="currentColor" strokeWidth="1.5" />
                    <path d="M5 8l2.5 2.5L11 6" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" />
                </svg>
                Order has been successfully cancelled.
            </div>
        );
    }

    return (
        <div className={styles.wrapper}>
            <button
                className={`${styles.cancelBtn} ${loading ? styles.loading : ''}`}
                onClick={handleCancel}
                disabled={loading}
                aria-label="Cancel Order"
            >
                {loading ? (
                    'Processing...'
                ) : (
                    <>
                        <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
                            <circle cx="7" cy="7" r="6" stroke="currentColor" strokeWidth="1.5" />
                            <path d="M4.5 4.5l5 5M9.5 4.5l-5 5" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" />
                        </svg>
                        CANCEL ORDER
                    </>
                )}
            </button>

            {error && (
                <div className={styles.errorBox} role="alert">
                    <svg className={styles.errorIcon} width="16" height="16" viewBox="0 0 16 16" fill="none">
                        <circle cx="8" cy="8" r="7" stroke="currentColor" strokeWidth="1.5" />
                        <path d="M8 5v4" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" />
                        <circle cx="8" cy="11.5" r="0.75" fill="currentColor" />
                    </svg>
                    {error}
                </div>
            )}
        </div>
    );
};

export default CancelOrderButton;
