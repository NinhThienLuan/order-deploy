import apiClient from "../../services/apiClient";

const ADMIN_BASE = '/api/v1/admin/categories';
const PUBLIC_BASE = '/api/v1/products/categories';

/**
 * Get all active categories (public endpoint).
 */
export const getCategories = async () => {
    const res = await apiClient.get(PUBLIC_BASE);
    return res.data.result;
};

/**
 * Get all categories via admin endpoint (includes all, for admin pages).
 */
export const getAdminCategories = async () => {
    const res = await apiClient.get(ADMIN_BASE);
    return res.data.result;
};

/**
 * Create a new category.
 * @param {Object} payload - e.g. { name, description }
 */
export const createCategory = async (payload) => {
    const res = await apiClient.post(ADMIN_BASE, payload);
    return res.data.result;
};

/**
 * Update an existing category.
 * @param {string} id
 * @param {Object} payload - e.g. { name, description }
 */
export const updateCategory = async (id, payload) => {
    const res = await apiClient.put(`${ADMIN_BASE}/${id}`, payload);
    return res.data.result;
};

/**
 * Delete a category by ID.
 * @param {string} id
 */
export const deleteCategory = async (id) => {
    await apiClient.delete(`${ADMIN_BASE}/${id}`);
};
