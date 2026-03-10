import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { 
    Package, 
    Truck, 
    CheckCircle2, 
    Clock, 
    CreditCard, 
    User, 
    MapPin, 
    FileText,
    ChevronLeft
} from 'lucide-react';
import { useAuth } from '@/features/authentication/services/AuthContext';
import { getOrderById } from '@/features/orders/services/order.service';
import AdminPageHeader from '@/components/layout/AdminPageHeader';
import Button from '@/components/ui/Button';
import styles from './AdminOrderDetailPage.module.css';
import pageStyles from './OrderManagementPage.module.css';

const STATUS_LABELS = {
    'PENDING': 'Pending',
    'PAID': 'Paid',
    'PREPARING': 'Preparing',
    'READY': 'Ready',
    'COMPLETED': 'Completed',
    'CANCELED': 'Canceled'
};

const STATUS_LABEL_MAP = STATUS_LABELS;

const formatVND = (n) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(n || 0);
const formatDateTime = (s) => s ? new Date(s).toLocaleString('vi-VN') : 'N/A';

const getIconForStatus = (status) => {
    switch (status) {
        case 'PENDING': return <Clock size={18} />;
        case 'PAID': return <CreditCard size={18} />;
        case 'PREPARING': return <Package size={18} />;
        case 'READY': return <Truck size={18} />;
        case 'COMPLETED': return <CheckCircle2 size={18} />;
        default: return <FileText size={18} />;
    }
};

