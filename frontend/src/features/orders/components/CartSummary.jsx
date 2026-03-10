import { Link } from 'react-router-dom';
import styles from '@/features/orders/pages/CartPage.module.css';

const formatVND = (n) =>
    new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(n ?? 0);

const CartSummary = ({
    subtotal,
    itemCount,
    grandTotal,
    hasItems,
    error,
    onCheckout
}) => {
    return (
        <div className={styles.summaryPanel}>
            <div className={styles.card}>
                <div className={styles.sectionTitle}>Order Summary</div>

                <div className={styles.summaryRow}>
                    <span className={styles.summaryLabel}>Subtotal ({itemCount} {itemCount === 1 ? 'item' : 'items'})</span>
                    <span className={styles.summaryValue}>{formatVND(subtotal)}</span>
                </div>

                <div className={styles.summaryRow}>
                    <span className={styles.summaryLabel}>Shipping</span>
                    <span className={styles.summaryValue} style={{ fontSize: '0.85rem' }}>Calculated at checkout</span>
                </div>

                <div className={`${styles.summaryRow} ${styles.totalRow}`}>
                    <span>Total</span>
                    <span className={styles.totalDisplay}>{formatVND(grandTotal)}</span>
                </div>
            </div>

            <div className={styles.card}>
                {error && <div className={styles.errorMsg}>{error}</div>}

                <button
                    className={styles.confirmBtn}
                    onClick={onCheckout}
                    disabled={!hasItems}
                >
                    PROCEED TO CHECKOUT
                </button>

                <p className={styles.confirmNote}>
                    By placing an order, you agree to our terms and conditions.
                </p>
            </div>
        </div>
    );
};

export default CartSummary;
