export default function Receipt({ order }) {
  return (
    <div className="receiptContainer" style={{ marginTop: 20 }}>
      <h3>Store Name</h3>
      <p>Order: {order.orderNumber}</p>
      <p>Date: {new Date(order.orderTime).toLocaleString()}</p>

      <hr />

      <p>Customer: {order.customer.customerName}</p>
      <p>Phone: {order.customer.contactNumber}</p>
      <p>Address: {order.customer.deliveryAddress}</p>

      <hr />

      <table width="100%">
        <thead>
          <tr>
            <th>Product</th>
            <th>Qty</th>
            <th>Price</th>
            <th>Subtotal</th>
          </tr>
        </thead>
        <tbody>
          {order.items.map((item, idx) => (
            <tr key={idx}>
              <td>{item.productName}</td>
              <td>{item.quantity}</td>
              <td>{item.unitPrice}</td>
              <td>{item.subtotal}</td>
            </tr>
          ))}
        </tbody>
      </table>

      <hr />

      <h4>Total: {order.pricing.totalAmount}</h4>
      <p>Payment: {order.payment.paymentMethod}</p>
      <p>Status: {order.payment.paymentStatus}</p>
    </div>
  );
}
