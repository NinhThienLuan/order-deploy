import apiClient from "../../services/apiClient";

// ── MOCK DATABASE CONFIG ──────────────────────────────────
// Toggle this to false when the real backend is ready.
const USE_MOCK = false;
const MOCK_DB_KEY = "ecoffee_mock_orders_v3";

// Pre-populated mock data for when DB is empty
const INITIAL_MOCK_ORDERS = {
    "ORD-100001": {
        id: "ORD-100001",
        createdAt: new Date(Date.now() - 1000 * 60 * 60 * 24).toISOString(),
        status: "COMPLETED",
        trackingRef: "TRK-2026-ORD-100001",
        seller: { name: "E-Coffee Flagship Store", address: "Tầng 1, 123 Lê Lợi\nQuận 1, TP.HCM\nViệt Nam" },
        buyer: { name: "Nguyen Van A", address: "45B Trần Hưng Đạo, Quận 5", phone: "0901234567" },
        items: [{ productId: "1", productName: "Iced Milk Coffee", quantity: 2, unitPrice: 2.5, subtotal: 5.0, image: 'https://images.unsplash.com/photo-1544233726-9f1d2b27be8b?auto=format&fit=crop&w=80&q=60' }],
        timeline: [
            { event: "Order placed", date: new Date(Date.now() - 1000 * 60 * 60 * 24).toISOString(), status: "done" },
            { event: "Completed", date: new Date(Date.now() - 1000 * 60 * 60 * 23).toISOString(), status: "done" }
        ],
        // Mapped response structure for Order Detail Page backwards compatibility
        pricing: { subtotal: 5.0, deliveryFee: 0.5, discount: 0, platformFee: 0, totalAmount: 5.5 },
        customer: { customerId: "cust-1", customerName: "Nguyen Van A", contactNumber: "0901234567", deliveryAddress: "45B Trần Hưng Đạo, Quận 5" },
        payment: { paymentMethod: "VNG Pay", amountPaid: 5.5, paymentStatus: "Paid", paymentDate: new Date(Date.now() - 1000 * 60 * 60 * 24).toISOString() },
        totalAmount: 5.5,
        orderNumber: "ORD-100001",
        orderTime: new Date(Date.now() - 1000 * 60 * 60 * 24).toISOString()
    },
    "ORD-100002": {
        id: "ORD-100002",
        createdAt: new Date(Date.now() - 1000 * 60 * 60 * 2).toISOString(),
        status: "PREPARING",
        trackingRef: "TRK-2026-ORD-100002",
        seller: { name: "E-Coffee Flagship Store", address: "Tầng 1, 123 Lê Lợi\nQuận 1, TP.HCM\nViệt Nam" },
        buyer: { name: "Tran Thi B", address: "12 Pasteur, Quận 1", phone: "0912345678" },
        items: [{ productId: "2", productName: "White Coffee", quantity: 1, unitPrice: 3.0, subtotal: 3.0, image: 'https://images.unsplash.com/photo-1544233726-9f1d2b27be8b?auto=format&fit=crop&w=80&q=60' }],
        timeline: [
            { event: "Order placed", date: new Date(Date.now() - 1000 * 60 * 60 * 2).toISOString(), status: "done" },
            { event: "Preparing", date: new Date(Date.now() - 1000 * 60 * 50).toISOString(), status: "active" }
        ],
        pricing: { subtotal: 3.0, deliveryFee: 1.0, discount: 0.0, platformFee: 0.2, totalAmount: 4.2 },
        customer: { customerId: "cust-2", customerName: "Tran Thi B", contactNumber: "0912345678", deliveryAddress: "12 Pasteur, Quận 1" },
        payment: { paymentMethod: "Credit Card", amountPaid: 4.2, paymentStatus: "Paid", paymentDate: new Date(Date.now() - 1000 * 60 * 60 * 2).toISOString() },
        totalAmount: 4.2,
        orderNumber: "ORD-100002",
        orderTime: new Date(Date.now() - 1000 * 60 * 60 * 2).toISOString()
    },
    "ORD-100003": {
        id: "ORD-100003",
        createdAt: new Date().toISOString(),
        status: "ORDERED",
        trackingRef: "TRK-2026-ORD-100003",
        seller: { name: "E-Coffee Flagship Store", address: "Tầng 1, 123 Lê Lợi\nQuận 1, TP.HCM\nViệt Nam" },
        buyer: { name: "Le Van C", address: "89 Lê Duẩn, Quận 1", phone: "0987654321" },
        items: [{ productId: "3", productName: "Americano", quantity: 3, unitPrice: 4.0, subtotal: 12.0, image: 'https://images.unsplash.com/photo-1544233726-9f1d2b27be8b?auto=format&fit=crop&w=80&q=60' }],
        timeline: [
            { event: "Order placed", date: new Date().toISOString(), status: "active" }
        ],
        pricing: { subtotal: 12.0, deliveryFee: 0.0, discount: 1.0, platformFee: 0.5, totalAmount: 11.5 },
        customer: { customerId: "cust-3", customerName: "Le Van C", contactNumber: "0987654321", deliveryAddress: "89 Lê Duẩn, Quận 1" },
        payment: { paymentMethod: "Cash", amountPaid: 11.5, paymentStatus: "Pending", paymentDate: null },
        totalAmount: 11.5,
        orderNumber: "ORD-100003",
        orderTime: new Date().toISOString()
    }
};

