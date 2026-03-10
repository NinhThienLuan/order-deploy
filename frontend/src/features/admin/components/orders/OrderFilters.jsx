import { ORDER_STATUS } from "@/features/admin/constants/orders/order.constants";

export default function OrderFilters({
  status,
  setStatus,
  from,
  setFrom,
  to,
  setTo,
  search,
  setSearch,
}) {
  return (
    <div style={{ marginBottom: 20 }}>
      <select value={status} onChange={(e) => setStatus(e.target.value)}>
        {ORDER_STATUS.map((s) => (
          <option key={s} value={s}>
            {s}
          </option>
        ))}
      </select>

      <input
        type="date"
        value={from}
        onChange={(e) => setFrom(e.target.value)}
      />

      <input
        type="date"
        value={to}
        onChange={(e) => setTo(e.target.value)}
      />

      <input
        type="text"
        placeholder="Search Order ID or Phone"
        value={search}
        onChange={(e) => setSearch(e.target.value)}
      />
    </div>
  );
}
