import { useParams } from "react-router-dom";

const formatVND = (n) =>
  new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(n ?? 0);

export default function OrderDetailPage() {
  const { id } = useParams();

  const order = {
    id,
    date: "June 26, 2026 | 07:42 PM",
    customer: "Nguyen Van A",
    phone: "0901234567",
    items: [
      { name: "Truffle Latte", qty: 1, price: 55000 },
      { name: "Roasted Arabica", qty: 2, price: 42000 },
      { name: "Matcha Cake", qty: 1, price: 68000 },
    ],
  };

  const subtotal = order.items.reduce(
    (sum, item) => sum + item.qty * item.price,
    0
  );

  const service = subtotal * 0.1;
  const vat = subtotal * 0.2;
  const total = subtotal + service + vat;

  const handlePrint = () => {
    window.print();
  };

  return (
    <>
      {/* Print Button */}
      <div
        style={{
          display: "flex",
          justifyContent: "flex-end",
          marginBottom: 20,
        }}
      >
        <button
          onClick={handlePrint}
          style={{
            background: "#d97706",
            color: "white",
            border: "none",
            padding: "10px 20px",
            borderRadius: 8,
            cursor: "pointer",
            fontWeight: 600,
          }}
        >
          🖨 Print Receipt
        </button>
      </div>

      {/* Receipt Card */}
      <div
        className="receipt"
        style={{
          maxWidth: 600,
          margin: "0 auto",
          background: "white",
          padding: 40,
          borderRadius: 20,
          boxShadow: "0 20px 60px rgba(0,0,0,0.08)",
        }}
      >
        <div style={{ textAlign: "center", marginBottom: 30 }}>
          <h1 style={{ fontSize: 28, fontWeight: 700 }}>
            Lumière Coffee
          </h1>
          <div style={{ color: "#888", fontSize: 13 }}>
            123 Coffee Street, Ho Chi Minh City
          </div>
        </div>

        {/* Info */}
        <div
          style={{
            display: "flex",
            justifyContent: "space-between",
            marginBottom: 20,
            fontSize: 14,
          }}
        >
          <div>
            <strong>Receipt No:</strong>
            <div>#{order.id}</div>
          </div>

          <div style={{ textAlign: "right" }}>
            <strong>Date & Time</strong>
            <div>{order.date}</div>
          </div>
        </div>

        <hr style={{ margin: "20px 0" }} />

        {/* Items Header */}
        <div
          style={{
            display: "grid",
            gridTemplateColumns: "1fr 60px 100px",
            fontWeight: 600,
            fontSize: 14,
            marginBottom: 10,
          }}
        >
          <div>Description</div>
          <div style={{ textAlign: "center" }}>Qty</div>
          <div style={{ textAlign: "right" }}>Amount</div>
        </div>

        {/* Items */}
        {order.items.map((item, index) => (
          <div
            key={index}
            style={{
              display: "grid",
              gridTemplateColumns: "1fr 60px 100px",
              marginBottom: 12,
              fontSize: 14,
            }}
          >
            <div>{item.name}</div>
            <div style={{ textAlign: "center" }}>{item.qty}</div>
            <div style={{ textAlign: "right" }}>
              {formatVND(item.qty * item.price)}
            </div>
          </div>
        ))}

        <hr style={{ margin: "25px 0" }} />

        {/* Totals */}
        <div style={{ fontSize: 14 }}>
          <div style={{ display: "flex", justifyContent: "space-between" }}>
            <span>Subtotal</span>
            <span>{formatVND(subtotal)}</span>
          </div>

          <div style={{ display: "flex", justifyContent: "space-between" }}>
            <span>Service Charge (10%)</span>
            <span>{formatVND(service)}</span>
          </div>

          <div style={{ display: "flex", justifyContent: "space-between" }}>
            <span>VAT (20%)</span>
            <span>{formatVND(vat)}</span>
          </div>
        </div>

        <hr style={{ margin: "20px 0", borderTop: "2px solid #000" }} />

        <div
          style={{
            display: "flex",
            justifyContent: "space-between",
            fontSize: 20,
            fontWeight: 700,
            color: "#d97706",
          }}
        >
          <span>Total</span>
          <span>{formatVND(total)}</span>
        </div>

        <div
          style={{
            marginTop: 30,
            textAlign: "center",
            fontSize: 12,
            color: "#888",
          }}
        >
          Thank you for choosing Lumière Coffee.
        </div>
      </div>

      {/* Print CSS */}
      <style>
        {`
        @media print {
          body * {
            visibility: hidden;
          }
          .receipt, .receipt * {
            visibility: visible;
          }
          .receipt {
            position: absolute;
            left: 0;
            top: 0;
            width: 100%;
          }
        }
      `}
      </style>
    </>
  );
}