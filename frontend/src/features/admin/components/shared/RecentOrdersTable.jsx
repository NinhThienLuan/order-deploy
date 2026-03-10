import styles from './RecentOrdersTable.module.css';

function getStatusBadge(status) {
  const key = status.toLowerCase();
  if (key === 'preparing') return styles.badgePreparing;
  if (key === 'completed') return styles.badgeCompleted;
  return styles.badgePending;
}

export default function RecentOrdersTable({ orders, title = 'Recent Orders' }) {
  return (
    <div className={styles.tableSection}>
      <h2 className={styles.tableTitle}>{title}</h2>
      <table className={styles.table}>
        <thead>
          <tr>
            <th>Order ID</th>
            <th>Customer</th>
            <th>Items</th>
            <th>Total</th>
            <th>Status</th>
          </tr>
        </thead>
        <tbody>
          {orders.map((order) => (
            <tr key={order.id}>
              <td>{order.id}</td>
              <td>{order.customer}</td>
              <td>{order.items}</td>
              <td>{order.total}</td>
              <td>
                <span className={`${styles.badge} ${getStatusBadge(order.status)}`}>
                  {order.status}
                </span>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
