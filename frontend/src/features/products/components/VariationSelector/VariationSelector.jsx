import { useState, useEffect } from 'react';
import { getProductById } from '../../services/products.service';
import styles from './VariationSelector.module.css';

const formatVND = (n) =>
    new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(n ?? 0);

const VariationSelector = ({ productId, onSelect, currentVariantId = null, onCancel }) => {
    const [product, setProduct] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        let isMounted = true;
        getProductById(productId)
            .then(data => {
                if (isMounted) {
                    setProduct(data);
                    setLoading(false);
                }
            })
            .catch(() => {
                if (isMounted) setLoading(false);
            });
        return () => { isMounted = false; };
    }, [productId]);

    if (loading) {
        return <div className={styles.loading}>Loading variations...</div>;
    }

    if (!product || !product.variants || product.variants.length === 0) {
        return <div className={styles.error}>No variations available for this product.</div>;
    }

    return (
        <div className={styles.container}>
            <div className={styles.header}>
                <h4 className={styles.title}>Select Variation</h4>
                {onCancel && (
                    <button className={styles.closeBtn} onClick={onCancel} aria-label="Cancel">
                        <svg width="18" height="18" viewBox="0 0 20 20" fill="none">
                            <path d="M15 5L5 15M5 5l10 10" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" />
                        </svg>
                    </button>
                )}
            </div>
            <div className={styles.grid}>
                {product.variants.map((v) => {
                    const isActive = v.active !== false;
                    const isSelected = v.id === currentVariantId;

                    return (
                        <button
                            key={v.id}
                            className={`${styles.variantBtn} ${isSelected ? styles.selected : ''} ${!isActive ? styles.disabled : ''}`}
                            onClick={() => isActive && onSelect(v, product)}
                            disabled={!isActive}
                        >
                            <span className={styles.variantName}>{v.sizeName}</span>
                            <span className={styles.variantPrice}>{formatVND(v.price)}</span>
                        </button>
                    );
                })}
            </div>
        </div>
    );
};

export default VariationSelector;
