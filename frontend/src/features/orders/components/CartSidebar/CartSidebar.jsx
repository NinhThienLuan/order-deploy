import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useCart } from '@/features/orders/services/CartContext';
import { useAuth } from '@/features/authentication/services/AuthContext';
import CartItemVariantDropdown from './CartItemVariantDropdown';
import styles from './CartSidebar.module.css';

const formatVND = (n) =>
    new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(n ?? 0);

const CartSidebar = () => {
    const { items, totalItems, subtotal, isSidebarOpen, closeSidebar, updateQty, removeItem, changeVariant, clearCart } = useCart();
    const { user } = useAuth();
    const navigate = useNavigate();
    const [confirming, setConfirming] = useState(false);

    const handleConfirm = () => {
        if (!items.length) return;
        closeSidebar();
        if (!user) {
            navigate('/login', { state: { from: { pathname: '/cart' } } });
            return;
        }
        navigate('/checkout');
    };

    return (
        <>
            {/* Backdrop */}
            <div
                className={`${styles.backdrop} ${isSidebarOpen ? styles.open : ''}`}
                onClick={closeSidebar}
                aria-hidden="true"
            />

            {/* Drawer */}
            <aside
                className={`${styles.drawer} ${isSidebarOpen ? styles.open : ''}`}
                aria-label="Cart"
                role="dialog"
                aria-modal="true"
            >
                {/* Header */}
                <div className={styles.header}>
                    <h2 className={styles.title}>
                        Cart
                        {totalItems > 0 && <span className={styles.badge}>{totalItems}</span>}
                    </h2>
                    <button className={styles.closeBtn} onClick={closeSidebar} aria-label="Close cart">
                        <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                            <path d="M15 5L5 15M5 5l10 10" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" />
                        </svg>
                    </button>
                </div>

                {/* Items */}
                {items.length === 0 ? (
                    <div className={styles.empty}>
                        <span className={styles.emptyIcon}>☕</span>
                        <span>No items yet</span>
                    </div>
                ) : (
                    <div className={styles.itemList}>
                        {items.map((item) => {
                            const itemKey = `${item.id}-${item.variantId || 'default'}`;
                            return (
                                <div key={itemKey} className={styles.itemWrapper}>
                                    <div className={styles.itemRow}>
                                        <img
                                            src={item.imageUrl}
                                            alt={item.productName}
                                            className={styles.thumb}
                                            onError={(e) => { e.target.src = 'https://images.unsplash.com/photo-1510591509098-f4fdc6d0ff04?auto=format&fit=crop&w=80&q=60'; }}
                                        />

                                        <div className={styles.itemMain}>
                                            <div className={styles.itemName}>
                                                {item.productName}
                                            </div>
                                            
                                            <div className={styles.itemControls}>
                                                <div className={styles.variantSelectorContainer}>
                                                    <CartItemVariantDropdown 
                                                        productId={item.id}
                                                        currentVariantId={item.variantId}
                                                        onChange={(newVariant) => changeVariant(item.id, item.variantId, newVariant)}
                                                    />
                                                </div>

                                                <div className={styles.qtyBlock}>
                                                    <button
                                                        className={styles.qtyBtn}
                                                        onClick={() => updateQty(item.id, item.quantity - 1, item.variantId)}
                                                        aria-label="Decrease"
                                                    >−</button>
                                                    <span className={styles.qty}>{item.quantity}</span>
                                                    <button
                                                        className={styles.qtyBtn}
                                                        onClick={() => updateQty(item.id, item.quantity + 1, item.variantId)}
                                                        aria-label="Increase"
                                                    >+</button>
                                                </div>

                                                <div className={styles.linePrice}>
                                                    {formatVND(item.price * item.quantity)}
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            );
                        })}
                    </div>
                )}

                {/* Footer */}
                {items.length > 0 && (
                    <div className={styles.footer}>
                        <div className={styles.subtotalRow}>
                            <span className={styles.subtotalLabel}>Subtotal</span>
                            <span className={styles.subtotalValue}>{formatVND(subtotal)}</span>
                        </div>

                        {/* Go to full cart page */}
                        <Link
                            to="/cart"
                            className={styles.confirmBtn}
                            style={{ display: 'block', textAlign: 'center', textDecoration: 'none', marginBottom: '0.5rem', background: 'var(--color-bg-offset)', color: 'var(--color-text-main)', border: '1px solid var(--color-border-default)' }}
                            onClick={closeSidebar}
                        >
                            View Cart &amp; Checkout
                        </Link>

                        <button
                            className={`${styles.confirmBtn} ${confirming ? styles.loading : ''}`}
                            onClick={handleConfirm}
                            disabled={confirming}
                        >
                            {confirming ? 'Processing...' : 'Confirm Order'}
                        </button>

                        <button className={styles.clearBtn} onClick={clearCart}>
                            Clear Cart
                        </button>
                    </div>
                )}
            </aside>
        </>
    );
};

export default CartSidebar;
