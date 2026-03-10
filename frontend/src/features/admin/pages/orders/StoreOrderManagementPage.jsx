import { useEffect, useState } from 'react';
import AdminPageHeader from '@/components/layout/AdminPageHeader';
import AdminFilterBar from '@/components/ui/AdminFilterBar';
import OrderTable from "@/features/admin/components/orders/OrderTable";
import { getAdminOrders } from '@/features/orders/services/order.service';
import Button from '@/components/ui/Button';
import { ChevronLeft, ChevronRight } from 'lucide-react';
import styles from './StoreOrderManagementPage.module.css';

const ORDER_STATUSES = ['PENDING', 'PAID', 'PREPARING', 'READY', 'COMPLETED', 'CANCELED'];

export default function StoreOrderManagementPage() {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);

  const [statusFilter, setStatusFilter] = useState('');
  const [fromDate, setFromDate] = useState('');
  const [toDate, setToDate] = useState('');
  const [searchId, setSearchId] = useState('');
  const [page, setPage] = useState(0);
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
      setPage(data.page || 0);
    } catch (error) {
      console.error('Failed to fetch orders:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchOrders();
  }, []);

  const handleFilter = () => {
    fetchOrders({ nextPage: 0, nextStatus: statusFilter, nextFrom: fromDate, nextTo: toDate });
  };

  const handleReset = () => {
    setStatusFilter('');
    setFromDate('');
    setToDate('');
    setSearchId('');
    fetchOrders({ nextPage: 0, nextStatus: '', nextFrom: '', nextTo: '' });
  };

  const displayedOrders = searchId.trim()
    ? orders.filter((o) =>
        (o.id || '').toLowerCase().includes(searchId.trim().toLowerCase())
      )
    : orders;

  const paginationUI = (
    <>
      <span className={styles.pageInfo}>Page {page + 1} of {totalPages}</span>
      <div className={styles.paginationBtns}>
        <Button 
          variant="ghost" 
          size="sm" 
          onClick={() => fetchOrders({ nextPage: page - 1 })} 
          disabled={page <= 0}
          title="Previous Page"
        >
          <ChevronLeft size={16} style={{ marginRight: '4px' }} />
          Prev
        </Button>
        <Button 
          variant="ghost" 
          size="sm" 
          onClick={() => fetchOrders({ nextPage: page + 1 })} 
          disabled={page + 1 >= totalPages}
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
            title="Order Management"
            subtitle="Manage your store's orders and track fulfillment."
          />

          <AdminFilterBar>
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

            <button className={styles.filterBtn} onClick={handleFilter}>Filter</button>
            <button className={styles.resetBtn} onClick={handleReset}>Reset</button>
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
