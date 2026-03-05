import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import styles from './OrderDetailPage.module.css';
import { getOrderById } from '../orders.service';
import { STEPPER_STEPS } from '../orders.constants';
import CancelOrderButton from '../components/CancelOrderButton/CancelOrderButton';
import Navbar from '../../../layouts/Navbar';

// ── Formatters ────────────────────────────────────────────
const formatVND = (n) =>
    new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(n ?? 0);

const formatDateTime = (iso) =>
    iso
        ? new Date(iso).toLocaleString('en-US', {
            day: '2-digit', month: '2-digit', year: 'numeric',
            hour: '2-digit', minute: '2-digit',
        })
        : '—';

// ── Mock data ─────────────────────────────────────────────
const MOCK_ORDER = {
    id: 'ORD-274186',
    createdAt: '2026-02-26T10:57:00Z',
    status: 'PREPARING',
    trackingRef: 'TRK-2026-ORD-274186',
    seller: { name: 'E-Coffee Flagship Store', address: 'Floor 1, 123 Le Loi\nDistrict 1, HCMC\nVietnam' },
    buyer: { name: 'Customer', address: 'E.g: 45B Tran Hung Dao\nDistrict 5, HCMC\nVietnam' },
    items: [
        {
            id: 1, name: 'Traditional Iced Coffee', category: 'Coffee / Traditional',
            note: 'Less sugar | Size: Large', quantity: 2, unitPrice: 35000,
            image: 'https://images.unsplash.com/photo-1544233726-9f1d2b27be8b?auto=format&fit=crop&w=80&q=60',
        },
        {
            id: 2, name: 'Cold Cut Banh Mi', category: 'Food / Baguette',
            note: 'No onions | Size: Regular', quantity: 1, unitPrice: 45000,
            image: 'https://images.unsplash.com/photo-1497633762265-9d179a990aa6?auto=format&fit=crop&w=80&q=60',
        },
        {
            id: 3, name: 'Peach Lemongrass Tea', category: 'Tea / Non-alcoholic',
            note: 'No ice | Size: Medium', quantity: 1, unitPrice: 40000,
            image: 'https://images.unsplash.com/photo-1511920170033-f8396924c348?auto=format&fit=crop&w=80&q=60',
        },
    ],
    timeline: [
        { event: 'Order received by store', date: '02/26/2026 10:57', status: 'done' },
        { event: 'Store confirmed order', date: '02/26/2026 10:57', status: 'done' },
        { event: 'Preparing drinks', date: '02/26/2026 10:57', status: 'active' },
        { event: 'Ready for pickup', date: '—', status: 'pending' },
        { event: 'Completed', date: '—', status: 'pending' },
    ],
};

const STATUS_LABEL_MAP = {
    PENDING: 'Order Placed',
    PAID: 'Payment Confirmed',
    PREPARING: 'Preparing',
    READY: 'Ready for Pickup',
    COMPLETED: 'Completed',
    CANCELED: 'Cancelled',
};

// ─────────────────────────────────────────────────────────

