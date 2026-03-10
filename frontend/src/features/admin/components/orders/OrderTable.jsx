import { useNavigate } from "react-router-dom";
import StatusBadge from "@/features/admin/components/shared/StatusBadge";
import AdminTable from "@/components/ui/AdminTable";
import styles from "./OrderTable.module.css";

function handlePrintReceipt(order) {
  const items = order.items || [];
  const subtotal = items.reduce((s, i) => s + (i.subtotal ?? i.quantity * i.unitPrice), 0);
  const total = order.totalAmount ?? subtotal;
  const date = order.createdAt
    ? new Date(order.createdAt).toLocaleString()
    : "—";
  const fmtVND = (n) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(n ?? 0);
  
  const html = `
    <html>
    <head>
      <title>Receipt #${order.id}</title>
      <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: 'Inter', Arial, sans-serif; padding: 40px; color: #231F1E; }
        .receipt { max-width: 420px; margin: 0 auto; }
        h1 { font-size: 22px; text-align: center; margin-bottom: 4px; }
        .address { text-align: center; color: #888; font-size: 12px; margin-bottom: 24px; }
        .info { display: flex; justify-content: space-between; font-size: 13px; margin-bottom: 16px; }
        hr { border: none; border-top: 1px solid #ddd; margin: 16px 0; }
        .row { display: flex; justify-content: space-between; font-size: 13px; margin-bottom: 8px; }
        .row.header { font-weight: 700; margin-bottom: 12px; }
        .total-line { border-top: 2px solid #231F1E; padding-top: 12px; margin-top: 12px; }
        .total { display: flex; justify-content: space-between; font-size: 18px; font-weight: 700; color: #D98324; }
        .footer { text-align: center; margin-top: 28px; font-size: 11px; color: #888; }
        @media print { body { padding: 20px; } }
      </style>
    </head>
    <body>
      <div class="receipt">
        <h1>Lumière Coffee</h1>
        <div class="address">123 Coffee Street, Ho Chi Minh City</div>

        <div class="info">
          <div><strong>Receipt No:</strong><br/>#${order.id}</div>
          <div style="text-align:right"><strong>Date & Time</strong><br/>${date}</div>
        </div>

        <div class="info">
          <div><strong>Customer:</strong> ${order.buyer?.name || "—"}</div>
          <div><strong>Phone:</strong> ${order.buyer?.phone || "—"}</div>
        </div>

        <hr/>

        <div class="row header">
          <span style="flex:1">Item</span>
          <span style="width:40px;text-align:center">Qty</span>
          <span style="width:80px;text-align:right">Amount</span>
        </div>

        ${items.map(i => `
          <div class="row">
            <span style="flex:1">${i.productName || i.name || "—"}</span>
            <span style="width:40px;text-align:center">${i.quantity ?? i.qty ?? 0}</span>
            <span style="width:80px;text-align:right">${fmtVND(i.subtotal ?? (i.quantity || i.qty || 0) * (i.unitPrice || i.price || 0))}</span>
          </div>
        `).join("")}

        <hr/>

        <div class="row"><span>Subtotal</span><span>${fmtVND(subtotal)}</span></div>

        <div class="total-line">
          <div class="total"><span>Total</span><span>${fmtVND(total)}</span></div>
        </div>

        <div class="footer">Thank you for choosing Lumière Coffee.</div>
      </div>
      <script>window.onload = function() { window.print(); }<\/script>
    </body>
    </html>
  `;

  const win = window.open("", "_blank", "width=500,height=700");
  win.document.write(html);
  win.document.close();
}

const truncateId = (id) => (id && id.length > 10 ? `...${id.slice(-6)}` : id);

export default function OrderTable({ orders, pagination }) {
  const navigate = useNavigate();

  const headers = [
    "Order ID",
    "Order Date",
    <div className={styles.centerCol}>Total (₫)</div>,
    <div className={styles.centerCol}>Status</div>,
    <div className={styles.centerCol}>Actions</div>
  ];

  return (
    <AdminTable
      headers={headers}
      gridTemplate="120px 1fr 140px 100px 100px"
      noData={orders.length === 0}
      noDataText="No orders found."
      pagination={pagination}
    >
      {orders.map((order) => (
        <div key={order.id} className={styles.tableRow}>
          {/* ID */}
          <div className={styles.orderId} title={order.id}>{truncateId(order.id)}</div>

          {/* ORDER DATE */}
          <div>
            <div className={styles.customerName}>
              {order.createdAt ? new Date(order.createdAt).toLocaleDateString() : "—"}
            </div>
            <div className={styles.customerPhone}>
              {order.createdAt ? new Date(order.createdAt).toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" }) : ""}
            </div>
          </div>

          {/* TOTAL */}
          <div className={styles.totalAmount}>
            {new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(order.totalAmount || 0)}
          </div>

          {/* STATUS */}
          <div className={styles.centerCol}>
            <StatusBadge status={order.status} />
          </div>

          {/* ACTIONS */}
          <div className={styles.actionsCol}>
            <button
              onClick={() => navigate(`/admin/orders/${order.id}`)}
              className={styles.iconBtn}
              title="View order"
            >
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                <circle cx="12" cy="12" r="3"/>
              </svg>
            </button>
            <button
              onClick={() => handlePrintReceipt(order)}
              className={`${styles.iconBtn} ${styles.iconBtnPrimary}`}
              title="Print receipt"
            >
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <polyline points="6 9 6 2 18 2 18 9"/>
                <path d="M6 18H4a2 2 0 0 1-2-2v-5a2 2 0 0 1 2-2h16a2 2 0 0 1 2 2v5a2 2 0 0 1-2 2h-2"/>
                <rect x="6" y="14" width="12" height="8"/>
              </svg>
            </button>
          </div>
        </div>
      ))}
    </AdminTable>
  );
}
