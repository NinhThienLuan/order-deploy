import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import {
    Search, Plus, Minus, Trash2, ShoppingCart, X, CheckCircle, Monitor,
} from 'lucide-react';
import { getProducts } from '@/features/products/services/products.service';
import { getCategories } from '@/features/products/services/categories.service';
import { createOrder } from '@/features/orders/services/order.service';
import { getPaymentMethods } from '@/features/payments/services/payments.service';
import Button from '@/components/ui/Button';
import styles from './POSPage.module.css';

const formatVND = (n) =>
    new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(n ?? 0);

// ── Variant Picker Modal ─────────────────────────────────────────
function VariantPicker({ product, onSelect, onClose }) {
    return (
        <div className={styles.overlay} onClick={onClose}>
            <div className={styles.variantModal} onClick={(e) => e.stopPropagation()}>
                <div className={styles.variantHeader}>
                    <span className={styles.variantTitle}>Chọn kích cỡ</span>
                    <button className={styles.closeIcon} onClick={onClose}><X size={16} /></button>
                </div>
                <p className={styles.variantProduct}>{product.productName}</p>
                <div className={styles.variantOptions}>
                    {product.variants.filter((v) => v.active !== false).map((v) => (
                        <button
                            key={v.id}
                            className={styles.variantBtn}
                            onClick={() => onSelect(product, v)}
                        >
                            <span className={styles.variantSize}>{v.sizeName}</span>
                            <span className={styles.variantPrice}>{formatVND(v.price)}</span>
                        </button>
                    ))}
                </div>
            </div>
        </div>
    );
}

// ── Success Receipt Modal ────────────────────────────────────────
function SuccessModal({ order, onViewOrder, onNewOrder }) {
    return (
        <div className={styles.overlay}>
            <div className={styles.successModal}>
                <div className={styles.successIconWrap}>
                    <CheckCircle size={52} strokeWidth={1.5} />
                </div>
                <h3 className={styles.successTitle}>Đặt hàng thành công!</h3>
                <p className={styles.successId}>#{order.orderNumber || order.orderId || order.id}</p>
                <div className={styles.successItems}>
                    {(order.items ?? []).map((item, i) => (
                        <div key={i} className={styles.successItem}>
                            <span>{item.productName}</span>
                            <span>×{item.quantity}</span>
                            <span>{formatVND(item.subtotal ?? item.unitPrice * item.quantity)}</span>
                        </div>
                    ))}
                </div>
                <div className={styles.successTotal}>
                    <span>Tổng cộng</span>
                    <span>{formatVND(order.totalAmount)}</span>
                </div>
                <div className={styles.successActions}>
                    <Button variant="ghost" onClick={onViewOrder}>Xem đơn hàng</Button>
                    <Button variant="primary" onClick={onNewOrder}>Đơn mới</Button>
                </div>
            </div>
        </div>
    );
}

