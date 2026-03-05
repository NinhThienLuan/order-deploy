import { useState, useEffect } from 'react';
import { getCategories } from '@/features/categories/categories.service';

/**
 * Fetches all categories from the API and provides a helper to find
 * a category ID by a keyword matched against the category name.
 *
 * Usage:
 *   const { getCategoryId } = useCategories();
 *   getCategoryId('espresso')  // → real UUID or undefined
 */
export const useCategories = () => {
    const [categories, setCategories] = useState([]);

    useEffect(() => {
        getCategories()
            .then(setCategories)
            .catch(() => setCategories([]));
    }, []);

    /**
     * Returns the id of the first category whose name contains `keyword` (case-insensitive).
     * Falls back to undefined if not found yet.
     */
    const getCategoryId = (keyword) => {
        const found = categories.find(cat =>
            cat.name.toLowerCase().includes(keyword.toLowerCase())
        );
        return found?.id;
    };

    return { categories, getCategoryId };
};