// Helper to get mock DB
const getMockDb = () => {
    try {
        const data = localStorage.getItem(MOCK_DB_KEY);
        if (!data) {
            saveMockDb(INITIAL_MOCK_ORDERS);
            return INITIAL_MOCK_ORDERS;
        }
        return JSON.parse(data);
    } catch {
        return INITIAL_MOCK_ORDERS;
    }
};

// Helper to save mock DB
const saveMockDb = (data) => {
    localStorage.setItem("ecoffee_mock_orders_v3", JSON.stringify(data));
};

// ── SERVICES ──────────────────────────────────────────────

/**
 * Fetch all orders for the current customer (GET /v1/orders/me)
 */
export const getAllOrders = async (params) => {
    if (USE_MOCK) {
        return new Promise((resolve) => {
            setTimeout(() => {
                const db = getMockDb();
                const ordersArray = Object.values(db).sort(
                    (a, b) => new Date(b.createdAt) - new Date(a.createdAt)
                );
                // Return wrapped in Live API format
                resolve({ content: ordersArray, page: 0, size: ordersArray.length, totalElements: ordersArray.length });
            }, 600); // simulate network latency
        });
    }

    const res = await apiClient.get(`/api/v1/orders/me`, { params });
    return res.data.result;
};

/**
 * Fetch all orders for admin/store manager roles (GET /v1/orders)
 */
export const getAdminOrders = async (params) => {
    if (USE_MOCK) {
        return new Promise((resolve) => {
            setTimeout(() => {
                const db = getMockDb();
                const ordersArray = Object.values(db).sort(
                    (a, b) => new Date(b.createdAt) - new Date(a.createdAt)
                );
                resolve({ content: ordersArray, page: 0, size: ordersArray.length, totalElements: ordersArray.length });
            }, 600);
        });
    }

    const res = await apiClient.get(`/api/v1/orders`, { params });
    return res.data.result;
};

/**
 * Fetch a single order by ID
 * @param {string} orderId 
 */
export const getOrderById = async (orderId) => {
    if (USE_MOCK) {
        return new Promise((resolve, reject) => {
            setTimeout(() => {
                const db = getMockDb();
                if (db[orderId]) {
                    resolve(db[orderId]);
                } else {
                    reject(new Error("Order not found"));
                }
            }, 800); // simulate network latency
        });
    }

    const res = await apiClient.get(`/api/v1/orders/${orderId}`);
    return res.data.result;
};

/**
 * Fetch current order status (Polling/Quick check)
 * @param {string} orderId 
 */
export const getOrderStatus = async (orderId) => {
    if (USE_MOCK) {
        return new Promise((resolve, reject) => {
            setTimeout(() => {
                const db = getMockDb();
                if (db[orderId]) {
                    resolve({ id: db[orderId].id, status: db[orderId].status, lastUpdated: new Date().toISOString() });
                } else {
                    reject(new Error("Order not found"));
                }
            }, 500);
        });
    }

    const res = await apiClient.get(`/api/v1/orders/${orderId}/status`);
    return res.data.result;
};

/**
 * Create a new order from cart items
 * @param {Object} payload 
 */
