import apiClient from '@/services/apiClient';

// ── Date helpers ──────────────────────────────────────────
const toDateStr = (d) => d.toISOString().slice(0, 10); // YYYY-MM-DD

const filterKeyToDates = (key) => {
    const now = new Date();
    const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
    switch (key) {
        case 'today':
            return { fromDate: toDateStr(today) };
        case 'yesterday': {
            const y = new Date(today); y.setDate(today.getDate() - 1);
            return { fromDate: toDateStr(y), toDate: toDateStr(y) };
        }
        case 'week': {
            const w = new Date(today); w.setDate(today.getDate() - 7);
            return { fromDate: toDateStr(w) };
        }
        case 'month': {
            const m = new Date(today); m.setMonth(today.getMonth() - 1);
            return { fromDate: toDateStr(m) };
        }
        case 'year': {
            const yr = new Date(today); yr.setFullYear(today.getFullYear() - 1);
            return { fromDate: toDateStr(yr) };
        }
        default:
            return {};
    }
};

// ── Normalizer ────────────────────────────────────────────
const normalize = (tx) => ({
    id:        tx.transactionId ?? tx.id,
    orderId:   tx.orderId   ?? null,
    from:      tx.customerId ? String(tx.customerId).slice(0, 8) + '…' : '—',
    to:        tx.orderId   ? String(tx.orderId).slice(0, 8)   + '…' : '—',
    amount:    Number(tx.amountPaid ?? tx.amount ?? 0),
    status:    tx.paymentStatus ?? tx.status ?? 'UNKNOWN',
    method:    tx.paymentMethod ?? tx.method ?? '—',
    createdAt: tx.createdDate ?? tx.createdAt,
});

/**
 * Fetch transaction history from the real admin API.
 * @param {Object} params - { filterKey, status, paymentMethod, page, size }
 */
export const getTransactions = async (params = {}) => {
    const { filterKey = 'all', status, paymentMethod, page = 1, size = 50 } = params;
    const dateParams = filterKeyToDates(filterKey);

    const query = { page, size, ...dateParams };
    if (status)        query.status        = status;
    if (paymentMethod) query.paymentMethod = paymentMethod;

    const res = await apiClient.get('/api/v1/admin/transactions', { params: query });
    const result = res.data.result;
    return (result.content ?? []).map(normalize);
};
