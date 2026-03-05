import { useEffect, useState } from "react";
import OrderTable from "../components/OrderTable";
import { getAdminOrders } from "../../orders/orders.service";
import Navbar from "../../../layouts/Navbar";
import styles from "./OrderManagementPage.module.css";

const ORDER_STATUSES = ["PENDING", "PAID", "PREPARING", "READY", "COMPLETED", "CANCELED"];

export default function OrderManagementPage() {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);

  const [statusFilter, setStatusFilter] = useState("");
  const [fromDate, setFromDate] = useState("");
  const [toDate, setToDate] = useState("");
  const [searchId, setSearchId] = useState("");

  const fetchOrders = async (params = {}) => {
    setLoading(true);
    try {
      const data = await getAdminOrders(params);
      setOrders(data.content || []);
    } catch (error) {
      console.error("Failed to fetch orders:", error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchOrders();
  }, []);

  const handleFilter = () => {
    const params = {};
    if (statusFilter) params.status = statusFilter;
    if (fromDate) params.fromDate = fromDate;
    if (toDate) params.toDate = toDate;
    fetchOrders(params);
  };

  const handleReset = () => {
    setStatusFilter("");
    setFromDate("");
    setToDate("");
    setSearchId("");
    fetchOrders();
  };

  const displayedOrders = searchId.trim()
    ? orders.filter((o) =>
        (o.id || "").toLowerCase().includes(searchId.trim().toLowerCase())
      )
    : orders;

  return (
    <>
      <Navbar />
      <div className={styles.page}>
        <div className={styles.container}>
          <h1 className={styles.pageTitle}>Coffee Order Management</h1>

          <div className={styles.filterBar}>
            <input
              placeholder="Search by order ID..."
              className={styles.searchInput}
              value={searchId}
              onChange={(e) => setSearchId(e.target.value)}
            />

            <select
              className={styles.statusSelect}
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
            >
              <option value="">All Status</option>
              {ORDER_STATUSES.map((s) => (
                <option key={s} value={s}>
                  {s.charAt(0) + s.slice(1).toLowerCase()}
                </option>
              ))}
            </select>

            <input
              type="date"
              className={styles.dateInput}
              value={fromDate}
              onChange={(e) => setFromDate(e.target.value)}
              title="From date"
            />
            <input
              type="date"
              className={styles.dateInput}
              value={toDate}
              onChange={(e) => setToDate(e.target.value)}
              title="To date"
            />

            <button className={styles.filterBtn} onClick={handleFilter}>
              Filter
            </button>
            <button className={styles.resetBtn} onClick={handleReset}>
              Reset
            </button>
          </div>

          {loading ? (
            <div className={styles.loading}>Loading orders...</div>
          ) : (
            <OrderTable orders={displayedOrders} />
          )}
        </div>
      </div>
    </>
  );
}