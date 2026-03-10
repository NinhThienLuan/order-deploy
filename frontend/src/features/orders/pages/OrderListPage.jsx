import React, { useEffect, useState, useMemo } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { getAllOrders } from '@/features/orders/services/order.service';
import { STATUS_LABELS, ORDER_FILTERS } from '@/features/orders/services/order.constants';
import Navbar from '@/layouts/Navbar';
import styles from './OrderListPage.module.css';

const formatVND = (n) =>
    new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(n ?? 0);

const OrderListPage = () => {
    const [searchParams] = useSearchParams();
    const [orders, setOrders] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [activeFilter, setActiveFilter] = useState(() => searchParams.get('filter') || 'all');

    useEffect(() => {
        const fetchOrders = async () => {
            try {
                setLoading(true);
                const data = await getAllOrders();
                setOrders(data.content || []);
            } catch (err) {
                console.error("Failed to fetch orders:", err);
                setError("An error occurred while loading orders.");
            } finally {
                setLoading(false);
            }
        };

        fetchOrders();
    }, []);

    const filtered = useMemo(() => {
        const f = ORDER_FILTERS.find((f) => f.key === activeFilter);
        if (!f || !f.statuses) return orders;
        return orders.filter((o) => f.statuses.includes(o.status));
    }, [orders, activeFilter]);

    const getStatusClass = (status) => {
        switch (status) {
            case 'PENDING':
            case 'PAID':
                return styles.statusNeutral;
            case 'PREPARING':
            case 'READY':
                return styles.statusWarning;
            case 'COMPLETED':
                return styles.statusSuccess;
            case 'CANCELED':
                return styles.statusDanger;
            default:
                return styles.statusNeutral;
        }
    };

    if (loading) {
        return (
            <>
                <Navbar />
                <div className={styles.page}>
                    <div className={styles.container}>
                        <h1 className={styles.pageTitle}>Your Orders</h1>
                        <div className={styles.stateBox}>
                            <div className={styles.spinner}></div>
                            <p>Loading orders...</p>
                        </div>
                    </div>
                </div>
            </>
        );
    }

    if (error) {
        return (
            <>
                <Navbar />
                <div className={styles.page}>
                    <div className={styles.container}>
                        <h1 className={styles.pageTitle}>Your Orders</h1>
                        <div className={styles.stateBox}>
                            <p className={styles.errorText}>{error}</p>
                            <button onClick={() => window.location.reload()} className={styles.primaryBtn}>
                                Retry
                            </button>
                        </div>
                    </div>
                </div>
            </>
        );
    }

    return (
        <>
            <Navbar />
            <div className={styles.page}>
                <div className={styles.container}>
                    <div className={styles.header}>
                        <h1 className={styles.pageTitle}>Your Orders</h1>
                        <span className={styles.orderCount}>
                            {filtered.length} {filtered.length === 1 ? 'Order' : 'Orders'}
                        </span>
                    </div>

                    {/* Filter tabs */}
                    <div className={styles.tabs} role="tablist">
                        {ORDER_FILTERS.map((f) => (
                            <button
                                key={f.key}
                                role="tab"
                                aria-selected={activeFilter === f.key}
                                className={`${styles.tab} ${activeFilter === f.key ? styles.tabActive : ''}`}
                                onClick={() => setActiveFilter(f.key)}
                            >
                                {f.label}
                            </button>
                        ))}
                    </div>

                    {filtered.length === 0 ? (
                        <div className={styles.stateBox}>
                            <div className={styles.emptyIcon}>📦</div>
                            <p>You don't have any orders yet.</p>
                            <Link to="/" className={styles.primaryBtn}>
                                Continue Shopping
                            </Link>
                        </div>
                    ) : (
                        <div className={styles.list}>
                            {filtered.map((order) => (
                                <div key={order.id} className={styles.card}>
                                    <div className={styles.cardLeft}>
                                        <div className={styles.orderId}>{order.id}</div>
                                        <div className={styles.orderDate}>
                                            {new Date(order.createdAt).toLocaleString('en-US')}
                                        </div>
                                    </div>
                                    <div className={styles.cardRight}>
                                        <div className={styles.total}>
                                            {formatVND(order.totalAmount)}
                                        </div>
                                        <div className={`${styles.statusBadge} ${getStatusClass(order.status)}`}>
                                            {STATUS_LABELS[order.status] || order.status}
                                        </div>
                                        <Link to={`/orders/${order.id}`} className={styles.viewBtn}>
                                            View Details
                                        </Link>
                                    </div>
                                </div>
                            ))}
                        </div>
                    )}
                </div>
            </div>
        </>
    );
};

export default OrderListPage;
