import React, { useState } from 'react';
import Button from '@/components/ui/Button';
import { REJECT_REASONS } from '../orders.constants';
import styles from './RejectOrderModal.module.css';

const RejectOrderModal = ({ isOpen, onClose, onSubmit, order }) => {
  const [reason, setReason] = useState('');
  const [note, setNote] = useState('');
  const [customReason, setCustomReason] = useState('');

  if (!isOpen || !order) return null;

  const handleSubmit = (e) => {
    e.preventDefault();
    const finalReason = reason === 'Lý do khác' ? customReason : reason;
    onSubmit(finalReason, note);
    resetForm();
  };

  const resetForm = () => {
    setReason('');
    setNote('');
    setCustomReason('');
  };

  const handleClose = () => {
    resetForm();
    onClose();
  };

  return (
    <div className={styles.overlay}>
      <div className={styles.modal}>
        <div className={styles.header}>
          <h2 className={styles.title}>Từ chối đơn hàng</h2>
          <p className={styles.orderInfo}>
            Mã đơn: <span className={styles.orderId}>{order.id}</span>
          </p>
        </div>

        <form onSubmit={handleSubmit}>
          <div className={styles.content}>
            <div className={styles.field}>
              <label className={styles.label}>
                Lý do từ chối <span className={styles.required}>*</span>
              </label>
              <select
                className={styles.select}
                value={reason}
                onChange={(e) => setReason(e.target.value)}
                required
              >
                <option value="">Chọn lý do</option>
                {REJECT_REASONS.map((r) => (
                  <option key={r} value={r}>{r}</option>
                ))}
              </select>
            </div>

            {reason === 'Lý do khác' && (
              <div className={styles.field}>
                <label className={styles.label}>
                  Lý do cụ thể <span className={styles.required}>*</span>
                </label>
                <input
                  type="text"
                  className={styles.input}
                  value={customReason}
                  onChange={(e) => setCustomReason(e.target.value)}
                  placeholder="Nhập lý do từ chối..."
                  required
                />
              </div>
            )}

            <div className={styles.field}>
              <label className={styles.label}>
                Ghi chú gửi khách <span className={styles.required}>*</span>
              </label>
              <textarea
                className={styles.textarea}
                value={note}
                onChange={(e) => setNote(e.target.value)}
                placeholder="Nhập nội dung thông báo gửi cho khách..."
                rows="4"
                required
              />
              <p className={styles.hint}>
                Nội dung này sẽ được gửi cho khách hàng qua SMS/Notification
              </p>
            </div>
          </div>

          <div className={styles.footer}>
            <Button
              type="button"
              variant="ghost"
              onClick={handleClose}
            >
              Hủy
            </Button>
            <Button
              type="submit"
              variant="danger"
            >
              Xác nhận từ chối
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default RejectOrderModal;