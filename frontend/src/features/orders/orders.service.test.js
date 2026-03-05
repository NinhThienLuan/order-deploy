import { describe, it, expect, beforeEach, vi } from 'vitest';
import { getOrderById, createOrder, cancelOrder, getOrders } from './orders.service';

const MOCK_DB_KEY = "ecoffee_mock_orders";

describe('Orders Service (Mock JS)', () => {
    beforeEach(() => {
        localStorage.clear();
        vi.clearAllTimers();
    });

    it('should create a new order and return it', async () => {
        const payload = {
            items: [{ id: '1', quantity: 2, price: 50 }],
            buyer: { name: 'Test User', address: '123 Test St' },
            note: 'Urgent',
            subtotal: 100,
            deliveryFee: 0,
            discount: 0,
            platformFee: 0,
            grandTotal: 100
        };

        const orderPromise = createOrder(payload);
        const newOrder = await orderPromise;

        expect(newOrder).toBeDefined();
        expect(newOrder.orderId).toMatch(/^ORD-\d{6}$/);
        expect(newOrder.status).toBe('Preparing');
        expect(newOrder.items).toHaveLength(1);
        expect(newOrder.customer.customerName).toBe('Test User');
        expect(newOrder.pricing.totalAmount).toBe(100);

        // Verify it was saved to localStorage
        const db = JSON.parse(localStorage.getItem(MOCK_DB_KEY));
        expect(db[newOrder.orderId]).toBeDefined();
        expect(db[newOrder.orderId].pricing.totalAmount).toBe(100);
    });

    it('should retrieve an order by ID', async () => {
        // Setup mock DB directly
        const db = {
            'ORD-123': { orderId: 'ORD-123', status: 'Preparing' }
        };
        localStorage.setItem(MOCK_DB_KEY, JSON.stringify(db));

        const order = await getOrderById('ORD-123');
        expect(order).toBeDefined();
        expect(order.orderId).toBe('ORD-123');
        expect(order.status).toBe('Preparing');
    });

    it('should throw an error if order ID is not found', async () => {
        await expect(getOrderById('ORD-999')).rejects.toThrow('Order not found');
    });

    it('should return all orders sorted by createdAt descending', async () => {
        const db = {
            'ORD-1': { id: 'ORD-1', createdAt: '2026-02-26T10:00:00Z' },
            'ORD-2': { id: 'ORD-2', createdAt: '2026-02-26T12:00:00Z' },
            'ORD-3': { id: 'ORD-3', createdAt: '2026-02-26T11:00:00Z' }
        };
        localStorage.setItem(MOCK_DB_KEY, JSON.stringify(db));

        const orders = await getOrders();

        expect(orders).toHaveLength(3);
        // Order 2 should be first, then 3, then 1
        expect(orders[0].id).toBe('ORD-2');
        expect(orders[1].id).toBe('ORD-3');
        expect(orders[2].id).toBe('ORD-1');
    });

    it('should cancel an order correctly', async () => {
        const db = {
            'ORD-1': {
                id: 'ORD-1',
                status: 'PREPARING',
                timeline: [{ event: 'Created', status: 'done' }]
            }
        };
        localStorage.setItem(MOCK_DB_KEY, JSON.stringify(db));

        const cancelledOrder = await cancelOrder('ORD-1');

        expect(cancelledOrder.status).toBe('CANCELLED');
        expect(cancelledOrder.timeline).toHaveLength(2);
        expect(cancelledOrder.timeline[1].event).toBe('Order has been cancelled');

        // Check DB mutation
        const updatedDb = JSON.parse(localStorage.getItem(MOCK_DB_KEY));
        expect(updatedDb['ORD-1'].status).toBe('CANCELLED');
    });

    it('should throw an error when cancelling a non-existent order', async () => {
        await expect(cancelOrder('ORD-999')).rejects.toThrow('Order not found');
    });
});
