import apiClient from '@/services/apiClient';

// Tries /api prefix first (as shown in Swagger), then falls back to non-/api path.
const requestWithApiFallback = async (method, path, config = {}) => {
  try {
    const first = await apiClient({ method, url: `/api${path}`, ...config });
    return first;
  } catch (error) {
    const status = error?.response?.status;
    if (status && status !== 404) throw error;
    const second = await apiClient({ method, url: path, ...config });
    return second;
  }
};

const normalizeSummary = (p) => ({
  id: p.id,
  productName: p.name,
  imageUrl: p.primaryImageUrl ?? null,
  price: p.basePrice ?? 0,
  status: p.active === false ? 'OutOfStock' : 'Available',
  categoryId: p.categoryId ?? null,
  categoryName: p.categoryName ?? null,
  description: p.description ?? null,
  type: p.type ?? null,
  badge: null,
});

const normalizeDetail = (p) => ({
  id: p.id,
  productName: p.name,
  imageUrl: p.images?.find((i) => i.isPrimary)?.imageUrl ?? p.images?.[0]?.imageUrl ?? null,
  price: p.variants?.find((v) => v.active)?.price ?? p.basePrice ?? 0,
  status: p.active === false ? 'OutOfStock' : 'Available',
  categoryId: p.categoryId ?? null,
  categoryName: p.categoryName ?? null,
  description: p.description ?? null,
  type: p.type ?? null,
  badge: null,
  variants: (p.variants ?? []).map((v) => ({
    id: v.id,
    sizeName: v.sizeName,
    price: v.price,
    active: v.active,
  })),
  images: (p.images ?? []).map((i) => ({
    imageUrl: i.imageUrl,
    isPrimary: i.isPrimary,
  })),
});

export const getProducts = async ({ page = 0, size = 12, categoryId, search, type } = {}) => {
  const params = { page, size };
  if (categoryId) params.categoryId = categoryId;
  if (search) params.search = search;
  if (type) params.type = type;

  const res = await requestWithApiFallback('get', '/v1/products', { params });
  const pageData = res.data?.result ?? {};

  return {
    content: (pageData.content ?? []).map(normalizeSummary),
    totalElements: pageData.totalElements ?? 0,
    totalPages: pageData.totalPages ?? 1,
    page: pageData.number ?? page,
  };
};

export const getProductById = async (id) => {
  const res = await requestWithApiFallback('get', `/v1/products/${id}`);
  return normalizeDetail(res.data?.result ?? {});
};

export const getCategories = async () => {
  const res = await requestWithApiFallback('get', '/v1/products/categories');
  return (res.data?.result ?? []).map((c) => ({
    id: c.id,
    name: c.name,
    description: c.description ?? null,
    parentId: c.parentId ?? null,
  }));
};

// Swagger: POST /api/v1/products
export const createProduct = async (payload) => {
  const res = await requestWithApiFallback('post', '/v1/products', { data: payload });
  return res.data?.result;
};

// Keep backward compatibility for existing imports
export const createSignatureProduct = async (payload) => createProduct(payload);

// Swagger: PUT /api/v1/products/{id}
export const updateProduct = async (id, payload) => {
  const res = await requestWithApiFallback('put', `/v1/products/${id}`, { data: payload });
  return res.data?.result;
};

// Swagger: DELETE /api/v1/products/{id}
export const deleteProduct = async (id) => {
  const res = await requestWithApiFallback('delete', `/v1/products/${id}`);
  return res.data?.result;
};

// Swagger: PATCH /api/v1/products/{id}/active
export const toggleProductActive = async (id, active) => {
  const res = await requestWithApiFallback('patch', `/v1/products/${id}/active`, {
    data: typeof active === 'boolean' ? { active } : undefined,
  });
  return res.data?.result;
};
