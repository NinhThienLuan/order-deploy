export const NAV_ITEMS = [
    { label: 'Home', href: '/' },
    {
        label: 'Menu',
        href: '/menu',
        mega: {
            columns: [
                { heading: 'Browse by Category', links: [] },
                { heading: 'Featured', links: [] }
            ],
            footer: [
                { label: 'New Arrivals', href: '/menu?sort=newest' },
                { label: 'Best Sellers', href: '/menu?sort=popular' }
            ]
        }
    },
    { label: 'Stores', href: '/stores' },
    { label: 'About', href: '/about' },
    { label: 'Admin', href: '/admin', adminOnly: true }
];
