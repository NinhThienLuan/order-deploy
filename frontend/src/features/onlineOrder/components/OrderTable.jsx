import React, { useState } from 'react';
import Button from '@/components/ui/Button';
import Badge from '@/components/ui/Badge';
import RejectOrderModal from './RejectOrderModal';
import { ORDER_STATUS_LABELS, ORDER_STATUS_COLORS } from '../orders.constants';
import styles from './OrderTable.module.css';

const OrderTable = ({ orders, onConfirm, onReject }) => {
  const [selectedOrder, setSelectedOrder] = useState(null);
  const [isRejectModalOpen, setIsRejectModalOpen] = useState(false);

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND'
    }).format(amount);
  };

  const formatTime = (timeString) => {
    const date = new Date(timeString);
    return date.toLocaleTimeString('vi-VN', {
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const handleRejectClick = (order) => {
    setSelectedOrder(order);
    setIsRejectModalOpen(true);
  };

  const handleRejectSubmit = (reason, note) => {
    onReject(selectedOrder.id, reason, note);
    setIsRejectModalOpen(false);
    setSelectedOrder(null);
  };

  return (
    <>
      <div className={styles.tableContainer}>
        <table className={styles.table}>
          <thead>
            <tr>
              <th>Mã đơn</th>
              <th>Khách hàng</th>
              <th>Thời gian</th>
              <th>Món order</th>
              <th>Ghi chú</th>
              <th>Tổng tiền</th>
              <th>Trạng thái</th>
              <th>Thao tác</th>
            </tr>
          </thead>
          <tbody>
            {orders.map((order) => (
              <tr key={order.id}>
                <td>
                  <span className={styles.orderId}>{order.id}</span>
                </td>
                <td>
                  <div className={styles.customerInfo}>
                    <div className={styles.customerName}>{order.customerName}</div>
                    <div className={styles.customerPhone}>{order.customerPhone}</div>
                  </div>
                </td>
                <td>
                  <span className={styles.orderTime}>{formatTime(order.orderTime)}</span>
                </td>
                <td>
                  <div className={styles.itemsList}>
                    {order.items.map((item, idx) => (
                      <div key={idx} className={styles.item}>
                        <span className={styles.itemName}>{item.name}</span>
                        <span className={styles.itemQuantity}>x{item.quantity}</span>
                      </div>
                    ))}
                  </div>
                </td>
                <td>
                  {order.note ? (
                    <span className={styles.note}>{order.note}</span>
                  ) : (
                    <span className={styles.noNote}>—</span>
                  )}
                </td>
                <td>
                  <span className={styles.totalAmount}>
                    {formatCurrency(order.totalAmount)}
                  </span>
                </td>
                <td>
                  <Badge variant={ORDER_STATUS_COLORS[order.status]}>
                    {ORDER_STATUS_LABELS[order.status]}
                  </Badge>
                </td>
                <td>
                  <div className={styles.actions}>
                    <Button
                      variant="primary"
                      size="small"
                      onClick={() => onConfirm(order.id)}
                    >
                      Xác nhận
                    </Button>
                    <Button
                      variant="ghost"
                      size="small"
                      onClick={() => handleRejectClick(order)}
                    >
                      Từ chối
                    </Button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <RejectOrderModal
        isOpen={isRejectModalOpen}
        onClose={() => setIsRejectModalOpen(false)}
        onSubmit={handleRejectSubmit}
        order={selectedOrder}
      />
    </>
  );
};

export default OrderTable;