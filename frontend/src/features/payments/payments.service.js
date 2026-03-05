import apiClient from "../../services/apiClient";

/**
 * Fetch all available payment methods (public — no auth required).
 * Returns an array of PaymentMethodResponse objects.
 */
export const getPaymentMethods = async () => {
    const res = await apiClient.get('/api/v1/payments/methods');
    const methods = res.data.result; // [{ code, name, description, icon, subOptions }]

    // Normalize subOptions: backend returns an array [{code, name, description, enabled}]
    // but the frontend consumes it as an object keyed by code: { captureWallet: { enabled, name, description } }
    return methods.map((m) => {
        if (!Array.isArray(m.subOptions) || m.subOptions.length === 0) {
            return { ...m, subOptions: {} };
        }
        const subOptions = {};
        m.subOptions.forEach((sub) => {
            subOptions[sub.code] = {
                enabled: sub.enabled !== false, // treat null/undefined as enabled
                name: sub.name,
                description: sub.description,
            };
        });
        return { ...m, subOptions };
    });
};

/**
 * Initiate a gateway payment for an order.
 * POST /v1/payments/create — works for both VNPay and MoMo.
 * @param {string} orderId
 * @param {string} paymentMethod  - 'VNPAY' | 'MOMO'
 * @param {number} amount
 * @returns {Promise<{ paymentUrl: string, paymentId: string, ... }>}
 */
export const createPayment = async (orderId, paymentMethod, amount) => {
    const transactionId = `TXN-${Date.now()}-${Math.random().toString(36).slice(2, 7).toUpperCase()}`;
    const res = await apiClient.post('/api/v1/payments/create', {
        orderId,
        paymentMethod,
        amount,
        transactionId,
    });
    return res.data.result;
};

/**
 * Get current payment status for an order.
 * @param {string} orderId
 */
export const getPaymentStatus = async (orderId) => {
    const res = await apiClient.get(`/api/v1/payments/${orderId}/status`);
    return res.data.result;
};

/**
 * Fetch a paginated list of payment transactions.
 * Admin can view and filter all transactions.
 * Customer can only view their own payment history.
 *
 * @param {Object} params - e.g. { orderId, customerId, email, status, fromDate, toDate, page, size }
 */
export const getPayments = async (params) => {
    const res = await apiClient.get('/api/v1/payments', { params });
    return res.data.result;
};
