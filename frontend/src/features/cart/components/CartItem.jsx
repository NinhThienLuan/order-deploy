import styles from '../pages/CartPage.module.css';

const formatVND = (n) =>
    new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(n ?? 0);

const CartItem = ({ item, onUpdateQty, onRemove }) => {
    return (
        <div className={styles.itemRow}>
            <img
                src={item.imageUrl}
                alt={item.productName}
                className={styles.thumb}
                onError={(e) => {
                    e.target.src =
                        'https://images.unsplash.com/photo-1510591509098-f4fdc6d0ff04?auto=format&fit=crop&w=80&q=60';
                }}
            />
            <div className={styles.itemInfo}>
                <div className={styles.itemName}>{item.productName}</div>
                {item.note && (
                    <div className={styles.itemNote}>
                        📝 {item.note}
                    </div>
                )}
                <div className={styles.unitPrice}>{formatVND(item.price)}</div>
            </div>

            <div className={styles.qtyBlock}>
                <button
                    className={styles.qtyBtn}
                    onClick={() => {
                        if (item.quantity > 1) onUpdateQty(item.id, item.quantity - 1);
                    }}
                >
                    −
                </button>
                <span className={styles.qty}>{item.quantity}</span>
                <button
                    className={styles.qtyBtn}
                    onClick={() => onUpdateQty(item.id, item.quantity + 1)}
                >
                    +
                </button>
            </div>

            <div className={styles.linePrice}>{formatVND(item.price * item.quantity)}</div>

            <button
                className={styles.removeBtn}
                onClick={() => onRemove(item.id)}
                aria-label="Remove item"
            >
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
                    <line x1="18" y1="6" x2="6" y2="18"></line>
                    <line x1="6" y1="6" x2="18" y2="18"></line>
                </svg>
            </button>
        </div>
    );
};

export default CartItem;
