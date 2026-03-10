// src/features/onlineOrder/pages/OnlineOrderManagementPage.jsx
import React, { useState, useEffect } from 'react';
import styles from './OnlineOrderManagementPage.module.css';

// Mock data cho online orders
const MOCK_ONLINE_ORDERS = [
  {
    id: 'ONL-001',
    customerName: 'Nguyễn Văn An',
    customerPhone: '0901.234.567',
    orderTime: '2024-01-15T10:30:00',
    items: [
      { name: 'Cà phê sữa đá', quantity: 2, price: 35000 },
      { name: 'Bánh mì que', quantity: 1, price: 15000 }
    ],
    totalAmount: 85000,
    status: 'pending',
    note: 'Giao đến 123 Nguyễn Trãi, Q1',
    paymentMethod: 'COD'
  },
  {
    id: 'ONL-002',
    customerName: 'Trần Thị Bình',
    customerPhone: '0902.345.678',
    orderTime: '2024-01-15T10:45:00',
    items: [
      { name: 'Matcha latte', quantity: 2, price: 49000 },
      { name: 'Bánh tiramisu', quantity: 1, price: 55000 }
    ],
    totalAmount: 153000,
    status: 'pending',
    note: 'Giao giờ hành chính',
    paymentMethod: 'Banking'
  }
];

const OnlineOrderManagementPage = () => {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [isAcceptingOrders, setIsAcceptingOrders] = useState(true);
  const [selectedOrder, setSelectedOrder] = useState(null);
  const [showRejectModal, setShowRejectModal] = useState(false);
  const [rejectReason, setRejectReason] = useState('');
  const [rejectNote, setRejectNote] = useState('');

  useEffect(() => {
    // Mock API call
    setTimeout(() => {
      setOrders(MOCK_ONLINE_ORDERS);
      setLoading(false);
    }, 500);
  }, []);

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

  const handleConfirm = (orderId) => {
    setOrders(orders.filter(order => order.id !== orderId));
    alert(`Đã xác nhận đơn ${orderId}`);
  };

  const handleReject = (order) => {
    setSelectedOrder(order);
    setShowRejectModal(true);
  };

  const submitReject = () => {
    if (!rejectReason || !rejectNote) {
      alert('Vui lòng nhập lý do và ghi chú');
      return;
    }
    
    setOrders(orders.filter(order => order.id !== selectedOrder.id));
    setShowRejectModal(false);
    setRejectReason('');
    setRejectNote('');
    alert(`Đã từ chối đơn ${selectedOrder.id}`);
  };

  const toggleOrders = () => {
    setIsAcceptingOrders(!isAcceptingOrders);
    alert(`${!isAcceptingOrders ? 'Đã bật' : 'Đã tắt'} nhận đơn online`);
  };

  if (loading) {
    return <div className={styles.loading}>Đang tải đơn hàng...</div>;
  }

  return (
    <div className={styles.container}>
      {/* Header */}
      <div className={styles.header}>
        <div>
          <h1 className={styles.pageTitle}>Quản lý đơn Online</h1>
          <p className={styles.pageSubtitle}>
            {orders.length} đơn đang chờ xác nhận
          </p>
        </div>
        
        <div className={styles.headerActions}>
          <div className={styles.toggleContainer}>
            <span className={styles.toggleLabel}>
              {isAcceptingOrders ? 'Đang nhận đơn' : 'Tạm ngừng nhận đơn'}
            </span>
            <button
              className={`${styles.toggleSwitch} ${isAcceptingOrders ? styles.active : ''}`}
              onClick={toggleOrders}
            >
              <span className={styles.toggleSlider} />
            </button>
          </div>
          
          <button 
            className={styles.refreshButton}
            onClick={() => window.location.reload()}
          >
            Tải lại
          </button>
        </div>
      </div>

      {/* Bảng đơn hàng */}
      <div className={styles.tableContainer}>
        <table className={styles.table}>
          <thead>
            <tr>
              <th>Mã đơn</th>
              <th>Khách hàng</th>
              <th>Thời gian</th>
              <th>Món order</th>
              <th>Địa chỉ</th>
              <th>Thanh toán</th>
              <th>Tổng tiền</th>
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
                  <span className={styles.address}>{order.note}</span>
                </td>
                <td>
                  <span className={`${styles.paymentMethod} ${styles[order.paymentMethod]}`}>
                    {order.paymentMethod}
                  </span>
                </td>
                <td>
                  <span className={styles.totalAmount}>
                    {formatCurrency(order.totalAmount)}
                  </span>
                </td>
                <td>
                  <div className={styles.actions}>
                    <button 
                      className={`${styles.button} ${styles.confirmBtn}`}
                      onClick={() => handleConfirm(order.id)}
                    >
                      Xác nhận
                    </button>
                    <button 
                      className={`${styles.button} ${styles.rejectBtn}`}
                      onClick={() => handleReject(order)}
                    >
                      Từ chối
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* Modal từ chối */}
      {showRejectModal && (
        <div className={styles.modalOverlay} onClick={() => setShowRejectModal(false)}>
          <div className={styles.modal} onClick={e => e.stopPropagation()}>
            <div className={styles.modalHeader}>
              <h2 className={styles.modalTitle}>Từ chối đơn hàng</h2>
              <p className={styles.modalOrderInfo}>
                Mã đơn: <span className={styles.modalOrderId}>{selectedOrder?.id}</span>
              </p>
            </div>

            <div className={styles.modalContent}>
              <div className={styles.field}>
                <label className={styles.fieldLabel}>
                  Lý do từ chối <span className={styles.required}>*</span>
                </label>
                <select 
                  className={styles.fieldInput}
                  value={rejectReason}
                  onChange={(e) => setRejectReason(e.target.value)}
                >
                  <option value="">Chọn lý do</option>
                  <option value="Hết nguyên liệu">Hết nguyên liệu</option>
                  <option value="Quá tải đơn hàng">Quá tải đơn hàng</option>
                  <option value="Ngoài khu vực giao hàng">Ngoài khu vực giao hàng</option>
                  <option value="Khác">Lý do khác</option>
                </select>
              </div>

              {rejectReason === 'Khác' && (
                <div className={styles.field}>
                  <label className={styles.fieldLabel}>
                    Lý do cụ thể <span className={styles.required}>*</span>
                  </label>
                  <input
                    type="text"
                    className={styles.fieldInput}
                    placeholder="Nhập lý do từ chối..."
                    value={rejectReason}
                    onChange={(e) => setRejectReason(e.target.value)}
                  />
                </div>
              )}

              <div className={styles.field}>
                <label className={styles.fieldLabel}>
                  Ghi chú gửi khách <span className={styles.required}>*</span>
                </label>
                <textarea
                  className={styles.fieldTextarea}
                  rows="4"
                  placeholder="Nhập nội dung thông báo gửi cho khách..."
                  value={rejectNote}
                  onChange={(e) => setRejectNote(e.target.value)}
                />
                <p className={styles.fieldHint}>
                  Nội dung này sẽ được gửi cho khách hàng qua SMS
                </p>
              </div>
            </div>

            <div className={styles.modalFooter}>
              <button 
                className={`${styles.button} ${styles.cancelBtn}`}
                onClick={() => setShowRejectModal(false)}
              >
                Hủy
              </button>
              <button 
                className={`${styles.button} ${styles.submitRejectBtn}`}
                onClick={submitReject}
              >
                Xác nhận từ chối
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default OnlineOrderManagementPage;