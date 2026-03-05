import { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { CART_SESSION_KEY } from './cart.constants';

// ── Shape stored in sessionStorage:
// [{ id, productName, imageUrl, price, category, quantity, note }]

const CartContext = createContext(null);

export const CartProvider = ({ children }) => {
    // Hydrate from sessionStorage on mount
    const [items, setItems] = useState(() => {
        try {
            const stored = sessionStorage.getItem(CART_SESSION_KEY);
            return stored ? JSON.parse(stored) : [];
        } catch {
            return [];
        }
    });

    const [isSidebarOpen, setIsSidebarOpen] = useState(false);

    // Keep sessionStorage and body overflow in sync
    useEffect(() => {
        sessionStorage.setItem(CART_SESSION_KEY, JSON.stringify(items));
    }, [items]);

    useEffect(() => {
        if (isSidebarOpen) {
            const scrollBarWidth = window.innerWidth - document.documentElement.clientWidth;
            document.body.style.overflow = 'hidden';
            if (scrollBarWidth > 0) {
                document.body.style.paddingRight = `${scrollBarWidth}px`;
            }
        } else {
            document.body.style.overflow = '';
            document.body.style.paddingRight = '';
        }
    }, [isSidebarOpen]);

    // ── Actions ────────────────────────────────────────
    const addItem = useCallback((product, quantity = 1, note = '') => {
        setItems((prev) => {
            const existing = prev.find((i) => i.id === product.id);
            if (existing) {
                return prev.map((i) =>
                    i.id === product.id ? { ...i, quantity: i.quantity + quantity } : i
                );
            }
            return [
                ...prev,
                {
                    id: product.id, productName: product.productName, imageUrl: product.imageUrl,
                    price: product.price, category: product.category, quantity, note
                },
            ];
        });
        setIsSidebarOpen(true); // auto-open sidebar on add
    }, []);

    const removeItem = useCallback((productId) => {
        setItems((prev) => prev.filter((i) => i.id !== productId));
    }, []);

    const updateQty = useCallback((productId, quantity) => {
        if (quantity <= 0) {
            setItems((prev) => prev.filter((i) => i.id !== productId));
        } else {
            setItems((prev) =>
                prev.map((i) => (i.id === productId ? { ...i, quantity } : i))
            );
        }
    }, []);

    const clearCart = useCallback(() => {
        setItems([]);
        sessionStorage.removeItem(CART_SESSION_KEY);
    }, []);

    const openSidebar = useCallback(() => setIsSidebarOpen(true), []);
    const closeSidebar = useCallback(() => setIsSidebarOpen(false), []);

    // ── Derived ────────────────────────────────────────
    const totalItems = items.reduce((s, i) => s + i.quantity, 0);
    const subtotal = items.reduce((s, i) => s + i.price * i.quantity, 0);

    return (
        <CartContext.Provider
            value={{
                items,
                totalItems,
                subtotal,
                isSidebarOpen,
                addItem,
                removeItem,
                updateQty,
                clearCart,
                openSidebar,
                closeSidebar,
            }}
        >
            {children}
        </CartContext.Provider>
    );
};

// eslint-disable-next-line react-refresh/only-export-components
export const useCart = () => {
    const ctx = useContext(CartContext);
    if (!ctx) throw new Error('useCart must be used inside <CartProvider>');
    return ctx;
};
