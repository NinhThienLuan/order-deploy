import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useCart } from '@/features/orders/services/CartContext';
import { useAuth } from '@/features/authentication/services/AuthContext';
import Navbar from '@/layouts/Navbar';
import styles from './CartPage.module.css';

// Modular Components
import CartItem from '@/features/orders/components/CartItem';
import CartSummary from '@/features/orders/components/CartSummary';

const CartPage = () => {
    const { items, updateQty, removeItem, totalItems, changeVariant } = useCart();
    const { user } = useAuth();
    const [note, setNote] = useState('');
    const navigate = useNavigate();

    const subtotal = items.reduce((s, i) => s + i.price * i.quantity, 0);
    // No extra fees as requested by user
    const grandTotal = subtotal;

    const handleCheckout = () => {
        if (!items.length) return;
        if (!user) {
            navigate('/login', { state: { from: { pathname: '/cart' } } });
            return;
        }
        navigate('/checkout', { state: { note } });
    };

    return (
        <>
            <Navbar />
            <div className={styles.page}>
                <div className={styles.header}>
                    <h1 className={styles.pageTitle}>Your Cart</h1>
                    <p className={styles.pageSubtitle}>
                        {totalItems > 0
                            ? `${totalItems} items pending checkout`
                            : 'No items yet'}
                    </p>
                </div>

                {items.length === 0 ? (
                    <div className={styles.card}>
                        <div className={styles.empty}>
                            <span className={styles.emptyIcon}>☕</span>
                            <p>Your cart is empty — choose your favorite drinks!</p>
                            <Link to="/#coffees" className={styles.backBtn}>View Menu</Link>
                        </div>
                    </div>
                ) : (
                    <div className={styles.layout}>
                        <div className={styles.mainContent}>
                            <div className={styles.card}>
                                <div className={styles.sectionTitle}>Selected Items</div>
                                {items.map((item) => (
                                    <CartItem
                                        key={item.id}
                                        item={item}
                                        onUpdateQty={updateQty}
                                        onRemove={removeItem}
                                        onChangeVariant={changeVariant}
                                    />
                                ))}
                            </div>

                            <div className={styles.card}>
                                <label className={styles.noteLabel} htmlFor="order-note">Order Note</label>
                                <textarea
                                    id="order-note"
                                    className={styles.noteInput}
                                    placeholder="E.g: Less sugar, no ice, deliver in 30 mins..."
                                    value={note}
                                    onChange={(e) => setNote(e.target.value)}
                                />
                            </div>
                        </div>

                        <CartSummary
                            subtotal={subtotal}
                            itemCount={totalItems}
                            grandTotal={grandTotal}
                            hasItems={items.length > 0}
                            onCheckout={handleCheckout}
                        />
                    </div>
                )}
            </div>
        </>
    );
};

export default CartPage;