export default function AdminOrderDetailPage() {
    const { orderId } = useParams();
    const navigate = useNavigate();
    const [order, setOrder] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        async function fetchOrder() {
            setLoading(true);
            try {
                const data = await getOrderById(orderId);
                setOrder(data);
            } catch (err) {
                console.error("Failed to fetch order:", err);
                setError("Could not load order details.");
            } finally {
                setLoading(false);
            }
        }
        fetchOrder();
    }, [orderId]);

    if (loading) return (
        <div className={pageStyles.main}>
            <div className={pageStyles.loading}>Loading order details...</div>
        </div>
    );

    if (error || !order) return (
        <div className={pageStyles.main}>
            <div className={pageStyles.container} style={{ padding: '40px', textAlign: 'center' }}>
                <p>{error || "Order not found."}</p>
                <Button onClick={() => navigate(-1)}>Back to Orders</Button>
            </div>
        </div>
    );

    const subtotal = order.pricing?.subtotal || 0;
    const deliveryFee = order.pricing?.deliveryFee || 0;
    const platformFee = order.pricing?.platformFee || 0;
    const discount = order.pricing?.discount || 0;
    const grandTotal = order.totalAmount || order.pricing?.totalAmount || 0;
    const totalItems = order.items?.reduce((sum, item) => sum + (item.quantity || 0), 0) || 0;

    const apiStatuses = ['PENDING', 'PAID', 'PREPARING', 'READY', 'COMPLETED'];
    const currentStepIdx = apiStatuses.indexOf(order.status);

    return (
        <div className={pageStyles.main}>
            <div className={pageStyles.container}>
                <div className={styles.content}>
                    <AdminPageHeader 
                        preTitle={`ORDER #${order.orderNumber || order.id || 'N/A'}`}
                        title={`Order Details`}
                        subtitle={`Placed on ${formatDateTime(order.orderTime || order.createdAt)}`}
                        actions={
                            <Button variant="outline" onClick={() => navigate(-1)}>
                                <ChevronLeft size={16} /> Back to List
                            </Button>
                        }
                    />

                    <div className={styles.dashboardGrid}>
                        <div className={styles.mainCol}>
                            <div className={styles.card}>
                                <h2 className={styles.sectionTitle}>Order Progress</h2>
                                <div className={styles.stepperContainer}>
                                    {apiStatuses.map((statusKey, idx) => {
                                        const isDone = currentStepIdx > -1 && idx < currentStepIdx;
                                        const isActive = currentStepIdx > -1 && idx === currentStepIdx;

                                        return (
                                            <div key={statusKey} className={`${styles.step} ${isDone ? styles.stepDone : ''} ${isActive ? styles.stepActive : ''}`}>
                                                <div className={styles.stepIndicator}>
                                                    {isDone ? (
                                                        <CheckCircle2 size={18} />
                                                    ) : isActive ? (
                                                        <div className={styles.activeDot}></div>
                                                    ) : (
                                                        getIconForStatus(statusKey)
                                                    )}
                                                </div>
                                                <div className={styles.stepContent}>
                                                    <span className={styles.stepLabel}>{STATUS_LABELS[statusKey] || statusKey}</span>
                                                </div>
                                            </div>
                                        );
                                    })}
                                </div>
                            </div>

                            <div className={styles.card}>
                                <h2 className={styles.sectionTitle}>Items ({totalItems})</h2>
                                <div className={styles.itemList}>
                                    {order.items?.map((item, index) => (
                                        <div key={item.productId || index} className={styles.itemRow}>
                                            <div className={styles.itemThumbWrap}>
                                                <img 
                                                    src={item.imageUrl || item.image || 'https://placehold.co/120x120/EFECE3/231F1E?text=No+Image'} 
                                                    alt={item.productName || item.name} 
                                                    className={styles.itemThumb} 
                                                />
                                            </div>
                                            <div className={styles.itemDetails}>
                                                <span className={styles.itemCategory}>{item.category || 'Beverage'}</span>
                                                <span className={styles.itemName}>{item.productName || item.name}</span>
                                                {item.note && <span className={styles.itemNote}>{item.note}</span>}
                                            </div>
                                            <div className={styles.itemPricing}>
                                                <span className={styles.itemMath}>{item.quantity} × {formatVND(item.unitPrice || item.price)}</span>
                                                <span className={styles.itemTotal}>{formatVND(item.subtotal || ((item.unitPrice || item.price) * item.quantity))}</span>
                                            </div>
                                        </div>
                                    ))}
                                </div>
                            </div>

                            <div className={styles.card}>
                                <h2 className={styles.sectionTitle}>Financial Summary</h2>
                                <div className={styles.summaryList}>
                                    <div className={styles.summaryRow}>
                                        <span className={styles.summaryLabel}>Subtotal</span>
                                        <span className={styles.summaryVal}>{formatVND(subtotal)}</span>
                                    </div>
                                    <div className={styles.summaryRow}>
                                        <span className={styles.summaryLabel}>Delivery Fee</span>
                                        <span className={styles.summaryVal}>{deliveryFee === 0 ? 'Free' : formatVND(deliveryFee)}</span>
                                    </div>
                                    {platformFee > 0 && (
                                        <div className={styles.summaryRow}>
                                            <span className={styles.summaryLabel}>Platform Fee</span>
                                            <span className={styles.summaryVal}>{formatVND(platformFee)}</span>
                                        </div>
                                    )}
                                    {discount > 0 && (
                                        <div className={`${styles.summaryRow} ${styles.summaryDiscount}`}>
                                            <span className={styles.summaryLabel}>Discount</span>
                                            <span className={styles.summaryVal}>-{formatVND(discount)}</span>
                                        </div>
                                    )}
                                    <div className={styles.summaryGrandTotal}>
                                        <span>Total Amount</span>
                                        <span className={styles.grandTotalVal}>{formatVND(grandTotal)}</span>
                                    </div>
                                </div>
                            </div>

                            <div className={styles.card}>
                                <h2 className={styles.sectionTitle}>Delivery Details</h2>
                                <div className={styles.addressBlock}>
                                    <span className={styles.addressLabel}>Customer (Buyer)</span>
                                    <span className={styles.addressName}>{order.customer?.customerName || 'Customer'}</span>
                                    <span className={styles.addressText}>{order.customer?.deliveryAddress || 'Pick-up at store'}</span>
                                </div>
                                <div className={styles.addressDivider}></div>
                                <div className={styles.addressBlock}>
                                    <span className={styles.addressLabel}>Origin (Seller)</span>
                                    <span className={styles.addressName}>E-Coffee Flagship Store</span>
                                    <span className={styles.addressText}>Floor 1, 123 Le Loi, District 1, HCMC, Vietnam</span>
                                </div>
                            </div>
                        </div>

                        <div className={styles.sideCol}>
                            <div className={`${styles.card} ${styles.statusCard} ${styles[`status_${order.status}`] || ''}`}>
                                <div className={styles.statusHeader}>
                                    <div className={styles.statusDot}></div>
                                    <span>{STATUS_LABEL_MAP[order.status] ?? order.status}</span>
                                </div>
                                <div className={styles.statusTracking}>
                                    <span className={styles.trackingLabel}>Tracking Reference</span>
                                    <span className={styles.trackingId}>{order.trackingRef || order.orderNumber || order.id}</span>
                                </div>
                            </div>

                            {order.payment && (
                                <div className={styles.card}>
                                    <h2 className={styles.sectionTitle}>Payment Details</h2>
                                    <div className={styles.paymentDetails}>
                                        <div className={styles.payMetaRow}>
                                            <span className={styles.payMetaLabel}>Method</span>
                                            <span className={styles.payMetaVal}>{order.payment.paymentMethod}</span>
                                        </div>
                                        <div className={styles.payMetaRow}>
                                            <span className={styles.payMetaLabel}>Status</span>
                                            <span className={`${styles.payMetaVal} ${order.payment.paymentStatus === 'Paid' ? styles.paySuccess : styles.payWarning}`}>
                                                {order.payment.paymentStatus}
                                            </span>
                                        </div>
                                        {order.payment.paymentDate && (
                                            <div className={styles.payMetaRow}>
                                                <span className={styles.payMetaLabel}>Date</span>
                                                <span className={styles.payMetaVal}>{formatDateTime(order.payment.paymentDate)}</span>
                                            </div>
                                        )}
                                    </div>
                                </div>
                            )}
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}
