import apiClient from '../../services/apiClient';

// ── Toggle to false when backend is ready ─────────────────
const USE_MOCK = true;

const MOCK_TRANSACTIONS = [
    { id: 'TX-001', from: '0x927e1e1278...d3asg567093o', to: '0x3AaBcD123456...GhljKLMnoPqr', amount: 55000,  status: 'PAID',     method: 'VNG Pay',     createdAt: '2023-05-08T08:00:00Z' },
    { id: 'TX-002', from: '0x927e1e1278...d3asg567093o', to: '0x3AaBcD123456...GhljKLMnoPqr', amount: 42000,  status: 'PAID',     method: 'Credit Card', createdAt: '2023-05-08T09:30:00Z' },
    { id: 'TX-003', from: '0x927e1e1278...d3asg567093o', to: '0x3AaBcD123456...GhljKLMnoPqr', amount: 115000, status: 'PENDING',  method: 'Cash',        createdAt: '2023-05-08T11:00:00Z' },
    { id: 'TX-004', from: '0x927e1e1278...d3asg567093o', to: '0x3AaBcD123456...GhljKLMnoPqr', amount: 78000,  status: 'PAID',     method: 'VNG Pay',     createdAt: '2023-05-08T13:15:00Z' },
    { id: 'TX-005', from: '0x927e1e1278...d3asg567093o', to: '0x3AaBcD123456...GhljKLMnoPqr', amount: 35000,  status: 'REFUNDED', method: 'Credit Card', createdAt: '2023-05-08T14:00:00Z' },
    { id: 'TX-006', from: '0x927e1e1278...d3asg567093o', to: '0x3AaBcD123456...GhljKLMnoPqr', amount: 90000,  status: 'PAID',     method: 'VNG Pay',     createdAt: new Date(Date.now() - 1000 * 60 * 60).toISOString() },
    { id: 'TX-007', from: '0x927e1e1278...d3asg567093o', to: '0x3AaBcD123456...GhljKLMnoPqr', amount: 62000,  status: 'PENDING',  method: 'Cash',        createdAt: new Date(Date.now() - 1000 * 60 * 30).toISOString() },
    { id: 'TX-008', from: '0x927e1e1278...d3asg567093o', to: '0x3AaBcD123456...GhljKLMnoPqr', amount: 150000, status: 'PAID',     method: 'Credit Card', createdAt: new Date(Date.now() - 1000 * 60 * 5).toISOString() },
    { id: 'TX-009', from: '0x927e1e1278...d3asg567093o', to: '0x3AaBcD123456...GhljKLMnoPqr', amount: 38000,  status: 'PAID',     method: 'VNG Pay',     createdAt: new Date(Date.now() - 1000 * 60 * 60 * 26).toISOString() },
    { id: 'TX-010', from: '0x927e1e1278...d3asg567093o', to: '0x3AaBcD123456...GhljKLMnoPqr', amount: 125000, status: 'REFUNDED', method: 'Credit Card', createdAt: new Date(Date.now() - 1000 * 60 * 60 * 48).toISOString() },
];

const isInRange = (iso, key) => {
    const d = new Date(iso);
    const now = new Date();
    const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
    const yesterday = new Date(today);
    yesterday.setDate(today.getDate() - 1);
    switch (key) {
        case 'today':     return d >= today;
        case 'yesterday': return d >= yesterday && d < today;
        case 'week':      return d >= new Date(today.getTime() - 7 * 86400000);
        case 'month':     return d >= new Date(today.getFullYear(), today.getMonth() - 1, today.getDate());
        case 'year':      return d >= new Date(today.getFullYear() - 1, today.getMonth(), today.getDate());
        default:          return true;
    }
};

/**
 * Fetch transaction history.
 * @param {Object} params - { status, fromDate, toDate, page, size }
 */
export const getTransactions = async (params = {}) => {
    if (USE_MOCK) {
        return new Promise((resolve) => {
            setTimeout(() => {
                const { filterKey = 'all' } = params;
                const data = MOCK_TRANSACTIONS.filter((tx) => isInRange(tx.createdAt, filterKey));
                resolve(data);
            }, 300);
        });
    }
    const res = await apiClient.get('/api/v1/payments', { params });
    return res.data.result;
};
