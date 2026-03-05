import styles from "./StatusBadge.module.css";

export default function StatusBadge({ status }) {
  const getStatusClass = (statusStr) => {
    switch (statusStr?.toUpperCase()) {
      case "COMPLETED":
        return styles.statusCompleted;
      case "PREPARING":
      case "READY":
        return styles.statusPreparing;
      case "CANCELLED":
        return styles.statusCancelled;
      case "PENDING":
      case "ORDERED":
      default:
        return styles.statusPending;
    }
  };

  return (
    <span className={`${styles.badge} ${getStatusClass(status)}`}>
      {status || "PENDING"}
    </span>
  );
}