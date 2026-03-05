// ── Main Navigation with Mega Menu ───────────────────────
// App-level navigation reflecting actual routes and user flows.
// `adminOnly: true` = Navbar hides this item unless user is ADMIN or STORE_MANAGER.
export const NAV_ITEMS = [
    {
        label: 'Menu',
        href: '/menu',
        mega: {
            columns: [
                {
                    heading: 'Browse by Category',
                    links: [
                        { label: 'All Products',           href: '/menu' },
                        { label: 'Espresso & Classics',    href: '/menu?category=cat-01' },
                        { label: 'Frappuccino / Blended',  href: '/menu?category=cat-02' },
                        { label: 'Tea & Non-Coffee',       href: '/menu?category=cat-03' },
                        { label: 'Cold & Iced',            href: '/menu?category=cat-07' },
                        { label: 'Food & Pastries',        href: '/menu?category=cat-06' },
                    ],
                },
                {
                    heading: 'Featured',
                    links: [
                        { label: 'Seasonal Specials',  href: '/menu?category=cat-05' },
                        { label: 'Retail & Equipment', href: '/menu?category=cat-04' },
                    ],
                },
            ],
            footer: [
                { label: 'All Products', href: '/menu' },
            ],
        },
    },
    {
        label: 'Orders',
        href: '/orders',
        mega: {
            columns: [
                {
                    heading: 'My Orders',
                    links: [
                        { label: 'All Orders',      href: '/orders' },
                        { label: 'Active Orders',   href: '/orders?filter=active' },
                        { label: 'Completed',       href: '/orders?filter=completed' },
                        { label: 'Cancelled',       href: '/orders?filter=cancelled' },
                    ],
                },
                {
                    heading: 'Details',
                    links: [
                        { label: 'Track an Order',  href: '/orders' },
                    ],
                },
            ],
            footer: [
                { label: 'All Orders', href: '/orders' },
            ],
        },
    },
    {
        label: 'Transactions',
        href: '/transactions',
        mega: {
            columns: [
                {
                    heading: 'By Period',
                    links: [
                        { label: 'All Transactions', href: '/transactions' },
                        { label: 'Today',            href: '/transactions?period=today' },
                        { label: 'Yesterday',        href: '/transactions?period=yesterday' },
                        { label: 'Last Week',        href: '/transactions?period=week' },
                        { label: 'Last Month',       href: '/transactions?period=month' },
                        { label: 'Last Year',        href: '/transactions?period=year' },
                    ],
                },
            ],
            footer: [
                { label: 'Transaction History', href: '/transactions' },
            ],
        },
    },
    {
        label: 'Stores',
        href: '/home',
        mega: {
            columns: [
                {
                    heading: 'Locations',
                    links: [
                        { label: 'URI Palace',   href: '/home' },
                        { label: 'Bac Ninh',     href: '/home' },
                        { label: 'Hanoi',        href: '/home' },
                        { label: 'Saigon',       href: '/home' },
                    ],
                },
                {
                    heading: 'Info',
                    links: [
                        { label: 'Find a Store',    href: '/home' },
                        { label: 'Opening Hours',   href: '/home' },
                        { label: 'Franchise Info',  href: '/home' },
                    ],
                },
            ],
            footer: [
                { label: 'All Stores', href: '/home' },
            ],
        },
    },
    {
        label: 'Admin',
        href: '/admin',
        adminOnly: true,
        mega: {
            columns: [
                {
                    heading: 'Dashboard',
                    links: [
                        { label: 'Overview',          href: '/admin' },
                        { label: 'Order Management',  href: '/admin/orders' },
                    ],
                },
                {
                    heading: 'Store Management',
                    links: [
                        { label: 'All Stores',        href: '/admin/stores' },
                        { label: 'Store Orders',      href: '/admin/stores/orders' },
                    ],
                },
            ],
            footer: [
                { label: 'Admin Dashboard',   href: '/admin' },
                { label: 'Store Management',  href: '/admin/stores' },
            ],
        },
    },
];