const OrderDetailPage = () => {
    const { id } = useParams();
    const navigate = useNavigate();
    const [order, setOrder] = useState(null);
    const [loading, setLoading] = useState(true);
    const [fetchError, setFetchError] = useState(null);
    const [paymentMethod, setPaymentMethod] = useState('VNPAY');
    const [momoRequestType, setMomoRequestType] = useState('captureWallet');

    useEffect(() => {
        const load = async () => {
            setLoading(true);
            try {
                const data = await getOrderById(id);
                setOrder(data);
            } catch {
                setFetchError('Could not load order.');
            } finally {
                setLoading(false);
            }
        };
        load();
    }, [id]);

    const handleCancelled = () => setOrder((prev) => ({ ...prev, status: 'CANCELED' }));

    const handlePayNow = () => {
        const orderId = order.orderId || order.id;
        const amount  = order.pricing?.totalAmount || order.totalAmount || 0;
        sessionStorage.setItem('pendingOrderId', String(orderId));
        sessionStorage.setItem('pendingPaymentMethod', paymentMethod);
        navigate('/payment/loading', {
            state: {
                orderId,
                paymentMethod,
                amount,
                ...(paymentMethod === 'MOMO' && { momoRequestType }),
            },
        });
    };

    if (loading)
        return (
            <>
                <Navbar />
                <div className={styles.page} style={{ paddingTop: '100px' }}>
                    <div className={styles.loadingState}>
                        <div className={styles.spinner} />
                        <span>Loading order...</span>
                    </div>
                </div>
            </>
        );

    if (fetchError || !order)
        return (
            <>
                <Navbar />
                <div className={styles.page} style={{ paddingTop: '100px' }}>
                    <div className={styles.errorState}>⚠️ {fetchError || 'Order not found.'}</div>
                </div>
            </>
        );

    const subtotal = order.pricing?.subtotal || 0;
    const deliveryFee = order.pricing?.deliveryFee || 0;
    const platformFee = order.pricing?.platformFee || 0;
    const discount = order.pricing?.discount || 0;
    const grandTotal = order.pricing?.totalAmount || 0;
    const currentStepIdx = STEPPER_STEPS.findIndex((s) => s.key === order.status);
    const totalItems = order.items?.reduce((s, i) => s + i.quantity, 0) || 0;
    const dotClass = (status) =>
        status === 'active' ? styles.active : status === 'done' ? styles.done : '';

    return (
        <>
            <Navbar />
            <div className={styles.page} style={{ paddingTop: '100px' }}>
                {/* Header */}
                <div className={styles.header}>
                    <div>
                        <h1 className={styles.pageTitle}>Order ID : {order.orderNumber || order.orderId || order.id || 'N/A'}</h1>
                        <p className={styles.pageSubtitle}>Placed at {formatDateTime(order.orderTime || order.createdAt)}</p>
                    </div>
                </div>

                <div className={styles.layout}>
                    {/* ── Main column ── */}
                    <div>
                        {/* Status bar */}
                        <div className={`${styles.statusBar} ${order.status === 'PREPARING' ? styles.statusBarPreparing : order.status === 'COMPLETED' ? styles.statusBarDone : order.status === 'READY' ? styles.statusBarReady : ''}`}>
                            <div className={styles.statusIndicator}>
                                <span
                                    className={`${styles.statusDot} ${order.status === 'COMPLETED' ? styles.done :
                                        order.status === 'READY' ? styles.ready :
                                        order.status === 'PREPARING' ? styles.preparing : ''
                                        }`}
                                />
                                {STATUS_LABEL_MAP[order.status] ?? order.status}
                            </div>
                            <span className={styles.trackingRef}>Tracking ID: {order.trackingRef || order.orderNumber || order.id}</span>
                        </div>

                        {/* Stepper */}
                        <div className={`${styles.stepperCard} ${order.status === 'PREPARING' ? styles.stepperCardPreparing : ''}`}>
                            <div className={styles.stepper}>
                                {STEPPER_STEPS.map((step, idx) => {
                                    const isDone = idx < currentStepIdx;
                                    const isActive = idx === currentStepIdx;
                                    let cls = styles.step;
                                    if (isDone) cls += ` ${styles.done}`;
                                    if (isActive) cls += ` ${styles.active}`;
                                    return (
                                        <div key={step.key} className={cls}>
                                            <div className={styles.stepIcon}></div>
                                            <span className={styles.stepLabel}>{step.label}</span>
                                            <span className={styles.stepSub}>
                                                {isDone ? 'Done' : isActive ? 'In Progress' : 'Pending'}
                                            </span>
                                        </div>
                                    );
                                })}
                            </div>
                        </div>

                        {/* Addresses */}
                        <div className={styles.card}>
                            <div className={styles.addressGrid}>
                                {[
                                    { title: 'Store Address (Seller)', data: { name: 'E-Coffee Flagship Store', address: 'Floor 1, 123 Le Loi\nDistrict 1, HCMC\nVietnam' } },
                                    { title: 'Customer Address (Buyer)', data: { name: order.customer?.customerName || 'Customer', address: order.customer?.deliveryAddress || 'Pick-up at store' } },
                                ].map(({ title, data }) => (
                                    <div key={title} className={styles.addressBlock}>
                                        <div className={styles.addressTitle}>
                                            {title}
                                            <button className={styles.editBtn} title="Edit">
                                                <svg width="12" height="12" viewBox="0 0 12 12" fill="none">
                                                    <path d="M8.5 1.5l2 2L3 11H1V9L8.5 1.5z" stroke="currentColor" strokeWidth="1.2" strokeLinejoin="round" />
                                                </svg>
                                            </button>
                                        </div>
                                        <div className={styles.addressName}>{data.name}</div>
                                        <div className={styles.addressText} style={{ whiteSpace: 'pre-line' }}>
                                            {data.address}
                                        </div>
                                    </div>
                                ))}
                            </div>
                        </div>

                        {/* Order Items */}
                        <div className={styles.card}>
                            <div className={styles.sectionTitle}>Order Item</div>
                            {order.items?.map((item, index) => (
                                <div key={item.productId || index} className={styles.itemRow}>
                                    <img src={item.imageUrl || item.image || 'https://images.unsplash.com/photo-1544233726-9f1d2b27be8b?auto=format&fit=crop&w=80&q=60'} alt={item.productName || item.name} className={styles.itemThumb} />
                                    <div className={styles.itemInfo}>
                                        <div className={styles.itemCategory}>{item.category || 'Beverage'}</div>
                                        <div className={styles.itemName}>{item.productName || item.name}</div>
                                        <div className={styles.itemMeta}>{item.note || ''}</div>
                                    </div>
                                    <div>
                                        <div className={styles.itemQty}>{item.quantity} x {formatVND(item.unitPrice || item.price)}</div>
                                        <div className={styles.itemPrice}>{formatVND(item.subtotal || ((item.unitPrice || item.price) * item.quantity))}</div>
                                    </div>
                                </div>
                            ))}
                        </div>

                        {/* Summary */}
                        <div className={styles.card}>
                            <div className={styles.sectionTitle}>Order Summary</div>
                            {[
                                { label: 'Product Price', meta: `${totalItems} Item`, value: subtotal },
                                { label: 'Shipping', value: deliveryFee },
                                platformFee > 0 ? { label: 'Platform Fee', value: platformFee } : null,
                                discount > 0 ? { label: 'Discount', value: -discount, danger: true } : null,
                            ].filter(Boolean).map(({ label, meta, value, danger }) => (
                                <div className={styles.summaryRow} key={label}>
                                    <div>
                                        <div className={styles.summaryLabel}>{label}</div>
                                        {meta && <div className={styles.summaryMeta}>{meta}</div>}
                                    </div>
                                    <div className={`${styles.summaryValue} ${danger ? styles.statusDanger : ''}`}>
                                        {value === 0 ? 'Free' : formatVND(value)}
                                    </div>
                                </div>
                            ))}
                            <div className={`${styles.summaryRow} ${styles.summaryTotal}`}>
                                <span>Total</span>
                                <span>{formatVND(grandTotal)}</span>
                            </div>
                        </div>

                        {/* Payment Information */}
                        {order.payment && (
                            <div className={styles.card}>
                                <div className={styles.sectionTitle}>Payment Details</div>
                                <div className={styles.summaryRow}>
                                    <div className={styles.summaryLabel}>Payment Method</div>
                                    <div className={styles.summaryValue}>{order.payment.paymentMethod}</div>
                                </div>
                                <div className={styles.summaryRow}>
                                    <div className={styles.summaryLabel}>Status</div>
                                    <div className={`${styles.summaryValue} ${order.payment.paymentStatus === 'Paid' ? styles.statusSuccess : styles.statusWarning}`}>
                                        {order.payment.paymentStatus}
                                    </div>
                                </div>
                                {order.payment.paymentDate && (
                                    <div className={styles.summaryRow}>
                                        <div className={styles.summaryLabel}>Date</div>
                                        <div className={styles.summaryValue}>{formatDateTime(order.payment.paymentDate)}</div>
                                    </div>
                                )}
                            </div>
                        )}

                        {/* Pay Now — shown only for PENDING orders */}
                        {order.status === 'PENDING' && (
                            <div className={styles.card}>
                                <div className={styles.sectionTitle}>Complete Your Payment</div>
                                <p style={{ fontSize: '0.875rem', color: 'var(--color-text-muted)', marginBottom: '1rem' }}>
                                    Your order is awaiting payment. Choose a payment method to proceed.
                                </p>
                                <div style={{ display: 'flex', gap: '0.75rem', marginBottom: '1rem' }}>
                                    {['VNPAY', 'MOMO'].map((m) => (
                                        <button
                                            key={m}
                                            onClick={() => setPaymentMethod(m)}
                                            style={{
                                                flex: 1,
                                                padding: '0.6rem',
                                                border: paymentMethod === m
                                                    ? '2px solid var(--color-primary, #C8871A)'
                                                    : '1px solid rgba(35,31,30,0.2)',
                                                background: paymentMethod === m
                                                    ? 'rgba(200,135,26,0.08)'
                                                    : 'transparent',
                                                fontWeight: paymentMethod === m ? 700 : 400,
                                                cursor: 'pointer',
                                                fontSize: '0.85rem',
                                                letterSpacing: '0.04em',
                                            }}
                                        >
                                            {m}
                                        </button>
                                    ))}
                                </div>
                                {paymentMethod === 'MOMO' && (
                                    <div style={{ display: 'flex', gap: '0.5rem', marginBottom: '1rem' }}>
                                        {[
                                            { code: 'captureWallet', label: 'QR / MoMo Wallet' },
                                            { code: 'payWithATM',    label: 'ATM Card' },
                                            { code: 'payWithCC',     label: 'International Card' },
                                        ].map((sub) => (
                                            <button
                                                key={sub.code}
                                                onClick={() => setMomoRequestType(sub.code)}
                                                style={{
                                                    flex: 1,
                                                    padding: '0.45rem 0.4rem',
                                                    border: momoRequestType === sub.code
                                                        ? '2px solid var(--color-primary, #C8871A)'
                                                        : '1px solid rgba(35,31,30,0.15)',
                                                    background: momoRequestType === sub.code
                                                        ? 'rgba(200,135,26,0.08)'
                                                        : 'transparent',
                                                    fontWeight: momoRequestType === sub.code ? 700 : 400,
                                                    cursor: 'pointer',
                                                    fontSize: '0.75rem',
                                                }}
                                            >
                                                {sub.label}
                                            </button>
                                        ))}
                                    </div>
                                )}
                                <button
                                    onClick={handlePayNow}
                                    style={{
                                        width: '100%',
                                        padding: '0.85rem',
                                        background: 'var(--color-primary, #C8871A)',
                                        color: '#fff',
                                        border: 'none',
                                        fontFamily: 'var(--font-body)',
                                        fontSize: '0.9rem',
                                        fontWeight: 600,
                                        letterSpacing: '0.08em',
                                        cursor: 'pointer',
                                    }}
                                >
                                    {`PAY WITH ${paymentMethod}`}
                                </button>
                            </div>
                        )}

                        {/* Cancel */}
                        <div className={`${styles.card} ${styles.cancelSection}`}>
                            <CancelOrderButton
                                orderId={order.orderId || order.id}
                                currentStatus={order.status}
                                onCancelled={handleCancelled}
                            />
                        </div>
                    </div>
                </div>
            </div>
        </>
    );
};

export default OrderDetailPage;
