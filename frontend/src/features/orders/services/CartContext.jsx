import { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { CART_SESSION_KEY } from '../constants/cart.constants';

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
    const addItem = useCallback((product, quantity = 1, variant = null, note = '') => {
        setItems((prev) => {
            const variantId = variant?.id || null;
            // Compound key check: same product AND same variant (or lack thereof)
            const existing = prev.find((i) => i.id === product.id && i.variantId === variantId);
            
            if (existing) {
                return prev.map((i) =>
                    (i.id === product.id && i.variantId === variantId) 
                        ? { ...i, quantity: i.quantity + quantity } 
                        : i
                );
            }

            return [
                ...prev,
                {
                    id: product.id,
                    variantId: variantId,
                    variantName: variant?.sizeName || '',
                    productName: product.productName,
                    imageUrl: product.imageUrl,
                    price: variant ? variant.price : product.price,
                    category: product.category,
                    quantity,
                    note
                },
            ];
        });
        setIsSidebarOpen(true); // auto-open sidebar on add
    }, []);

    const removeItem = useCallback((productId, variantId = null) => {
        setItems((prev) => prev.filter((i) => !(i.id === productId && i.variantId === variantId)));
    }, []);

    const updateQty = useCallback((productId, quantity, variantId = null) => {
        if (quantity <= 0) {
            setItems((prev) => prev.filter((i) => !(i.id === productId && i.variantId === variantId)));
        } else {
            setItems((prev) =>
                prev.map((i) => (i.id === productId && i.variantId === variantId ? { ...i, quantity } : i))
            );
        }
    }, []);

    const changeVariant = useCallback((productId, oldVariantId, newVariant) => {
        setItems((prev) => {
            const itemToUpdate = prev.find(i => i.id === productId && i.variantId === oldVariantId);
            if (!itemToUpdate) return prev;

            const otherItems = prev.filter(i => !(i.id === productId && i.variantId === oldVariantId));
            const existingWithNewVariant = otherItems.find(i => i.id === productId && i.variantId === newVariant.id);

            if (existingWithNewVariant) {
                // Merge if target variant already exists
                return otherItems.map(i => 
                    (i.id === productId && i.variantId === newVariant.id)
                    ? { ...i, quantity: i.quantity + itemToUpdate.quantity }
                    : i
                );
            }

            // Just update its properties
            return [
                ...otherItems,
                {
                    ...itemToUpdate,
                    variantId: newVariant.id,
                    variantName: newVariant.sizeName,
                    price: newVariant.price
                }
            ];
        });
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
                changeVariant,
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
