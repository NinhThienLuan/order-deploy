import apiClient from "../../services/apiClient";

// ── Response normalizers ─────────────────────────────────────────────────────
// Backend shape  →  flat UI shape used by ProductListPage & ProductDetailPage

/**
 * Map ProductSummaryResponse (GET /v1/products list item) to UI shape.
 */
const normalizeSummary = (p) => ({
    id:           p.id,
    productName:  p.name,
    imageUrl:     p.primaryImageUrl ?? null,
    price:        p.basePrice       ?? 0,
    status:       p.active === false ? 'OutOfStock' : 'Available',
    categoryId:   p.categoryId      ?? null,
    categoryName: p.categoryName    ?? null,
    description:  p.description     ?? null,
    type:         p.type            ?? null,
    badge:        null,
});

/**
 * Map ProductDetailResponse (GET /v1/products/:id) to UI shape.
 * Keeps variants + images arrays for the detail page.
 */
const normalizeDetail = (p) => ({
    id:           p.id,
    productName:  p.name,
    imageUrl:     p.images?.find((i) => i.isPrimary)?.imageUrl
                  ?? p.images?.[0]?.imageUrl
                  ?? null,
    price:        p.variants?.find((v) => v.active)?.price ?? 0,
    status:       p.active === false ? 'OutOfStock' : 'Available',
    categoryId:   p.categoryId   ?? null,
    categoryName: p.categoryName ?? null,
    description:  p.description  ?? null,
    type:         p.type         ?? null,
    badge:        null,
    variants: (p.variants ?? []).map((v) => ({
        id:       v.id,
        sizeName: v.sizeName,
        price:    v.price,
        active:   v.active,
    })),
    images: (p.images ?? []).map((i) => ({
        imageUrl:  i.imageUrl,
        isPrimary: i.isPrimary,
    })),
});

// ── GET /v1/products ─────────────────────────────────────────────────────────
/**
 * Fetch paginated product list with optional filters.
 * @param {{ page?: number, size?: number, categoryId?: string, search?: string, type?: string }} params
 * @returns {{ content: Product[], totalElements: number, totalPages: number, page: number }}
 */
export const getProducts = async ({ page = 0, size = 12, categoryId, search, type } = {}) => {
    const params = { page, size };
    if (categoryId) params.categoryId = categoryId;
    if (search)     params.search     = search;
    if (type)       params.type       = type;

    const res = await apiClient.get('/api/v1/products', { params });
    const pageData = res.data.result;

    return {
        content:       (pageData.content ?? []).map(normalizeSummary),
        totalElements: pageData.totalElements ?? 0,
        totalPages:    pageData.totalPages    ?? 1,
        page:          pageData.number        ?? page,
    };
};

// ── GET /v1/products/:id ─────────────────────────────────────────────────────
/**
 * Fetch full product detail including variants and images.
 * @param {string} id
 * @returns {Product}
 */
export const getProductById = async (id) => {
    const res = await apiClient.get(`/api/v1/products/${id}`);
    return normalizeDetail(res.data.result);
};

// ── GET /v1/products/categories ──────────────────────────────────────────────
/**
 * Fetch all active categories.
 * @returns {Category[]}
 */
export const getCategories = async () => {
    const res = await apiClient.get('/api/v1/products/categories');
    return (res.data.result ?? []).map((c) => ({
        id:          c.id,
        name:        c.name,
        description: c.description ?? null,
        parentId:    c.parentId    ?? null,
    }));
};

// ── POST /v1/products/signature ──────────────────────────────────────────────
/**
 * Create a Signature product for a Store. Only Store Managers can do this.
 * @param {Object} payload
 * @returns {Product}
 */
export const createSignatureProduct = async (payload) => {
    const res = await apiClient.post('/api/v1/products/signature', payload);
    return res.data.result;
};

// ── PUT /v1/products/:id ─────────────────────────────────────────────────────
/**
 * Update a product (Master or Signature).
 * @param {string} id
 * @param {Object} payload
 * @returns {Product}
 */
export const updateProduct = async (id, payload) => {
    const res = await apiClient.put(`/api/v1/products/${id}`, payload);
    return res.data.result;
};

// ── DELETE /v1/products/:id ──────────────────────────────────────────────────
/**
 * Delete a product by ID.
 * @param {string} id
 */
export const deleteProduct = async (id) => {
    const res = await apiClient.delete(`/api/v1/products/${id}`);
    return res.data.result;
};
