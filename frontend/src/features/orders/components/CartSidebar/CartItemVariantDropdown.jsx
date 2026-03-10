import { useState, useEffect } from 'react';
import { getProductById } from '@/features/products/services/products.service';
import styles from './CartItemVariantDropdown.module.css';

/**
 * Flexible variant selector that supports 'select' (dropdown) and 'buttons' (picker) layouts.
 */
const CartItemVariantDropdown = ({ 
    productId, 
    currentVariantId, 
    onChange, 
    layout = 'select' 
}) => {
    const [variants, setVariants] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        let isMounted = true;
        getProductById(productId)
            .then(data => {
                if (isMounted) {
                    setVariants(data.variants || []);
                    setLoading(false);
                }
            })
            .catch(() => {
                if (isMounted) setLoading(false);
            });
        return () => { isMounted = false; };
    }, [productId]);

    if (loading) {
        return <div className={styles.variantLoading}>Brewing options...</div>;
    }

    if (variants.length === 0) return null;

    // Render Buttons Layout
    if (layout === 'buttons') {
        return (
            <div className={styles.variantPicker}>
                {variants.map((v) => (
                    <button
                        key={v.id}
                        className={`${styles.pickerBtn} ${currentVariantId === v.id ? styles.pickerBtnActive : ''}`}
                        onClick={() => onChange(v)}
                    >
                        {v.sizeName}
                    </button>
                ))}
            </div>
        );
    }

    // Render Dropdown Layout (Default)
    return (
        <div className={styles.container}>
            <select
                className={styles.variantSelect}
                value={currentVariantId || ''}
                onChange={(e) => {
                    const v = variants.find((varItem) => String(varItem.id) === e.target.value);
                    if (v) onChange(v);
                }}
            >
                {!currentVariantId && <option value="">Select size...</option>}
                {variants.map((v) => (
                    <option key={v.id} value={v.id}>
                        {v.sizeName}
                    </option>
                ))}
            </select>
        </div>
    );
};

export default CartItemVariantDropdown;
