import { useState, useEffect, useRef } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useCart } from '@/features/orders/services/CartContext';
import { useAuth } from '@/features/authentication/services/AuthContext';
import styles from './CheckoutPage.module.css';
import { createOrder } from '@/features/orders/services/order.service';
import { getPaymentMethods } from '@/features/payments/services/payments.service';

const formatVND = (n) =>
    new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(n ?? 0);

const DELIVERY_FEE = 0;

// Fallback displayed while methods are loading or if API fails
const FALLBACK_METHODS = [
    { code: 'VNPAY', name: 'VNPay', description: 'Pay via VNPay gateway', subOptions: {} },
    { code: 'MOMO',  name: 'MoMo',  description: 'Pay via MoMo wallet',   subOptions: {
        captureWallet: { enabled: true, name: 'QR / MoMo Wallet', description: 'Scan QR or open MoMo app' },
        payWithATM:    { enabled: true, name: 'ATM / Bank transfer',  description: 'Domestic ATM card' },
        payWithCC:     { enabled: true, name: 'International card', description: 'Visa / Mastercard / JCB' },
    }},
];

const CheckoutPage = () => {
    const { items, clearCart } = useCart();
    const { user } = useAuth();
    const navigate = useNavigate();

    const [address, setAddress] = useState(user?.address || '');
    const [paymentMethods, setPaymentMethods] = useState(FALLBACK_METHODS);
    const [paymentMethod, setPaymentMethod] = useState('VNPAY');
    const [momoRequestType, setMomoRequestType] = useState('captureWallet');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    // Guard: don't let the API response overwrite a method the user already picked
    const userPickedMethodRef = useRef(false);

    // Fetch available payment methods from backend
    useEffect(() => {
        getPaymentMethods()
            .then((methods) => {
                if (Array.isArray(methods) && methods.length > 0) {
                    setPaymentMethods(methods);
                    if (!userPickedMethodRef.current) {
                        setPaymentMethod(methods[0].code);
                    }
                }
            })
            .catch(() => {
                // silently keep fallback methods
            });
    }, []);

    const subtotal = items.reduce((s, i) => s + i.price * i.quantity, 0);
    const totalItems = items.reduce((s, i) => s + i.quantity, 0);
    const grandTotal = subtotal + DELIVERY_FEE;

    if (items.length === 0) {
        return (
            <div className={styles.page}>
                <div className={styles.emptyCard}>
                    <h2>Empty Cart</h2>
                    <p className={styles.mutedText}>
                        There are no items to checkout.
                    </p>
                    <Link to="/cart" className={styles.primaryBtn} style={{ maxWidth: '200px', margin: '0 auto', textDecoration: 'none' }}>
                        Back to cart
                    </Link>
                </div>
            </div>
        );
    }

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError(null);
        setLoading(true);
        try {
            const orderPayload = {
                items: items.map((i) => ({
                    productId: i.id,
                    quantity: i.quantity,
                })),
                deliveryAddress: address || null,
            };
            const newOrder = await createOrder(orderPayload);
            clearCart();

            // Persist orderId so result pages can read it after gateway redirect
            sessionStorage.setItem('pendingOrderId', newOrder.orderId);
            sessionStorage.setItem('pendingPaymentMethod', paymentMethod);

            navigate('/payment/loading', {
                replace: true,
                state: {
                    orderId: newOrder.orderId,
                    paymentMethod,
                    amount: newOrder.totalAmount,
                    ...(paymentMethod === 'MOMO' && { momoRequestType }),
                },
            });
        } catch (err) {
            const msg = err?.response?.data?.message || err?.message || 'Unknown error';
            console.error('[Checkout] failed:', err?.response?.data || err);
            setError(`Order failed: ${msg}`);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className={styles.page}>
            <form id="checkout-form" onSubmit={handleSubmit} className={styles.layout}>
                {/* Left + Middle Grouped for Focus */}
                <div className={styles.formSection}>
                    <h1 className={styles.mainHeading}>Order Information</h1>

                    <div className={styles.formColumns}>
                        {/* Column 1: Info */}
                        <div className={styles.column}>
                            <h2 className={styles.sectionTitle}>Your Information</h2>

                            <div className={styles.formGroup}>
                                <div className={styles.infoReadOnly}>
                                    <span className={styles.infoLabel}>Name</span>
                                    <span className={styles.infoValue}>{user?.name || '—'}</span>
                                </div>
                            </div>
                            <div className={styles.formGroup}>
                                <div className={styles.infoReadOnly}>
                                    <span className={styles.infoLabel}>Phone</span>
                                    <span className={styles.infoValue}>{user?.phone || '—'}</span>
                                </div>
                            </div>
                            <div className={styles.formGroup}>
                                <div className={styles.infoReadOnly}>
                                    <span className={styles.infoLabel}>Email</span>
                                    <span className={styles.infoValue}>{user?.email || '—'}</span>
                                </div>
                            </div>
                            <div className={styles.formGroup}>
                                <input
                                    type="text"
                                    className={styles.input}
                                    placeholder="Delivery Address"
                                    value={address}
                                    onChange={(e) => setAddress(e.target.value)}
                                />
                            </div>
                        </div>

                        {/* Column 2: Methods */}
                        <div className={styles.column}>
                            <div className={styles.section}>
                                <h2 className={styles.sectionTitle}>Shipping Method</h2>
                                <div className={styles.radioCardBorder}>
                                    <div className={styles.radioOption}>
                                        <input type="radio" id="ship-delivery" name="shipping" checked readOnly />
                                        <label htmlFor="ship-delivery">
                                            Delivery <span className={styles.mutedText}>Miễn phí</span>
                                        </label>
                                    </div>
                                </div>
                            </div>

                            <div className={styles.section} style={{ marginTop: '2.5rem' }}>
                                <h2 className={styles.sectionTitle}>Payment Method</h2>
                                <div className={styles.radioCardBorder}>
                                    {paymentMethods.map((m) => (
                                        <div className={styles.radioOption} key={m.code}>
                                            <input
                                                type="radio"
                                                id={`pay-${m.code}`}
                                                name="payment"
                                                value={m.code}
                                                checked={paymentMethod === m.code}
                                                onChange={() => {
                                                    userPickedMethodRef.current = true;
                                                    setPaymentMethod(m.code);
                                                    // Reset to first enabled sub-option when switching to MOMO
                                                    if (m.code === 'MOMO' && m.subOptions) {
                                                        const first = Object.entries(m.subOptions).find(([, v]) => v.enabled);
                                                        if (first) setMomoRequestType(first[0]);
                                                    }
                                                }}
                                            />
                                            <label htmlFor={`pay-${m.code}`}>
                                                {m.name}
                                                {m.description && (
                                                    <span className={styles.mutedText}> — {m.description}</span>
                                                )}
                                            </label>
                                        </div>
                                    ))}
                                </div>

                                {/* MoMo sub-options */}
                                {paymentMethod === 'MOMO' && (() => {
                                    const momoMethod = paymentMethods.find((m) => m.code === 'MOMO');
                                    const subs = momoMethod?.subOptions
                                        ? Object.entries(momoMethod.subOptions).filter(([, v]) => v.enabled)
                                        : [];
                                    return subs.length > 1 ? (
                                        <div className={styles.radioCardBorder} style={{ marginTop: '0.75rem', marginLeft: '1.25rem' }}>
                                            <p className={styles.mutedText} style={{ padding: '0.25rem 0.75rem', fontSize: '0.8rem', fontWeight: 600 }}>
                                                Payment channel
                                            </p>
                                            {subs.map(([key, sub]) => (
                                                <div className={styles.radioOption} key={key}>
                                                    <input
                                                        type="radio"
                                                        id={`momo-${key}`}
                                                        name="momoType"
                                                        value={key}
                                                        checked={momoRequestType === key}
                                                        onChange={() => setMomoRequestType(key)}
                                                    />
                                                    <label htmlFor={`momo-${key}`}>
                                                        {sub.name}
                                                        {sub.description && (
                                                            <span className={styles.mutedText}> — {sub.description}</span>
                                                        )}
                                                    </label>
                                                </div>
                                            ))}
                                        </div>
                                    ) : null;
                                })()}
                            </div>
                        </div>
                    </div>
                </div>

                {/* Column 3: Order Summary */}
                <div className={`${styles.column} ${styles.summaryColumn}`}>
                    <div className={styles.summaryCard}>
                        <h2 className={styles.summaryTitle}>Order ({totalItems} items)</h2>

                        <div className={styles.itemList}>
                            {items.map((item) => (
                                <div key={item.id} className={styles.itemRow}>
                                    <div className={styles.itemThumbWrap}>
                                        <img src={item.imageUrl} alt={item.productName} className={styles.itemThumb} />
                                        <span className={styles.itemBadge}>{item.quantity}</span>
                                    </div>
                                    <div className={styles.itemInfo}>
                                        <div className={styles.itemName}>{item.productName}</div>
                                        <div className={styles.itemCategory}>{item.category}</div>
                                    </div>
                                    <div className={styles.itemPriceText}>{formatVND(item.price * item.quantity)}</div>
                                </div>
                            ))}
                        </div>

                        <div className={styles.calcRows}>
                            <div className={styles.calcRow}>
                                <span className={styles.calcLabel}>Subtotal</span>
                                <span className={styles.calcValue}>{formatVND(subtotal)}</span>
                            </div>
                            <div className={styles.calcRow}>
                                <span className={styles.calcLabel}>Shipping</span>
                                <span className={styles.calcValue}>Free</span>
                            </div>
                        </div>

                        <div className={styles.totalRow}>
                            <span className={styles.totalLabel}>Total</span>
                            <span className={styles.totalValue}>{formatVND(grandTotal)}</span>
                        </div>

                        <div className={styles.actionsRow}>
                            <button
                                type="submit"
                                className={styles.primaryBtn}
                                disabled={loading || items.length === 0}
                            >
                                {loading ? 'Processing...' : 'PLACE ORDER'}
                            </button>
                            <Link to="/cart" className={styles.backLink}>
                                &lt; Back to cart
                            </Link>
                        </div>

                        {error && <div className={styles.errorMsg}>{error}</div>}
                    </div>
                </div>
            </form>
        </div>
    );
};

export default CheckoutPage;
