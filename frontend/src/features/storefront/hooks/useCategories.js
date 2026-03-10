import { useState, useEffect } from 'react';
import { getCategories } from '@/features/products/services/categories.service';

/**
 * useCategories
 * Hook to manage categories in the storefront.
 * Provides a mapping utility to find category IDs by slug/name.
 */
export const useCategories = () => {
    const [categories, setCategories] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchCategories = async () => {
            try {
                const data = await getCategories();
                setCategories(data || []);
            } catch (error) {
                console.error("Failed to fetch categories for hook:", error);
            } finally {
                setLoading(false);
            }
        };

        fetchCategories();
    }, []);

    /**
     * Resolves a category ID based on a slug or name.
     * This is used by the HomePage to link segments to specific menu categories.
     * 
     * @param {string} slug 
     * @returns {string|null}
     */
    const getCategoryId = (slug) => {
        if (!slug || !categories.length) return null;
        
        const target = slug.toLowerCase();
        const found = categories.find(c => 
            c.name.toLowerCase().includes(target) || 
            (c.description && c.description.toLowerCase().includes(target))
        );
        
        return found ? found.id : null;
    };

    return { categories, loading, getCategoryId };
};
