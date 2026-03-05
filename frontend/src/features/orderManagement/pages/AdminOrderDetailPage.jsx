import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '@/features/auth/AuthContext';
import AdminHeader from '../../../features/admin/components/AdminHeader';
import { getOrderById } from '../../orders/orders.service';
import { STEPPER_STEPS } from '../../orders/orders.constants';
import styles from '../../orders/pages/OrderDetailPage.module.css';
import pageStyles from './AdminOrderDetailPage.module.css';

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

const STATUS_LABEL_MAP = {
    ORDERED: 'Order Placed',
    PREPARING: 'Preparing',
    READY: 'Ready for Pickup',
    COMPLETED: 'Completed',
    CANCELLED: 'Cancelled',
};

export default function AdminOrderDetailPage() {
    const { id } = useParams();
    const navigate = useNavigate();
    const { user } = useAuth();
    const isAdmin = user?.role === 'FRANCHISE_ADMIN';

    const [order, setOrder] = useState(null);
    const [loading, setLoading] = useState(true);
    const [fetchError, setFetchError] = useState(null);

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

    const breadcrumbs = isAdmin
        ? ['Franchise', 'Orders', id]
        : ['Store', 'Orders', id];

    return (
        <div className={pageStyles.main}>
                <AdminHeader breadcrumbs={breadcrumbs} />

                {loading && (
                    <div className={styles.loadingState}>
                        <div className={styles.spinner} />
                        <span>Loading order...</span>
                    </div>
                )}

                {(fetchError || (!loading && !order)) && (
                    <div className={styles.errorState}>⚠️ {fetchError || 'Order not found.'}</div>
                )}

                {order && (() => {
                    const subtotal = order.pricing?.subtotal || 0;
                    const deliveryFee = order.pricing?.deliveryFee || 0;
                    const platformFee = order.pricing?.platformFee || 0;
                    const discount = order.pricing?.discount || 0;
                    const grandTotal = order.pricing?.totalAmount || 0;
                    const currentStepIdx = STEPPER_STEPS.findIndex((s) => s.key === order.status);
                    const totalItems = order.items?.reduce((s, i) => s + i.quantity, 0) || 0;

                    return (
                        <div className={pageStyles.content}>
                            <button className={pageStyles.backBtn} onClick={() => navigate(-1)}>
                                ← Back
                            </button>

                            {/* Header */}
                            <div className={styles.header}>
                                <div>
                                    <h1 className={styles.pageTitle}>Order ID : {order.orderNumber || order.id || 'N/A'}</h1>
                                    <p className={styles.pageSubtitle}>Placed at {formatDateTime(order.orderTime || order.createdAt)}</p>
                                </div>
                            </div>

                            <div className={styles.layout}>
                                <div>
                                    {/* Status bar */}
                                    <div className={`${styles.statusBar} ${order.status === 'PREPARING' ? styles.statusBarPreparing : order.status === 'COMPLETED' ? styles.statusBarDone : order.status === 'READY' ? styles.statusBarReady : ''}`}
                                        style={{ position: 'sticky', top: '5rem' }}>
                                        <div className={styles.statusIndicator}>
                                            <span className={`${styles.statusDot} ${order.status === 'COMPLETED' ? styles.done : order.status === 'READY' ? styles.ready : order.status === 'PREPARING' ? styles.preparing : ''}`} />
                                            {STATUS_LABEL_MAP[order.status] ?? order.status}
                                        </div>
                                        <span className={styles.trackingRef}>Tracking ID: {order.trackingRef || order.orderNumber || order.id}</span>
                                    </div>

                                    {/* Stepper */}
                                    <div className={`${styles.stepperCard} ${order.status === 'PREPARING' ? styles.stepperCardPreparing : ''}`}
                                        style={{ paddingTop: '2rem' }}>
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
                                                    <div className={styles.addressTitle}>{title}</div>
                                                    <div className={styles.addressName}>{data.name}</div>
                                                    <div className={styles.addressText} style={{ whiteSpace: 'pre-line' }}>{data.address}</div>
                                                </div>
                                            ))}
                                        </div>
                                    </div>

                                    {/* Order Items */}
                                    <div className={styles.card}>
                                        <div className={styles.sectionTitle}>Order Items</div>
                                        {order.items?.map((item, index) => (
                                            <div key={item.productId || index} className={styles.itemRow}>
                                                <img
                                                    src={item.imageUrl || item.image || 'https://images.unsplash.com/photo-1544233726-9f1d2b27be8b?auto=format&fit=crop&w=80&q=60'}
                                                    alt={item.productName || item.name}
                                                    className={styles.itemThumb}
                                                />
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

                                    {/* Payment */}
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
                                </div>
                            </div>
                        </div>
                    );
                })()}
        </div>
    );
}
