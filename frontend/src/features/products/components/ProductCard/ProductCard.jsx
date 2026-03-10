import { Link } from 'react-router-dom';
import CartItemVariantDropdown from '@/features/orders/components/CartSidebar/CartItemVariantDropdown';
import styles from './ProductCard.module.css';

/**
 * Shared Product Card component used in ProductListPage and CatalogSection (Landing).
 * Ensures consistency in look, feel, and variation selection flow.
 */
const ProductCard = ({ 
    product, 
    isAdded, 
    quickAddProductId, 
    setQuickAddProductId, 
    onAddVariant 
}) => {
    const isOut = product.status === 'OutOfStock';
    
    const formatVND = (n) =>
        new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(n ?? 0);

    return (
        <div 
            className={`${styles.card} ${isOut ? styles.cardOut : ''}`}
            onMouseLeave={() => setQuickAddProductId(null)}
        >
            {/* Image area */}
            <div className={styles.imageLink}>
                <Link to={`/menu/${product.id}`} className={styles.imageWrap}>
                    <img
                        src={product.imageUrl || 'https://placehold.co/400x400/EFECE3/231F1E?text=No+Image'}
                        alt={product.productName}
                        className={styles.image}
                        loading="lazy"
                        onError={(e) => { e.target.src = 'https://placehold.co/400x400/EFECE3/231F1E?text=No+Image'; }}
                    />

                    {/* Badge */}
                    {product.badge && !isOut && (
                        <span className={styles.badge}>{product.badge}</span>
                    )}
                    {isOut && (
                        <span className={`${styles.badge} ${styles.badgeOut}`}>Out of Stock</span>
                    )}
                </Link>

                {/* Add to cart icon button */}
                {!isOut && (
                    <button
                        className={`${styles.cartIconBtn} ${isAdded ? styles.cartIconBtnAdded : ''}`}
                        onClick={(e) => {
                            e.preventDefault();
                            e.stopPropagation();
                            setQuickAddProductId(product.id);
                        }}
                        disabled={isAdded}
                        aria-label={isAdded ? 'Added to cart' : 'Add to cart'}
                    >
                        {isAdded ? (
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                                <polyline points="20 6 9 17 4 12" />
                            </svg>
                        ) : (
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                                <circle cx="9" cy="21" r="1" /><circle cx="20" cy="21" r="1" />
                                <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6" />
                            </svg>
                        )}
                    </button>
                )}
            </div>

            {/* Variation Selector Overlay */}
            {quickAddProductId === product.id && (
                <div className={styles.variationOverlay}>
                    <button 
                        className={styles.overlayClose}
                        onClick={() => setQuickAddProductId(null)}
                    >
                        ✕
                    </button>
                    <div className={styles.overlayInner}>
                        <div className={styles.overlayLabel}>Select Size</div>
                        <CartItemVariantDropdown
                            productId={product.id}
                            layout="buttons"
                            onChange={(variant) => {
                                onAddVariant(variant);
                                setQuickAddProductId(null);
                            }}
                        />
                    </div>
                </div>
            )}

            {/* Info */}
            <div className={styles.info}>
                <span className={styles.category}>{product.categoryName || ''}</span>
                <div className={styles.nameRow}>
                    <h3 className={styles.name}>{product.productName}</h3>
                    <span className={styles.price}>{formatVND(product.price)}</span>
                </div>
            </div>
        </div>
    );
};

export default ProductCard;
