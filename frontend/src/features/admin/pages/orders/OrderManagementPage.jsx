import { useEffect, useState } from "react";
import OrderTable from "@/features/admin/components/orders/OrderTable";
import { getAdminOrders } from "@/features/orders/services/order.service";
import Input from "@/components/ui/Input";
import Select from "@/components/ui/Select";
import Button from "@/components/ui/Button";
import { ChevronLeft, ChevronRight } from 'lucide-react';
import AdminPageHeader from "@/components/layout/AdminPageHeader";
import AdminFilterBar from "@/components/ui/AdminFilterBar";
import styles from "./OrderManagementPage.module.css";

const ORDER_STATUSES = ["PENDING", "PAID", "PREPARING", "READY", "COMPLETED", "CANCELED"];

export default function OrderManagementPage() {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);

  const [statusFilter, setStatusFilter] = useState("");
  const [fromDate, setFromDate] = useState("");
  const [toDate, setToDate] = useState("");
  const [searchId, setSearchId] = useState("");
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);

  const fetchOrders = async ({ nextPage = page, nextStatus = statusFilter, nextFrom = fromDate, nextTo = toDate } = {}) => {
    setLoading(true);
    try {
      const data = await getAdminOrders({
        page: nextPage,
        size: 10,
        status: nextStatus || undefined,
        fromDate: nextFrom || undefined,
        toDate: nextTo || undefined
      });
      setOrders(data.content || []);
      setTotalPages(data.totalPages || 1);
      setPage(data.page || 1);
    } catch (error) {
      console.error("Failed to fetch orders:", error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchOrders();
  }, []);

  // ── Auto-Filter handlers ────────────────────────────────────────────────────────
  useEffect(() => {
    const delayDebounceFn = setTimeout(() => {
      fetchOrders({ nextPage: 1, nextStatus: statusFilter, nextFrom: fromDate, nextTo: toDate });
    }, 400); // 400ms debounce for typing

    return () => clearTimeout(delayDebounceFn);
  }, [searchId, statusFilter, fromDate, toDate]);

  const displayedOrders = searchId.trim()
    ? orders.filter((o) =>
        (o.id || "").toLowerCase().includes(searchId.trim().toLowerCase())
      )
    : orders;

  const paginationUI = (
    <>
      <span className={styles.pageInfo}>Page {page} of {totalPages}</span>
      <div className={styles.paginationBtns}>
        <Button 
          variant="ghost" 
          size="sm" 
          onClick={() => fetchOrders({ nextPage: page - 1 })} 
          disabled={page <= 1}
          title="Previous Page"
        >
          <ChevronLeft size={16} style={{ marginRight: '4px' }} />
          Prev
        </Button>
        <Button 
          variant="ghost" 
          size="sm" 
          onClick={() => fetchOrders({ nextPage: page + 1 })} 
          disabled={page >= totalPages}
          title="Next Page"
        >
          Next
          <ChevronRight size={16} style={{ marginLeft: '4px' }} />
        </Button>
      </div>
    </>
  );

  return (
    <div className={styles.main}>
      <div className={styles.container}>
        <AdminPageHeader 
          preTitle="ORDER MANAGEMENT"
          title="Order Management"
          subtitle="View and manage customer orders, track fulfillment status, and process receipts."
        />

        <AdminFilterBar>
          <div className={styles.searchBox}>
            <Input
              placeholder="Search by ID..."
              value={searchId}
              onChange={(e) => setSearchId(e.target.value)}
              className={styles.searchInput}
            />
          </div>

          <Select
            className={styles.filterSelect}
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
          >
            <option value="">All Statuses</option>
            {ORDER_STATUSES.map((s) => (
              <option key={s} value={s}>
                {s.charAt(0) + s.slice(1).toLowerCase()}
              </option>
            ))}
          </Select>

          <Input
            type="date"
            className={styles.dateInput}
            value={fromDate}
            onChange={(e) => setFromDate(e.target.value)}
            title="From date"
          />
          <Input
            type="date"
            className={styles.dateInput}
            value={toDate}
            onChange={(e) => setToDate(e.target.value)}
            title="To date"
          />
        </AdminFilterBar>

        {loading ? (
          <div className={styles.loading}>Loading orders...</div>
        ) : (
          <div className={styles.tableWrap}>
             <OrderTable orders={displayedOrders} pagination={paginationUI} />
          </div>
        )}
      </div>
    </div>
  );
}