// ── Main POS Component ──────────────────────────────────────────
export default function POSPage() {
    const navigate = useNavigate();

    // Catalog State
    const [products, setProducts] = useState([]);
    const [categories, setCategories] = useState([]);
    const [selectedCategory, setSelectedCategory] = useState('');
    const [search, setSearch] = useState('');
    const [loadingProducts, setLoadingProducts] = useState(true);
    const [catalogPage, setCatalogPage] = useState(0);
    const [totalPages, setTotalPages] = useState(1);

    // UI State
    const [pickerProduct, setPickerProduct] = useState(null);
    const [successOrder, setSuccessOrder] = useState(null);

    // Cart State
    const [cartItems, setCartItems] = useState([]);
    const [customerName, setCustomerName] = useState('');
    const [note, setNote] = useState('');

    // Payment
    const [paymentMethods, setPaymentMethods] = useState([
        { code: 'CASH',  name: 'Tiền mặt', icon: 'cash',  subOptions: {} },
        { code: 'VNPAY', name: 'VNPay',    icon: 'vnpay', subOptions: {} },
        { code: 'MOMO',  name: 'MoMo',     icon: 'momo',  subOptions: {
            captureWallet: { enabled: true, name: 'QR / Ví MoMo' },
            payWithATM:    { enabled: true, name: 'ATM / Chuyển khoản' },
        }},
    ]);
    const [paymentMethod, setPaymentMethod] = useState('CASH');
    const [momoType, setMomoType] = useState('captureWallet');

    // Submission
    const [submitting, setSubmitting] = useState(false);
    const [error, setError] = useState(null);

    // ── Fetch Products ──
    const fetchProducts = useCallback(async ({ pg = 0, cat = selectedCategory, q = search } = {}) => {
        setLoadingProducts(true);
        try {
            const data = await getProducts({
                page: pg,
                size: 16,
                categoryId: cat || undefined,
                search: q || undefined,
            });
            setProducts(data.content ?? []);
            setTotalPages(data.totalPages ?? 1);
            setCatalogPage(pg);
        } catch {
            // silent fail
        } finally {
            setLoadingProducts(false);
        }
    }, []);

    useEffect(() => {
        getCategories().then(setCategories).catch(() => {});
    }, []);

    useEffect(() => {
        getPaymentMethods()
            .then((methods) => {
                if (Array.isArray(methods) && methods.length > 0) {
                    setPaymentMethods(methods);
                    // Giữ CASH làm default nếu có, không thì lấy method đầu tiên
                    const hasCash = methods.some((m) => m.code === 'CASH');
                    setPaymentMethod(hasCash ? 'CASH' : methods[0].code);
                }
            })
            .catch(() => { /* giữ fallback */ });
    }, []);

    useEffect(() => {
        const timeout = setTimeout(() => {
            fetchProducts({ pg: 0, cat: selectedCategory, q: search });
        }, 300);
        return () => clearTimeout(timeout);
    }, [selectedCategory, search]);

    // ── Cart Actions ──
    const addToCart = (product, variant = null) => {
        setPickerProduct(null);
        const key = `${product.id}__${variant?.id ?? 'default'}`;
        setCartItems((prev) => {
            const existing = prev.find((i) => i.key === key);
            if (existing) {
                return prev.map((i) => i.key === key ? { ...i, quantity: i.quantity + 1 } : i);
            }
            return [
                ...prev,
                {
                    key,
                    productId: product.id,
                    variantId: variant?.id ?? null,
                    productName: product.productName,
                    imageUrl: product.imageUrl,
                    sizeName: variant?.sizeName ?? null,
                    price: variant ? variant.price : product.price,
                    quantity: 1,
                },
            ];
        });
    };

    const handleProductClick = (product) => {
        const activeVariants = (product.variants ?? []).filter((v) => v.active !== false);
        if (activeVariants.length > 1) {
            setPickerProduct({ ...product, variants: activeVariants });
        } else if (activeVariants.length === 1) {
            addToCart(product, activeVariants[0]);
        } else {
            addToCart(product);
        }
    };

    const updateQty = (key, delta) => {
        setCartItems((prev) =>
            prev.reduce((acc, item) => {
                if (item.key !== key) return [...acc, item];
                const q = item.quantity + delta;
                if (q <= 0) return acc;
                return [...acc, { ...item, quantity: q }];
            }, [])
        );
    };

    const removeItem = (key) => setCartItems((prev) => prev.filter((i) => i.key !== key));

    const clearCart = () => {
        setCartItems([]);
        setCustomerName('');
        setNote('');
        setError(null);
    };

    const subtotal = cartItems.reduce((s, i) => s + i.price * i.quantity, 0);
    const totalQty = cartItems.reduce((s, i) => s + i.quantity, 0);

    // ── Submit Order ──
    const handleSubmit = async () => {
        if (!cartItems.length || submitting) return;
        setSubmitting(true);
        setError(null);
        try {
            const noteParts = [];
            if (customerName.trim()) noteParts.push(`Khách: ${customerName.trim()}`);
            if (note.trim()) noteParts.push(note.trim());

            const payload = {
                items: cartItems.map((i) => ({
                    productId: i.productId,
                    ...(i.variantId && { variantId: i.variantId }),
                    quantity: i.quantity,
                })),
                note: noteParts.join(' | ') || undefined,
                deliveryAddress: '',
            };

            const order = await createOrder(payload);
            const orderId = order.orderId || order.id;

            if (paymentMethod === 'CASH') {
                // Tiền mặt: hiện receipt rồi xong
                setSuccessOrder(order);
                return;
            }

            // Online payment: chuyển sang trang OrderDetail để xử lý thanh toán
            clearCart();
            navigate(`/orders/${orderId}`, {
                state: {
                    preselectedPaymentMethod: paymentMethod,
                    preselectedMomoType: paymentMethod === 'MOMO' ? momoType : undefined,
                }
            });
        } catch (err) {
            setError(err?.response?.data?.message || err.message || 'Tạo đơn thất bại');
        } finally {
            setSubmitting(false);
        }
    };

    // ── Render ──
    return (
        <div className={styles.pos}>
            {/* ── LEFT: Product Catalog ── */}
            <div className={styles.catalog}>
                {/* Header */}
                <div className={styles.catalogHead}>
                    <div className={styles.catalogTitle}>
                        <Monitor size={18} />
                        <span>POS — Tạo đơn tại quầy</span>
                    </div>
                    <div className={styles.searchBox}>
                        <Search size={15} className={styles.searchIcon} />
                        <input
                            type="text"
                            className={styles.searchInput}
                            placeholder="Tìm sản phẩm..."
                            value={search}
                            onChange={(e) => setSearch(e.target.value)}
                        />
                        {search && (
                            <button className={styles.searchClear} onClick={() => setSearch('')}>
                                <X size={14} />
                            </button>
                        )}
                    </div>
                </div>

                {/* Category Chips */}
                <div className={styles.categories}>
                    <button
                        className={`${styles.catChip} ${selectedCategory === '' ? styles.catActive : ''}`}
                        onClick={() => setSelectedCategory('')}
                    >
                        Tất cả
                    </button>
                    {categories.map((c) => (
                        <button
                            key={c.id}
                            className={`${styles.catChip} ${selectedCategory === c.id ? styles.catActive : ''}`}
                            onClick={() => setSelectedCategory(c.id)}
                        >
                            {c.name}
                        </button>
                    ))}
                </div>

                {/* Product Grid */}
                <div className={styles.productGrid}>
                    {loadingProducts ? (
                        [...Array(8)].map((_, i) => <div key={i} className={styles.skeleton} />)
                    ) : products.length === 0 ? (
                        <div className={styles.emptyProducts}>
                            <Search size={32} />
                            <p>Không tìm thấy sản phẩm</p>
                        </div>
                    ) : (
                        products.map((p) => {
                            const isOut = p.status === 'OutOfStock';
                            return (
                                <button
                                    key={p.id}
                                    className={`${styles.productCard} ${isOut ? styles.outOfStock : ''}`}
                                    onClick={() => !isOut && handleProductClick(p)}
                                    disabled={isOut}
                                >
                                    <div className={styles.productImg}>
                                        {p.imageUrl ? (
                                            <img src={p.imageUrl} alt={p.productName} loading="lazy" />
                                        ) : (
                                            <span className={styles.noImg}>☕</span>
                                        )}
                                        {isOut && <span className={styles.outBadge}>Hết</span>}
                                    </div>
                                    <div className={styles.productBody}>
                                        <span className={styles.productName}>{p.productName}</span>
                                        <span className={styles.productPrice}>{formatVND(p.price)}</span>
                                    </div>
                                    <div className={styles.productAddIcon}>
                                        <Plus size={14} />
                                    </div>
                                </button>
                            );
                        })
                    )}
                </div>

                {/* Pagination */}
                {totalPages > 1 && (
                    <div className={styles.catalogPagination}>
                        <button
                            className={styles.pageBtn}
                            disabled={catalogPage <= 0}
                            onClick={() => fetchProducts({ pg: catalogPage - 1 })}
                        >
                            ‹
                        </button>
                        <span>{catalogPage + 1} / {totalPages}</span>
                        <button
                            className={styles.pageBtn}
                            disabled={catalogPage + 1 >= totalPages}
                            onClick={() => fetchProducts({ pg: catalogPage + 1 })}
                        >
                            ›
                        </button>
                    </div>
                )}
            </div>

            {/* ── RIGHT: Order Panel ── */}
            <div className={styles.orderPanel}>
                {/* Panel Header */}
                <div className={styles.panelHeader}>
                    <div className={styles.panelTitle}>
                        <ShoppingCart size={17} />
                        <span>Đơn hiện tại</span>
                        {totalQty > 0 && (
                            <span className={styles.qtyBadge}>{totalQty}</span>
                        )}
                    </div>
                    {cartItems.length > 0 && (
                        <button className={styles.clearBtn} onClick={clearCart}>
                            <Trash2 size={13} />
                            Xóa tất cả
                        </button>
                    )}
                </div>

                {/* Cart Items */}
                <div className={styles.cartItems}>
                    {cartItems.length === 0 ? (
                        <div className={styles.emptyCart}>
                            <ShoppingCart size={36} strokeWidth={1.2} />
                            <p>Chưa có sản phẩm</p>
                            <p className={styles.emptyHint}>Chọn sản phẩm từ danh mục bên trái</p>
                        </div>
                    ) : (
                        cartItems.map((item) => (
                            <div key={item.key} className={styles.cartItem}>
                                <div className={styles.cartThumb}>
                                    {item.imageUrl
                                        ? <img src={item.imageUrl} alt={item.productName} />
                                        : <span>☕</span>
                                    }
                                </div>
                                <div className={styles.cartInfo}>
                                    <span className={styles.cartName}>{item.productName}</span>
                                    {item.sizeName && (
                                        <span className={styles.cartSize}>{item.sizeName}</span>
                                    )}
                                    <span className={styles.cartLineTotal}>{formatVND(item.price * item.quantity)}</span>
                                </div>
                                <div className={styles.qtyRow}>
                                    <button className={styles.qtyBtn} onClick={() => updateQty(item.key, -1)}>
                                        <Minus size={11} />
                                    </button>
                                    <span className={styles.qtyNum}>{item.quantity}</span>
                                    <button className={styles.qtyBtn} onClick={() => updateQty(item.key, 1)}>
                                        <Plus size={11} />
                                    </button>
                                    <button className={styles.removeBtn} onClick={() => removeItem(item.key)}>
                                        <X size={11} />
                                    </button>
                                </div>
                            </div>
                        ))
                    )}
                </div>

                {/* Customer + Note */}
                <div className={styles.metaSection}>
                    <input
                        className={styles.metaInput}
                        type="text"
                        placeholder="Tên khách hàng (tùy chọn)"
                        value={customerName}
                        onChange={(e) => setCustomerName(e.target.value)}
                    />
                    <textarea
                        className={styles.metaTextarea}
                        placeholder="Ghi chú đơn hàng..."
                        value={note}
                        onChange={(e) => setNote(e.target.value)}
                        rows={2}
                    />
                </div>

                {/* Payment Method */}
                <div className={styles.paySection}>
                    <p className={styles.payLabel}>Phương thức thanh toán</p>
                    <div className={styles.payMethods}>
                        {paymentMethods.filter((m) => m.code !== 'WALLET' && m.code !== 'PAYOS').map((m) => {
                            const ICONS = { cash: '💵', vnpay: '🏦', momo: '🟣', wallet: '👛', payos: '💳' };
                            const icon = ICONS[m.icon?.toLowerCase()] ?? '💳';
                            return (
                                <button
                                    key={m.code}
                                    className={`${styles.payBtn} ${paymentMethod === m.code ? styles.payBtnActive : ''}`}
                                    onClick={() => setPaymentMethod(m.code)}
                                >
                                    {icon} {m.name}
                                </button>
                            );
                        })}
                    </div>
                    {paymentMethod === 'MOMO' && (() => {
                        const momoMethod = paymentMethods.find((m) => m.code === 'MOMO');
                        const subOpts = momoMethod?.subOptions ?? {};
                        const enabledSubs = Object.entries(subOpts).filter(([, v]) => v.enabled !== false);
                        if (enabledSubs.length === 0) return null;
                        return (
                            <select
                                className={styles.momoSelect}
                                value={momoType}
                                onChange={(e) => setMomoType(e.target.value)}
                            >
                                {enabledSubs.map(([code, sub]) => (
                                    <option key={code} value={code}>{sub.name}</option>
                                ))}
                            </select>
                        );
                    })()}
                </div>

                {/* Totals + Submit */}
                <div className={styles.footer}>
                    <div className={styles.totalRow}>
                        <span className={styles.totalLabel}>Tổng cộng</span>
                        <span className={styles.totalAmount}>{formatVND(subtotal)}</span>
                    </div>
                    {error && <p className={styles.errorMsg}>{error}</p>}
                    <Button
                        variant="primary"
                        className={styles.submitBtn}
                        onClick={handleSubmit}
                        disabled={cartItems.length === 0 || submitting}
                    >
                        {submitting
                            ? 'Đang xử lý...'
                            : paymentMethod === 'CASH'
                                ? `Tạo đơn — ${formatVND(subtotal)}`
                                : `Thanh toán ${paymentMethod} — ${formatVND(subtotal)}`
                        }
                    </Button>
                </div>
            </div>

            {/* ── Variant Picker Modal ── */}
            {pickerProduct && (
                <VariantPicker
                    product={pickerProduct}
                    onSelect={addToCart}
                    onClose={() => setPickerProduct(null)}
                />
            )}

            {/* ── Success Modal ── */}
            {successOrder && (
                <SuccessModal
                    order={successOrder}
                    onViewOrder={() => {
                        const id = successOrder.orderId || successOrder.id;
                        navigate(`/admin/orders/${id}`);
                        setSuccessOrder(null);
                        clearCart();
                    }}
                    onNewOrder={() => {
                        setSuccessOrder(null);
                        clearCart();
                    }}
                />
            )}
        </div>
    );
}