export const createOrder = async (payload) => {
    if (USE_MOCK) {
        return new Promise((resolve) => {
            setTimeout(() => {
                const orderId = `ORD-${Date.now().toString().slice(-6)}`;

                const newOrder = {
                    id: orderId,
                    createdAt: new Date().toISOString(),
                    status: 'PREPARING',
                    trackingRef: `TRK-2026-${orderId}`,
                    seller: {
                        name: 'E-Coffee Flagship Store',
                        address: 'Tầng 1, 123 Lê Lợi\nQuận 1, TP.HCM\nViệt Nam',
                    },
                    buyer: {
                        name: payload.buyer?.name || 'Guest',
                        address: payload.buyer?.address || 'E.g: 45B Tran Hung Dao\nDistrict 5, HCMC\nVietnam',
                    },
                    items: payload.items?.map(it => ({
                        productId: it.id,
                        productName: it.name,
                        quantity: it.quantity,
                        unitPrice: it.price,
                        subtotal: (it.price * it.quantity),
                        image: it.image
                    })) || [],
                    pricing: { subtotal: payload.subtotal, deliveryFee: payload.deliveryFee, discount: payload.discount, platformFee: payload.platformFee, totalAmount: payload.grandTotal },
                    customer: { customerId: "guest-id", customerName: payload.buyer?.name || 'Guest', contactNumber: payload.buyer?.phone || "0900000000", deliveryAddress: payload.buyer?.address || 'Store Pickup' },
                    payment: { paymentMethod: "VNG Pay", amountPaid: payload.grandTotal, paymentStatus: "Paid", paymentDate: new Date().toISOString() },
                    totalAmount: payload.grandTotal,
                    orderNumber: orderId,
                    orderTime: new Date().toISOString(),
                    timeline: [
                        { event: 'Order placed', date: new Date().toISOString(), status: 'done' },
                        { event: 'Preparing drinks', date: new Date().toISOString(), status: 'active' },
                        { event: 'Ready for pickup', date: '—', status: 'pending' },
                        { event: 'Completed', date: '—', status: 'pending' },
                    ],
                };

                const db = getMockDb();
                db[orderId] = newOrder;
                saveMockDb(db);

                resolve(newOrder);
            }, 1000); // simulate network latency
        });
    }

    const res = await apiClient.post(`/api/v1/orders`, payload);
    return res.data.result;
};

/**
 * Cancel an order by ID
 * @param {string} orderId 
 */
export const cancelOrder = async (orderId) => {
    if (USE_MOCK) {
        return new Promise((resolve, reject) => {
            setTimeout(() => {
                const db = getMockDb();
                if (db[orderId]) {
                    db[orderId].status = 'CANCELED';
                    db[orderId].timeline.push({
                        event: 'Order has been cancelled',
                        date: new Date().toLocaleString('en-US'),
                        status: 'done'
                    });
                    saveMockDb(db);
                    resolve(db[orderId]);
                } else {
                    reject(new Error("Order not found"));
                }
            }, 600);
        });
    }

    const res = await apiClient.patch(`/api/v1/orders/${orderId}/cancel`);
    return res.data.result;
};

/**
 * Initiate a gateway payment for an order.
 * Calls POST /v1/payments/create — the single payment entry point for VNPay & MoMo.
 * @param {string} orderId
 * @param {{ paymentMethod: string, amount: number }} payload
 * @returns {Promise<{ paymentUrl: string, paymentId: string, ... }>}
 */
export const processPayment = async (orderId, payload) => {
    const transactionId = `TXN-${Date.now()}-${Math.random().toString(36).slice(2, 7).toUpperCase()}`;
    const res = await apiClient.post('/api/v1/payments/create', {
        orderId,
        paymentMethod: payload.paymentMethod,
        amount: payload.amount,
        transactionId,
        ...(payload.momoRequestType && { momoRequestType: payload.momoRequestType }),
    });
    return res.data.result;
};

/**
 * Confirm or fail a payment (called from PaymentResultPage after gateway returns)
 * @param {string} orderId
 * @param {string} paymentId
 * @param {boolean} success
 */
export const confirmPayment = async (orderId, paymentId, success) => {
    const res = await apiClient.post(
        `/api/v1/orders/${orderId}/payments/confirm?paymentId=${paymentId}&success=${success}`
    );
    return res.data.result;
};
