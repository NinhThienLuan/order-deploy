// Sidebar navigation items for Franchise Admin
export const SIDEBAR_NAV = {
  overview: [
    { label: 'Dashboard', icon: 'dashboard', path: '/admin' },
  ],
  franchise: [
    { label: 'Stores Directory', icon: 'store', path: '/admin/stores' },
    { label: 'Master Catalog', icon: 'catalog', path: '/admin/catalog' },
    { label: 'Categories', icon: 'catalog', path: '/admin/categories' },
    { label: 'Global Settings', icon: 'settings', path: '/admin/settings' },
  ],
};

// Mock stats for dashboard
export const DASHBOARD_STATS = [
  { label: 'Total Revenue', value: '124.250.000 ₫', prefix: '₫' },
  { label: 'Active Stores', value: '18' },
  { label: 'Master Products', value: '142' },
  { label: 'Pending Approvals', value: '5', highlight: true },
];

// Mock top performing stores
export const TOP_STORES = [
  { id: 'STR-D1', name: 'District 1 Flagship', amount: '45.000.000 ₫', status: 'Excellent' },
  { id: 'STR-D2', name: 'District 2 Premium', amount: '32.500.000 ₫', status: 'Good' },
  { id: 'STR-D7', name: 'District 7 Hub', amount: '28.000.000 ₫', status: 'Good' },
  { id: 'STR-D4', name: 'District 4 Express', amount: '18.750.000 ₫', status: 'Needs Attention' },
];

// Quick actions on dashboard
export const QUICK_ACTIONS = [
  { label: '+ New Master Product', variant: 'primary', path: '/admin/catalog/new' },
  { label: '+ Add New Store', variant: 'outline', path: '/admin/stores/new' },
  { label: 'Global Reports', variant: 'outline', path: '/admin/reports' },
];

// Store Manager sidebar navigation
export const STORE_SIDEBAR_NAV = {
  overview: [
    { label: 'Dashboard', icon: 'dashboard', path: '/admin' },
  ],
  management: [
    { label: 'Product Catalog', icon: 'catalog', path: '/admin/stores/products' },
    { label: 'Orders', icon: 'orders', path: '/admin/stores/orders' },
    { label: 'Staff', icon: 'staff', path: '/admin/stores/staff' },
    { label: 'Settings', icon: 'settings', path: '/admin/stores/settings' },
  ],
};

// Store Manager mock stats
export const STORE_STATS = [
  { label: 'Today Revenue', value: '3.450.000 ₫', prefix: '₫' },
  { label: 'Pending Orders', value: '12' },
  { label: 'Products', value: '86' },
  { label: 'Staff On Duty', value: '8' },
];

// Store Manager recent orders
export const STORE_RECENT_ORDERS = [
  { id: 'ORD-1201', customer: 'Nguyen Van A', items: 3, total: '24.500 ₫', status: 'Preparing' },
  { id: 'ORD-1202', customer: 'Tran Thi B', items: 1, total: '8.000 ₫', status: 'Completed' },
  { id: 'ORD-1203', customer: 'Le Van C', items: 5, total: '42.000 ₫', status: 'Pending' },
  { id: 'ORD-1204', customer: 'Pham Thi D', items: 2, total: '16.500 ₫', status: 'Completed' },
];
