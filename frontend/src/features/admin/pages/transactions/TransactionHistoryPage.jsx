import { useState, useEffect, useMemo } from 'react';
import { useSearchParams } from 'react-router-dom';
import Navbar from '@/layouts/Navbar';
import { getTransactions } from '@/features/admin/services/transactions/transactions.service.js';
import { TRANSACTION_FILTERS, TRANSACTION_PAGE_SIZE, TRANSACTION_STATUS_META } from '@/features/admin/constants/transactions/transactions.constants';
import styles from './TransactionHistoryPage.module.css';

const formatDate = (iso) =>
    new Date(iso).toLocaleString('en-US');

const formatVND = (n) =>
    new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(n ?? 0);

const TransactionHistoryPage = () => {
    const [searchParams] = useSearchParams();
    const [activeFilter, setActiveFilter] = useState(() => searchParams.get('period') || 'all');
    const [showAll, setShowAll]           = useState(false);
    const [data, setData]                 = useState([]);
    const [loading, setLoading]           = useState(true);
    const [error, setError]               = useState(null);

    useEffect(() => {
        setLoading(true);
        setError(null);
        getTransactions({ filterKey: activeFilter })
            .then(setData)
            .catch(() => setError('Failed to load transactions.'))
            .finally(() => setLoading(false));
    }, [activeFilter]);

    const handleFilterChange = (key) => {
        setActiveFilter(key);
        setShowAll(false);
    };

    const displayed = useMemo(
        () => (showAll ? data : data.slice(0, TRANSACTION_PAGE_SIZE)),
        [data, showAll]
    );

    const hasMore = data.length > TRANSACTION_PAGE_SIZE;

    if (loading) {
        return (
            <>
                <Navbar />
                <div className={styles.page}>
                    <div className={styles.container}>
                        <h1 className={styles.pageTitle}>Transaction History</h1>
                        <div className={styles.stateBox}>
                            <div className={styles.spinner}></div>
                            <p>Loading transactions...</p>
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
                        <h1 className={styles.pageTitle}>Transaction History</h1>
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

                    {/* Page header */}
                    <div className={styles.header}>
                        <h1 className={styles.pageTitle}>Transaction History</h1>
                        <span className={styles.orderCount}>
                            {data.length} {data.length === 1 ? 'Transaction' : 'Transactions'}
                        </span>
                    </div>

                    {/* Filter tabs */}
                    <div className={styles.tabs} role="tablist">
                        {TRANSACTION_FILTERS.map((f) => (
                            <button
                                key={f.key}
                                role="tab"
                                aria-selected={activeFilter === f.key}
                                className={`${styles.tab} ${activeFilter === f.key ? styles.tabActive : ''}`}
                                onClick={() => handleFilterChange(f.key)}
                            >
                                {f.label}
                            </button>
                        ))}
                    </div>

                    {/* Transaction list */}
                    {displayed.length === 0 ? (
                        <div className={styles.stateBox}>
                            <div className={styles.emptyIcon}>💳</div>
                            <p>No transactions found for this period.</p>
                        </div>
                    ) : (
                        <div className={styles.list}>
                            {displayed.map((tx) => {
                                const meta = TRANSACTION_STATUS_META[tx.status] ?? TRANSACTION_STATUS_META.PENDING;
                                return (
                                    <div key={tx.id} className={styles.card}>
                                        <div className={styles.cardLeft}>
                                            <div className={styles.txId}>{tx.id}</div>
                                            <div className={styles.txDate}>{formatDate(tx.createdAt)}</div>
                                            <div className={styles.txParties}>
                                                <span>{tx.from}</span>
                                                <span className={styles.arrow}>→</span>
                                                <span>{tx.to}</span>
                                            </div>
                                        </div>
                                        <div className={styles.cardRight}>
                                            <div className={styles.total}>{formatVND(tx.amount)}</div>
                                            <div className={`${styles.statusBadge} ${styles[meta.cls]}`}>
                                                {meta.label}
                                            </div>
                                        </div>
                                    </div>
                                );
                            })}
                        </div>
                    )}

                    {/* Show more / less */}
                    {hasMore && (
                        <div className={styles.footerRow}>
                            <button className={styles.primaryBtn} onClick={() => setShowAll((p) => !p)}>
                                {showAll ? 'Show Less' : 'View All Transactions'}
                            </button>
                        </div>
                    )}

                </div>
            </div>
        </>
    );
};

export default TransactionHistoryPage;
