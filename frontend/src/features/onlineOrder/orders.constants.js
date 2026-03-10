export const ORDER_STATUS_LABELS = {
  pending: 'Chờ xác nhận',
  confirmed: 'Đã xác nhận',
  rejected: 'Đã từ chối',
  completed: 'Hoàn thành'
};

export const ORDER_STATUS_COLORS = {
  pending: 'warning',
  confirmed: 'success',
  rejected: 'danger',
  completed: 'neutral'
};

export const REJECT_REASONS = [
  'Hết nguyên liệu',
  'Quá tải đơn hàng',
  'Khách ở xa',
  'Nghỉ bán món này',
  'Lý do khác'
];