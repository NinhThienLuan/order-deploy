import styles from './TopStoresTable.module.css';

function getStatusBadge(status) {
  const key = status.toLowerCase();
  if (key === 'excellent') return styles.badgeExcellent;
  if (key === 'good') return styles.badgeGood;
  return styles.badgeAttention;
}

export default function TopStoresTable({ stores, title = 'Top Performing Stores' }) {
  return (
    <div className={styles.tableSection}>
      <h2 className={styles.tableTitle}>{title}</h2>
      <table className={styles.table}>
        <thead>
          <tr>
            <th>ID</th>
            <th>Name</th>
            <th>Amount</th>
            <th>Status</th>
          </tr>
        </thead>
        <tbody>
          {stores.map((store) => (
            <tr key={store.id}>
              <td>{store.id}</td>
              <td>{store.name}</td>
              <td>{store.amount}</td>
              <td>
                <span className={`${styles.badge} ${getStatusBadge(store.status)}`}>
                  {store.status}
                </span>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
