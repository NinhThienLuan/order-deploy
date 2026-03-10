import axios from 'axios';
import apiClient from '@/services/apiClient';

const BASE = '/api/v1/admin/products';

// ── Normalizers ───────────────────────────────────────────────────────────────

/** Maps ProductSummaryResponse → UI shape used in list views */
const normalizeSummary = (p) => ({
  id: p.id,
  productName: p.name,
  imageUrl: p.primaryImageUrl ?? null,
  price: p.basePrice ?? 0,
  status: p.active === false ? 'Inactive' : 'Available',
  categoryId: p.categoryId ?? null,
  categoryName: p.categoryName ?? null,
  description: p.description ?? null,
  type: p.type ?? null,
});

/** Maps ProductDetailResponse → UI shape used in detail / edit views */
const normalizeDetail = (p) => ({
  id: p.id,
  productName: p.name,
  description: p.description ?? null,
  type: p.type ?? null,
  categoryId: p.categoryId ?? null,
  categoryName: p.categoryName ?? null,
  active: p.active,
  status: p.active === false ? 'Inactive' : 'Available',
  imageUrl: p.images?.find((i) => i.isPrimary)?.imageUrl
    ?? p.images?.[0]?.imageUrl
    ?? null,
  variants: p.variants ?? [],
  images: p.images ?? [],
});

// ── Product CRUD ──────────────────────────────────────────────────────────────

export const getAdminProducts = async ({ page = 0, size = 12, categoryId, search, active } = {}) => {
  const params = { page, size };
  if (categoryId) params.categoryId = categoryId;
  if (search) params.search = search;
  if (active !== undefined && active !== '') params.active = active;

  const res = await apiClient.get(BASE, { params });
  const pageData = res.data.result;
  return {
    content: (pageData.content ?? []).map(normalizeSummary),
    totalElements: pageData.totalElements ?? 0,
    totalPages: pageData.totalPages ?? 1,
    page: pageData.number ?? page,
  };
};

export const getAdminProductById = async (id) => {
  const res = await apiClient.get(`${BASE}/${id}`);
  return normalizeDetail(res.data.result);
};



export const createAdminProduct = async (payload) => {
  const res = await apiClient.post(BASE, payload);
  return normalizeDetail(res.data.result);
};

export const updateAdminProduct = async (id, payload) => {
  const res = await apiClient.put(`${BASE}/${id}`, payload);
  return normalizeDetail(res.data.result);
};

export const deleteAdminProduct = async (id) => {
  await apiClient.delete(`${BASE}/${id}`);
};

export const toggleAdminProductActive = async (id) => {
  const res = await apiClient.patch(`${BASE}/${id}/active`);
  return res.data.result;
};

// ── Variants ─────────────────────────────────────────────────────────────────

export const getProductVariants = async (productId) => {
  const res = await apiClient.get(`${BASE}/${productId}/variants`);
  return res.data.result ?? [];
};

export const createProductVariant = async (productId, payload) => {
  const res = await apiClient.post(`${BASE}/${productId}/variants`, { active: true, ...payload });
  return res.data.result;
};

export const updateProductVariant = async (productId, variantId, payload) => {
  const res = await apiClient.put(`${BASE}/${productId}/variants/${variantId}`, { active: true, ...payload });
  return res.data.result;
};

export const deleteProductVariant = async (productId, variantId) => {
  await apiClient.delete(`${BASE}/${productId}/variants/${variantId}`);
};

// ── Images ───────────────────────────────────────────────────────────────────

export const getProductImages = async (productId) => {
  const res = await apiClient.get(`${BASE}/${productId}/images`);
  return res.data.result ?? [];
};

export const uploadProductImages = async (productId, files, setPrimaryFirst = true) => {
  const form = new FormData();
  files.forEach((f) => form.append('files', f));
  
  // Use a direct axios call to avoid apiClient's global 'Content-Type: application/json' header.
  // Axios will automatically set the correct 'multipart/form-data' header with boundary when data is FormData.
  const res = await axios.post(
    `${apiClient.defaults.baseURL}${BASE}/${productId}/images?setPrimaryFirst=${setPrimaryFirst}`,
    form,
    { withCredentials: true }
  );
  return res.data.result ?? [];
};

export const setPrimaryImage = async (productId, imageId) => {
  const res = await apiClient.patch(`${BASE}/${productId}/images/${imageId}/primary`);
  return res.data.result;
};

export const deleteProductImage = async (imageId) => {
  await apiClient.delete(`${BASE}/images/${imageId}`);
};

export const deleteAllProductImages = async (productId) => {
  await apiClient.delete(`${BASE}/${productId}/images`);
};
