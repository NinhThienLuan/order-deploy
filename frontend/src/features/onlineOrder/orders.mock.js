export const MOCK_ORDERS = [
  {
    id: 'ORD-001',
    customerName: 'Nguyễn Văn An',
    customerPhone: '0901.234.567',
    orderTime: '2024-01-15T10:30:00',
    items: [
      { name: 'Cà phê sữa đá', quantity: 2, price: 35000 },
      { name: 'Bánh mì que', quantity: 1, price: 15000 },
      { name: 'Nước ép cam', quantity: 1, price: 45000 }
    ],
    totalAmount: 130000,
    status: 'pending',
    note: 'Không đường, ít đá'
  },
  {
    id: 'ORD-002',
    customerName: 'Trần Thị Bình',
    customerPhone: '0902.345.678',
    orderTime: '2024-01-15T10:45:00',
    items: [
      { name: 'Matcha latte', quantity: 1, price: 49000 },
      { name: 'Bánh tiramisu', quantity: 1, price: 55000 }
    ],
    totalAmount: 104000,
    status: 'pending',
    note: 'Gói mang đi'
  },
  {
    id: 'ORD-003',
    customerName: 'Lê Văn Cường',
    customerPhone: '0903.456.789',
    orderTime: '2024-01-15T11:00:00',
    items: [
      { name: 'Americano', quantity: 2, price: 39000 },
      { name: 'Bánh sừng bò', quantity: 2, price: 25000 },
      { name: 'Nước suối', quantity: 1, price: 10000 }
    ],
    totalAmount: 138000,
    status: 'pending',
    note: ''
  },
  {
    id: 'ORD-004',
    customerName: 'Phạm Thị Dung',
    customerPhone: '0904.567.890',
    orderTime: '2024-01-15T11:15:00',
    items: [
      { name: 'Cappuccino', quantity: 1, price: 45000 },
      { name: 'Bánh cookies', quantity: 3, price: 12000 }
    ],
    totalAmount: 81000,
    status: 'pending',
    note: 'Nóng'
  },
  {
    id: 'ORD-005',
    customerName: 'Hoàng Văn Em',
    customerPhone: '0905.678.901',
    orderTime: '2024-01-15T11:30:00',
    items: [
      { name: 'Sinh tố bơ', quantity: 2, price: 55000 },
      { name: 'Bánh mì nướng', quantity: 1, price: 25000 }
    ],
    totalAmount: 135000,
    status: 'pending',
    note: 'Giao nhanh'
  }
];

export const ORDER_STATUS = {
  PENDING: 'pending',
  CONFIRMED: 'confirmed',
  REJECTED: 'rejected',
  COMPLETED: 'completed'
};