import { useState } from 'react';
import { cancelOrder } from '@/features/orders/services/order.service';
import Button from '@/components/ui/Button';
import styles from './CancelOrderButton.module.css';

const CancelOrderButton = ({ orderId, currentStatus, onCancelled }) => {
    const [submitting, setSubmitting] = useState(false);
    const [error, setError] = useState(null);
    const [showConfirm, setShowConfirm] = useState(false);

    const handleCancel = async () => {
        setSubmitting(true);
        setError(null);
        try {
            await cancelOrder(orderId);
            if (onCancelled) onCancelled();
            setShowConfirm(false);
        } catch (err) {
            setError(err.message || 'Failed to cancel order');
        } finally {
            setSubmitting(false);
        }
    };

    if (currentStatus !== 'PENDING' && currentStatus !== 'PROCESSING') {
        return null;
    }

    if (showConfirm) {
        return (
            <div className={styles.confirmBox}>
                <p className={styles.confirmText}>Are you sure you want to cancel this order?</p>
                {error && <p className={styles.errorText}>{error}</p>}
                <div className={styles.actions}>
                    <Button 
                        variant="ghost" 
                        size="sm" 
                        onClick={() => setShowConfirm(false)}
                        disabled={submitting}
                    >
                        Keep Order
                    </Button>
                    <Button 
                        variant="danger" 
                        size="sm" 
                        onClick={handleCancel}
                        disabled={submitting}
                    >
                        {submitting ? 'Cancelling...' : 'Confirm Cancel'}
                    </Button>
                </div>
            </div>
        );
    }

    return (
        <Button 
            variant="outline" 
            className={styles.cancelBtn}
            onClick={() => setShowConfirm(true)}
        >
            Cancel Order
        </Button>
    );
};

export default CancelOrderButton;
