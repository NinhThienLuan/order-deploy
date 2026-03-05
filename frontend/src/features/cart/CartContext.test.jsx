import { describe, it, expect, beforeEach } from 'vitest';
import { renderHook, act } from '@testing-library/react';
import { CartProvider, useCart } from './CartContext';
import { CART_SESSION_KEY } from './cart.constants';

describe('CartContext', () => {
    beforeEach(() => {
        sessionStorage.clear();
    });

    const wrapper = ({ children }) => <CartProvider>{children}</CartProvider>;

    it('should initialize with an empty cart and closed sidebar', () => {
        const { result } = renderHook(() => useCart(), { wrapper });

        expect(result.current.items).toEqual([]);
        expect(result.current.totalItems).toBe(0);
        expect(result.current.subtotal).toBe(0);
        expect(result.current.isSidebarOpen).toBe(false);
    });

    it('should add a product correctly and open the sidebar', () => {
        const { result } = renderHook(() => useCart(), { wrapper });

        const mockProduct = {
            id: 'p1',
            productName: 'Latte',
            imageUrl: 'latte.jpg',
            price: 50,
            category: 'Coffee',
        };

        act(() => {
            result.current.addItem(mockProduct, 2, 'No sugar');
        });

        expect(result.current.items).toHaveLength(1);
        expect(result.current.items[0]).toEqual({
            id: 'p1',
            productName: 'Latte',
            imageUrl: 'latte.jpg',
            price: 50,
            category: 'Coffee',
            quantity: 2,
            note: 'No sugar',
        });
        expect(result.current.totalItems).toBe(2);
        expect(result.current.subtotal).toBe(100);
        expect(result.current.isSidebarOpen).toBe(true);

        // Verify sessionStorage sync
        const stored = JSON.parse(sessionStorage.getItem(CART_SESSION_KEY));
        expect(stored).toHaveLength(1);
        expect(stored[0].id).toBe('p1');
    });

    it('should increment quantity when adding an existing product', () => {
        const { result } = renderHook(() => useCart(), { wrapper });

        const mockProduct = { id: 'p1', price: 50 };

        act(() => {
            result.current.addItem(mockProduct, 1);
        });

        act(() => {
            result.current.addItem(mockProduct, 2);
        });

        expect(result.current.items).toHaveLength(1);
        expect(result.current.items[0].quantity).toBe(3);
        expect(result.current.subtotal).toBe(150);
    });

    it('should update item quantity', () => {
        const { result } = renderHook(() => useCart(), { wrapper });
        const mockProduct = { id: 'p1', price: 50 };

        act(() => {
            result.current.addItem(mockProduct, 2);
        });

        act(() => {
            result.current.updateQty('p1', 5);
        });

        expect(result.current.items[0].quantity).toBe(5);
        expect(result.current.subtotal).toBe(250);
    });

    it('should remove item when quantity is updated to 0', () => {
        const { result } = renderHook(() => useCart(), { wrapper });
        const mockProduct = { id: 'p1', price: 50 };

        act(() => {
            result.current.addItem(mockProduct, 2);
        });

        act(() => {
            result.current.updateQty('p1', 0);
        });

        expect(result.current.items).toHaveLength(0);
        expect(result.current.totalItems).toBe(0);
    });

    it('should remove an item precisely', () => {
        const { result } = renderHook(() => useCart(), { wrapper });
        const product1 = { id: 'p1', price: 50 };
        const product2 = { id: 'p2', price: 30 };

        act(() => {
            result.current.addItem(product1, 1);
            result.current.addItem(product2, 1);
        });

        expect(result.current.items).toHaveLength(2);

        act(() => {
            result.current.removeItem('p1');
        });

        expect(result.current.items).toHaveLength(1);
        expect(result.current.items[0].id).toBe('p2');
    });

    it('should clear all items in the cart', () => {
        const { result } = renderHook(() => useCart(), { wrapper });
        const product1 = { id: 'p1', price: 50 };

        act(() => {
            result.current.addItem(product1, 1);
        });

        act(() => {
            result.current.clearCart();
        });

        expect(result.current.items).toHaveLength(0);
        expect(result.current.totalItems).toBe(0);

        // Verify sessionStorage sync
        const stored = sessionStorage.getItem(CART_SESSION_KEY);
        // By implementation, useEffect syncs empty items array to "[]"
        expect(stored).toBe('[]');
    });

    it('should open and close the sidebar manually', () => {
        const { result } = renderHook(() => useCart(), { wrapper });

        act(() => {
            result.current.openSidebar();
        });
        expect(result.current.isSidebarOpen).toBe(true);

        act(() => {
            result.current.closeSidebar();
        });
        expect(result.current.isSidebarOpen).toBe(false);
    });
});
