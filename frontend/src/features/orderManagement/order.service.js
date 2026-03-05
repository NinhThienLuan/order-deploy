import apiClient from "../../services/apiClient";

/**
 * GET /v1/orders — Admin/Manager order history with filters
 * @param {{ page?: number, size?: number, status?: string, branchId?: number, fromDate?: string, toDate?: string }} params
 */
export const getOrders = async (params) => {
  const res = await apiClient.get("/api/v1/orders", { params });
  return res.data.result;
};

/**
 * GET /v1/orders/:id — Order detail (CUSTOMER, STORE_MANAGER, FRANCHISE_ADMIN)
 */
export const getOrderDetail = async (id) => {
  const res = await apiClient.get(`/api/v1/orders/${id}`);
  return res.data.result;
};

/**
 * GET /v1/orders/:id/status — Order status (CUSTOMER, FRANCHISE_ADMIN, STORE_MANAGER)
 */
export const getOrderStatus = async (id) => {
  const res = await apiClient.get(`/api/v1/orders/${id}/status`);
  return res.data.result;
};

/**
 * PATCH /v1/orders/:id/cancel — Cancel an order
 */
export const cancelOrder = async (id) => {
  const res = await apiClient.patch(`/api/v1/orders/${id}/cancel`);
  return res.data.result;
};